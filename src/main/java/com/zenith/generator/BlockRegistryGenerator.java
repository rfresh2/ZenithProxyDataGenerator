package com.zenith.generator;

import com.squareup.javapoet.CodeBlock;
import com.zenith.extension.IBlockProperties;
import com.zenith.mc.block.Block;
import com.zenith.mc.block.BlockOffsetType;
import com.zenith.mixin.AccessorBlockBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockRegistryGenerator extends RegistryGenerator<Block> {

    public BlockRegistryGenerator() {
        super(Block.class, Block.class.getPackage().getName(), "BlockRegistry");
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
                ((IBlockProperties) block.properties()).getOffsetType(),
                ((AccessorBlockBehavior) block).invokeGetMaxHorizontalOffset(),
                ((AccessorBlockBehavior) block).invokeGetMaxVerticalOffset(),
                mcplBlockEntityType);
            blockList.add(data);
        });
        return blockList;
    }

    @Override
    public CodeBlock dataInitializer(final Block data) {
        if (data.blockEntityType() == null) {
            return CodeBlock.of(
                "new $T($L, $S, $L, $L, $L, $L, $T.$L, $Lf, $Lf, $L)",
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
                null
            );
        }
        return CodeBlock.of(
            "new $T($L, $S, $L, $L, $L, $L, $T.$L, $Lf, $Lf, $T.$L)",
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
            BlockEntityType.class,
            data.blockEntityType()
        );
    }
}
