package com.zenith.generator.impl;

import com.zenith.DataGenerator;
import com.zenith.generator.Generator;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;

import java.io.FileWriter;
import java.io.Writer;
import java.util.List;

public class PathfindableGenerator implements Generator {
    @Override
    public void generate() {
        var list = new IntArrayList();
        BuiltInRegistries.BLOCK.forEach(block -> {
            List<BlockState> possibleStates = block.getStateDefinition().getPossibleStates();
            possibleStates.forEach(state -> {
                var blockStateId = Block.getId(state);
                boolean pathfindable = state.isPathfindable(PathComputationType.LAND);
                if (pathfindable) {
                    list.add(blockStateId);
                }
            });
        });
        try (Writer out = new FileWriter(DataGenerator.outputFile("pathfindable.json"))) {
            DataGenerator.gson.toJson(list, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DataGenerator.LOG.info("Dumped pathfindable.json");
    }
}
