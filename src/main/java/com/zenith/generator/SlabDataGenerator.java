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

public class SlabDataGenerator implements Generator {
    @Override
    public void generate() {
        var topSlabs = new IntArrayList();
        var bottomSlabs = new IntArrayList();
        var doubleSlabs = new IntArrayList();

        var blockRegistry = BuiltInRegistries.BLOCK;
        blockRegistry.forEach(block -> {
            List<BlockState> blockStates = block.getStateDefinition().getPossibleStates();
            for (var state : blockStates) {
                if (!state.hasProperty(BlockStateProperties.SLAB_TYPE)) continue;
                int id = getId(state);
                var slabState = state.getValue(BlockStateProperties.SLAB_TYPE);
                switch (slabState) {
                    case TOP -> topSlabs.add(id);
                    case BOTTOM -> bottomSlabs.add(id);
                    case DOUBLE -> doubleSlabs.add(id);
                }
            }
        });

        try (Writer out = new FileWriter(DataGenerator.outputFile("slabBlockStateIds.json"))) {
            DataGenerator.gson.toJson(new SlabData(topSlabs, bottomSlabs, doubleSlabs), out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Dumped slabBlockStateIds.json");
    }

    record SlabData(IntArrayList topSlabs, IntArrayList bottomSlabs, IntArrayList doubleSlabs) {}
}
