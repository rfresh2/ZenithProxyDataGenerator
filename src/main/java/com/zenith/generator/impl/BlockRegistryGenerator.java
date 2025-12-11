package com.zenith.generator.impl;

import com.google.common.base.Suppliers;
import com.zenith.extension.IBlockProperties;
import com.zenith.generator.JsonRegistryGenerator;
import com.zenith.mc.block.Block;
import com.zenith.mc.block.BlockRegistrySpec;
import com.zenith.mc.block.BlockTags;
import com.zenith.mixin.AccessorBlockBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

import java.util.*;
import java.util.function.Supplier;

public class BlockRegistryGenerator extends JsonRegistryGenerator<Block> {
    public BlockRegistryGenerator() {
        super(Block.class, "BlockRegistry", BlockRegistrySpec.class, "blocks.json");
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
                ((IBlockProperties) block.properties()).dg$isReplaceable(),
                ((IBlockProperties) block.properties()).dg$getFriction(),
                ((IBlockProperties) block.properties()).dg$getSpeedFactor(),
                ((IBlockProperties) block.properties()).dg$getJumpFactor(),
                ((IBlockProperties) block.properties()).dg$isAir(),
                mcplBlockEntityType);
            blockList.add(data);
        });
        return blockList;
    }

    private Set<net.minecraft.world.level.block.Block> getTaggedBlocks(TagKey<net.minecraft.world.level.block.Block> tag) {
        Set<net.minecraft.world.level.block.Block> blocks = new HashSet<>();
        BuiltInRegistries.BLOCK.getOrThrow(tag).stream()
            .forEach(t -> blocks.add(t.value()));
        return blocks;
    }

    final Supplier<Set<net.minecraft.world.level.block.Block>> AXE_MINEABLE_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE));
    final Supplier<Set<net.minecraft.world.level.block.Block>> HOE_MINEABLE_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.MINEABLE_WITH_HOE));
    final Supplier<Set<net.minecraft.world.level.block.Block>> PICKAXE_MINEABLE_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE));
    final Supplier<Set<net.minecraft.world.level.block.Block>> SHOVEL_MINEABLE_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.MINEABLE_WITH_SHOVEL));
    final Supplier<Set<net.minecraft.world.level.block.Block>> SWORD_MINEABLE_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.SWORD_EFFICIENT));
    final Supplier<Set<net.minecraft.world.level.block.Block>> NEEDS_DIAMOND_TOOL_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL));
    final Supplier<Set<net.minecraft.world.level.block.Block>> NEEDS_IRON_TOOL_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL));
    final Supplier<Set<net.minecraft.world.level.block.Block>> NEEDS_STONE_TOOL_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.NEEDS_STONE_TOOL));
    final Supplier<Set<net.minecraft.world.level.block.Block>> CLIMBABLE_BLOCKS = Suppliers.memoize(() -> getTaggedBlocks(net.minecraft.tags.BlockTags.CLIMBABLE));

    private EnumSet<BlockTags> getZenithBlockTags(final net.minecraft.world.level.block.Block block) {
        var set = EnumSet.noneOf(BlockTags.class);
        if (AXE_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_AXE);
        if (HOE_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_HOE);
        if (PICKAXE_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_PICKAXE);
        if (SHOVEL_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_SHOVEL);
        if (SWORD_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.SWORD_EFFICIENT);
        if (NEEDS_DIAMOND_TOOL_BLOCKS.get().contains(block)) set.add(BlockTags.NEEDS_DIAMOND_TOOL);
        if (NEEDS_IRON_TOOL_BLOCKS.get().contains(block)) set.add(BlockTags.NEEDS_IRON_TOOL);
        if (NEEDS_STONE_TOOL_BLOCKS.get().contains(block)) set.add(BlockTags.NEEDS_STONE_TOOL);
        if (CLIMBABLE_BLOCKS.get().contains(block)) set.add(BlockTags.CLIMBABLE);
        if (set.isEmpty()) return EnumSet.noneOf(BlockTags.class);
        return set;
    }
}
