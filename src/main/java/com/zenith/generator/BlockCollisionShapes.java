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

    private static class BlockShapesCache {
        private final ShapeAccessor shapeAccessor;
        public LinkedHashMap<VoxelShape, Integer> shapeToShapeId = new LinkedHashMap<>();
        public LinkedHashMap<Block, List<Integer>> blockToShapes = new LinkedHashMap<>();
        private int lastCollisionShapeId = 0;

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
                    Vec3 reverseOffset = blockState.getOffset(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).reverse();
                    blockShape = blockShape.move(reverseOffset.x(), reverseOffset.y(), reverseOffset.z());
                }

                Integer blockShapeIndex = shapeToShapeId.get(blockShape);

                if (blockShapeIndex == null) {
                    blockShapeIndex = lastCollisionShapeId++;
                    shapeToShapeId.put(blockShape, blockShapeIndex);
                }
                blockCollisionShapes.add(blockShapeIndex);
            }

            this.blockToShapes.put(block, blockCollisionShapes);
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
                entry.getKey().forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
                    JsonArray oneBoxJsonArray = new JsonArray();

                    oneBoxJsonArray.add(x1);
                    oneBoxJsonArray.add(y1);
                    oneBoxJsonArray.add(z1);

                    oneBoxJsonArray.add(x2);
                    oneBoxJsonArray.add(y2);
                    oneBoxJsonArray.add(z2);

                    boxesArray.add(oneBoxJsonArray);
                });
                shapesObject.add(Integer.toString(entry.getValue()), boxesArray);
            }
            return shapesObject;
        }
    }
}
