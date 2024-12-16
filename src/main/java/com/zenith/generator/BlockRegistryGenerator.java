package com.zenith.generator;

import com.google.common.base.Suppliers;
import com.palantir.javapoet.CodeBlock;
import com.zenith.extension.IBlockProperties;
import com.zenith.mc.block.Block;
import com.zenith.mc.block.BlockOffsetType;
import com.zenith.mc.block.BlockTags;
import com.zenith.mixin.AccessorBlockBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class BlockRegistryGenerator extends RegistryGenerator<Block> {

    public BlockRegistryGenerator() {
        super(Block.class, Block.class.getPackage().getName(), "BlockRegistry");
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

    private @Nullable EnumSet<BlockTags> getZenithBlockTags(final net.minecraft.world.level.block.Block block) {
        var set = EnumSet.noneOf(BlockTags.class);
        if (AXE_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_AXE);
        if (HOE_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_HOE);
        if (PICKAXE_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_PICKAXE);
        if (SHOVEL_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.MINEABLE_WITH_SHOVEL);
        if (SWORD_MINEABLE_BLOCKS.get().contains(block)) set.add(BlockTags.SWORD_EFFICIENT);
        if (NEEDS_DIAMOND_TOOL_BLOCKS.get().contains(block)) set.add(BlockTags.NEEDS_DIAMOND_TOOL);
        if (NEEDS_IRON_TOOL_BLOCKS.get().contains(block)) set.add(BlockTags.NEEDS_IRON_TOOL);
        if (NEEDS_STONE_TOOL_BLOCKS.get().contains(block)) set.add(BlockTags.NEEDS_STONE_TOOL);
        if (set.isEmpty()) return null;
        return set;
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
                .map(type -> type.builtInRegistryHolder().key().location().getPath())
                .orElse("")
                .toUpperCase(Locale.ENGLISH);
            var mcplBlockEntityType = blockEntityTypeStr.isEmpty() ? null : BlockEntityType.valueOf(blockEntityTypeStr);
            Block data = new Block(
                blockRegistry.getId(block),
                registryKey.getPath(),
                !block.defaultBlockState().getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty(),
                net.minecraft.world.level.block.Block.getId(blockStates.getFirst()),
                net.minecraft.world.level.block.Block.getId(blockStates.getLast()),
                block.defaultMapColor().id,
                ((IBlockProperties) block.properties()).dg$getOffsetType(),
                ((AccessorBlockBehavior) block).invokeGetMaxHorizontalOffset(),
                ((AccessorBlockBehavior) block).invokeGetMaxVerticalOffset(),
                ((IBlockProperties) block.properties()).dg$getDestroySpeed(),
                ((IBlockProperties) block.properties()).dg$requiresCorrectToolForDrops(),
                getZenithBlockTags(block),
                mcplBlockEntityType);
            blockList.add(data);
        });
        return blockList;
    }

    @Override
    public CodeBlock dataInitializer(final Block data) {

        CodeBlock baseConstructor = CodeBlock.of(
            "new $T($L, $S, $L, $L, $L, $L, $T.$L, $Lf, $Lf, $Lf, $L",
            Block.class,
            data.id(),
            data.name(),
            data.isBlock(),
            data.minStateId(),
            data.maxStateId(),
            data.mapColorId(),
            BlockOffsetType.class,
            data.offsetType(),
            data.maxHorizontalOffset(),
            data.maxVerticalOffset(),
            data.destroySpeed(),
            data.requiresCorrectToolForDrops()
        );

        CodeBlock blockTagsPart = CodeBlock.of("");

        if (data.blockTags() != null) {
            var builder = CodeBlock.builder()
                .add(", $T.of(", EnumSet.class);
            BlockTags[] tagsArray = data.blockTags().toArray(BlockTags[]::new);
            for (int i = 0; i < tagsArray.length; i++) {
                BlockTags tag = tagsArray[i];
                if (i > 0) {
                    builder.add(", ");
                }
                builder.add("$T.$L", BlockTags.class, tag);
            }
            blockTagsPart = builder.add(")").build();
        }

        CodeBlock blockEntityTypePart = CodeBlock.of("");
        if (data.blockEntityType() != null) {
            blockEntityTypePart = CodeBlock.of(
                ", $T.$L",
                BlockEntityType.class,
                data.blockEntityType()
            );
        }

        return baseConstructor.toBuilder()
            .add(blockTagsPart)
            .add(blockEntityTypePart)
            .add(")")
            .build();
    }
}
