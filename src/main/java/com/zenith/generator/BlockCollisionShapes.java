package com.zenith.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.zenith.DataGenerator;
import com.zenith.extension.IBlockProperties;
import com.zenith.mc.block.BlockOffsetType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class BlockCollisionShapes implements Generator {
    @Override
    public void generate() {
        var blockRegistry = BuiltInRegistries.BLOCK;
        BlockShapesCache collisionShapesCache = new BlockShapesCache((blockState -> blockState.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)));
        BlockShapesCache interactionShapesCache = new BlockShapesCache((blockState -> blockState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)));

        blockRegistry.forEach(collisionShapesCache::processBlock);
        blockRegistry.forEach(interactionShapesCache::processBlock);

        writeCache("blockCollisionShapes", collisionShapesCache, blockRegistry);
        writeCache("blockInteractionShapes", interactionShapesCache, blockRegistry);
    }

    private void writeCache(String name, BlockShapesCache cache, DefaultedRegistry<Block> blockRegistry) {
        JsonObject resultObject = new JsonObject();
        resultObject.add("blocks", cache.dumpBlockShapeIndices(blockRegistry));
        resultObject.add("shapes", cache.dumpShapesObject());
        resultObject.add("boxes", cache.dumpBoxShapeIndices());

        try (Writer out = new FileWriter(DataGenerator.outputFile(name + ".json"))) {
            DataGenerator.gson.toJson(resultObject, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DataGenerator.LOG.info("Dumped {}.json", name);
    }

    @FunctionalInterface
    interface ShapeAccessor {
        VoxelShape getCollisionShape(BlockState blockState);
    }

    record Box(double x1, double x2, double y1, double y2, double z1, double z2) { }

    private static class BlockShapesCache {
        private final ShapeAccessor shapeAccessor;
        public LinkedHashMap<VoxelShape, Integer> shapeToShapeId = new LinkedHashMap<>();
        public LinkedHashMap<Block, List<Integer>> blockToShapes = new LinkedHashMap<>();
        public LinkedHashMap<VoxelShape, List<Box>> shapeToBoxes = new LinkedHashMap<>();
        public LinkedHashMap<Box, Integer> boxToBoxId = new LinkedHashMap<>();
        private int lastCollisionShapeId = 0;
        private int lastBoxId = 0;

        public BlockShapesCache(ShapeAccessor shapeAccessor) {
            this.shapeAccessor = shapeAccessor;
        }

        public void processBlock(Block block) {
            List<BlockState> blockStates = block.getStateDefinition().getPossibleStates();
            List<Integer> blockCollisionShapes = new ArrayList<>();

            for (BlockState blockState : blockStates) {
                VoxelShape blockShape = shapeAccessor.getCollisionShape(blockState);
                BlockOffsetType offsetType = ((IBlockProperties) blockState.getBlock().properties()).dg$getOffsetType();
                if (offsetType != BlockOffsetType.NONE) {
                    Vec3 reverseOffset = blockState.getOffset(BlockPos.ZERO).reverse();
                    blockShape = blockShape.move(reverseOffset.x(), reverseOffset.y(), reverseOffset.z());
                }
                List<Box> boxes = getBoxes(blockShape);
                for (Box box : boxes) {
                    Integer boxId = boxToBoxId.get(box);
                    if (boxId == null) {
                        boxId = lastBoxId++;
                        boxToBoxId.put(box, boxId);
                    }
                }
                shapeToBoxes.put(blockShape, boxes);

                Integer blockShapeIndex = shapeToShapeId.get(blockShape);

                if (blockShapeIndex == null) {
                    blockShapeIndex = lastCollisionShapeId++;
                    shapeToShapeId.put(blockShape, blockShapeIndex);
                }
                blockCollisionShapes.add(blockShapeIndex);
            }

            this.blockToShapes.put(block, blockCollisionShapes);
        }

        private List<Box> getBoxes(VoxelShape shape) {
            List<Box> boxes = new ArrayList<>();
            shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
                Box box = new Box(x1, x2, y1, y2, z1, z2);
                boxes.add(box);
            });
            return boxes;
        }

        public JsonObject dumpBlockShapeIndices(Registry<Block> blockRegistry) {
            JsonObject resultObject = new JsonObject();

            for (var entry : blockToShapes.entrySet()) {
                List<Integer> blockCollisions = entry.getValue();
                long distinctShapesCount = blockCollisions.stream().distinct().count();
                JsonElement blockCollision;
                if (distinctShapesCount == 1L) {
                    blockCollision = new JsonPrimitive(blockCollisions.get(0));
                } else {
                    blockCollision = new JsonArray();
                    for (int collisionId : blockCollisions) {
                        ((JsonArray) blockCollision).add(collisionId);
                    }
                }

                resultObject.add(Integer.toString(blockRegistry.getId(entry.getKey())), blockCollision);
            }

            return resultObject;
        }

        public JsonObject dumpShapesObject() {
            JsonObject shapesObject = new JsonObject();

            for (var entry : shapeToShapeId.entrySet()) {
                JsonArray boxesArray = new JsonArray();
                List<Integer> shapeToBoxIds = new ArrayList<>();
                List<Box> boxes = shapeToBoxes.get(entry.getKey());
                for (Box box : boxes) {
                    int boxId = boxToBoxId.get(box);
                    shapeToBoxIds.add(boxId);
                }
                for (int boxId : shapeToBoxIds) {
                    boxesArray.add(boxId);
                }
                shapesObject.add(Integer.toString(entry.getValue()), boxesArray);
            }
            return shapesObject;
        }

        public JsonObject dumpBoxShapeIndices() {
            JsonObject boxShapeIndices = new JsonObject();

            for (var entry : boxToBoxId.entrySet()) {
                Box box = entry.getKey();
                JsonArray boxArray = new JsonArray();
                boxArray.add(box.x1());
                boxArray.add(box.x2());
                boxArray.add(box.y1());
                boxArray.add(box.y2());
                boxArray.add(box.z1());
                boxArray.add(box.z2());

                boxShapeIndices.add(Integer.toString(entry.getValue()), boxArray);
            }

            return boxShapeIndices;
        }
    }
}
