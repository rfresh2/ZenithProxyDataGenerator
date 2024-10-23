package com.zenith.generator;

import com.zenith.DataGenerator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static com.zenith.DataGenerator.LOG;
import static net.minecraft.world.level.block.Block.getId;

public class WaterLoggedStatesGenerator implements Generator {
    @Override
    public void generate() {
        var list = new IntArrayList();
        var blockRegistry = BuiltInRegistries.BLOCK;
        blockRegistry.forEach(block -> {
            List<BlockState> blockStates = block.getStateDefinition().getPossibleStates();
            for (var state : blockStates) {
                if (!state.hasProperty(BlockStateProperties.WATERLOGGED)) continue;
                int id = getId(state);
                var waterLoggedState = state.getValue(BlockStateProperties.WATERLOGGED);
                if (waterLoggedState) {
                    list.add(id);
                }
            }
        });

        try (Writer out = new FileWriter(DataGenerator.outputFile("waterloggedBlockStateIds.json"))) {
            DataGenerator.gson.toJson(list, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Dumped waterloggedBlockStateIds.json");
    }
}
