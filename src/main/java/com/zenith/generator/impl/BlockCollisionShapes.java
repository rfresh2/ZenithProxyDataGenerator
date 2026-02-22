package com.zenith.generator.impl;

import com.zenith.DataGenerator;
import com.zenith.extension.IBlockProperties;
import com.zenith.generator.Generator;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> resultObject = new LinkedHashMap<>();
        resultObject.put("blocks", cache.dumpBlockShapeIndices(blockRegistry));
        resultObject.put("shapes", cache.dumpShapesObject());
        resultObject.put("boxes", cache.dumpBoxShapeIndices());

        DataGenerator.writeSmile(name + ".smile", resultObject);
        DataGenerator.LOG.info("Dumped {}.smile", name);
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

        public Map<String, Object> dumpBlockShapeIndices(Registry<Block> blockRegistry) {
            Map<String, Object> resultObject = new LinkedHashMap<>();

            for (var entry : blockToShapes.entrySet()) {
                List<Integer> blockCollisions = entry.getValue();
                long distinctShapesCount = blockCollisions.stream().distinct().count();
                Object blockCollision;
                if (distinctShapesCount == 1L) {
                    blockCollision = blockCollisions.get(0);
                } else {
                    blockCollision = blockCollisions;
                }

                resultObject.put(Integer.toString(blockRegistry.getId(entry.getKey())), blockCollision);
            }

            return resultObject;
        }

        public Map<String, List<Integer>> dumpShapesObject() {
            Map<String, List<Integer>> shapesObject = new LinkedHashMap<>();

            for (var entry : shapeToShapeId.entrySet()) {
                List<Integer> shapeToBoxIds = new ArrayList<>();
                List<Box> boxes = shapeToBoxes.get(entry.getKey());
                for (Box box : boxes) {
                    int boxId = boxToBoxId.get(box);
                    shapeToBoxIds.add(boxId);
                }
                shapesObject.put(Integer.toString(entry.getValue()), shapeToBoxIds);
            }
            return shapesObject;
        }

        public Map<String, List<Double>> dumpBoxShapeIndices() {
            Map<String, List<Double>> boxShapeIndices = new LinkedHashMap<>();

            for (var entry : boxToBoxId.entrySet()) {
                Box box = entry.getKey();
                List<Double> boxArray = List.of(box.x1(), box.x2(), box.y1(), box.y2(), box.z1(), box.z2());
                boxShapeIndices.put(Integer.toString(entry.getValue()), boxArray);
            }

            return boxShapeIndices;
        }
    }
}
