package com.zenith.generator.impl;

import com.zenith.extension.IBlockProperties;
import com.zenith.generator.JsonRegistryGenerator;
import com.zenith.mc.block.Block;
import com.zenith.mc.block.BlockRegistrySpec;
import com.zenith.mc.block.BlockTags;
import com.zenith.mixin.AccessorBlockBehavior;
import lombok.SneakyThrows;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

import java.util.*;

public class BlockRegistryGenerator extends JsonRegistryGenerator<Block> {
    public BlockRegistryGenerator() {
        super(Block.class, "BlockRegistry", BlockRegistrySpec.class, "blocks.smile");
    }

    @Override
    public List<Block> buildDataList() {
        List<Block> blockList = new ArrayList<>();
        var blockRegistry = BuiltInRegistries.BLOCK;
        blockRegistry.forEach(block -> {
            List<BlockState> blockStates = block.getStateDefinition().getPossibleStates();
            var registryKey = blockRegistry.getKey(block);
            var blockEntityTypeStr = BuiltInRegistries.BLOCK_ENTITY_TYPE.stream()
                .filter(type -> type.isValid(block.defaultBlockState()))
                .findFirst()
                .map(type -> type.builtInRegistryHolder().key().identifier().getPath())
                .orElse("")
                .toUpperCase(Locale.ENGLISH);
            var mcplBlockEntityType = blockEntityTypeStr.isEmpty() ? null : BlockEntityType.valueOf(blockEntityTypeStr);
            Block data = new Block(
                blockRegistry.getId(block),
                registryKey.getPath(),
                net.minecraft.world.level.block.Block.getId(blockStates.getFirst()),
                net.minecraft.world.level.block.Block.getId(blockStates.getLast()),
                block.defaultMapColor().id,
                ((IBlockProperties) block.properties()).dg$getOffsetType(),
                ((AccessorBlockBehavior) block).invokeGetMaxHorizontalOffset(),
                ((AccessorBlockBehavior) block).invokeGetMaxVerticalOffset(),
                block.defaultBlockState().isSolid(),
                ((IBlockProperties) block.properties()).dg$getDestroySpeed(),
                ((IBlockProperties) block.properties()).dg$requiresCorrectToolForDrops(),
                getZenithBlockTags(block),
                ((IBlockProperties) block.properties()).dg$getFriction(),
                ((IBlockProperties) block.properties()).dg$getSpeedFactor(),
                ((IBlockProperties) block.properties()).dg$getJumpFactor(),
                block instanceof FallingBlock,
                mcplBlockEntityType);
            blockList.add(data);
        });
        return blockList;
    }

    @SneakyThrows
    private EnumSet<BlockTags> getZenithBlockTags(final net.minecraft.world.level.block.Block block) {
        var set = EnumSet.noneOf(BlockTags.class);

        var blockRegRef = BuiltInRegistries.BLOCK.getOrThrow(block.builtInRegistryHolder().key());
        var tagsOnBlock = blockRegRef.tags()
            .filter(tag -> tag.location().getNamespace().equals("minecraft"))
            .toList();
        var tagFields = Arrays.stream(net.minecraft.tags.BlockTags.class.getDeclaredFields())
            .filter(field -> field.getType().equals(TagKey.class))
            .toList();
        var blockItemTagFields = Arrays.stream(net.minecraft.tags.BlockItemTags.class.getDeclaredFields())
            .filter(field -> field.getType().equals(BlockItemTagId.class))
            .toList();
        OUTER: for (var tag : tagsOnBlock) {
            for (var field : tagFields) {
                var fieldValue = (TagKey<Block>) field.get(null);
                if (fieldValue.location().equals(tag.location())) {
                    set.add(BlockTags.valueOf(field.getName()));
                    continue OUTER;
                }
            }
            for (var field : blockItemTagFields) {
                var fieldValue = (BlockItemTagId) field.get(null);
                if (fieldValue.block().location().equals(tag.location())) {
                    set.add(BlockTags.valueOf(field.getName()));
                    continue OUTER;
                }
            }
        }
        if (set.size() != tagsOnBlock.size()) {
            throw new RuntimeException("Failed to find all tags for block " + block.builtInRegistryHolder().key().identifier());
        }

        if (set.isEmpty()) return EnumSet.noneOf(BlockTags.class);
        return set;
    }
}
