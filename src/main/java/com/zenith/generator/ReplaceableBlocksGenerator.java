package com.zenith.generator;

import com.zenith.DataGenerator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

public class ReplaceableBlocksGenerator implements Generator {
    @Override
    public void generate() {
        var list = new IntArrayList();
        BuiltInRegistries.BLOCK.forEach(block -> {
            List<BlockState> possibleStates = block.getStateDefinition().getPossibleStates();
            possibleStates.forEach(state -> {
                var blockStateId = Block.getId(state);
                boolean replaceable = state.canBeReplaced();
                if (replaceable) {
                    list.add(blockStateId);
                }
            });
        });
        try (Writer out = new FileWriter(DataGenerator.outputFile("replaceable.json"))) {
            DataGenerator.gson.toJson(list, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DataGenerator.LOG.info("Dumped replaceable.json");
    }
}
