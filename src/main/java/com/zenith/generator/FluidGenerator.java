package com.zenith.generator;

import com.zenith.DataGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.WaterFluid;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static com.zenith.DataGenerator.LOG;

public class FluidGenerator implements Generator {
    @Override
    public void generate() {
        var result = new Int2ObjectLinkedOpenHashMap<FluidState>();

        var blockRegistry = BuiltInRegistries.BLOCK;
        blockRegistry.forEach(block -> {
            List<BlockState> blockStates = block.getStateDefinition().getPossibleStates();
            for (var state : blockStates) {
                var fluidState = state.getFluidState();
                if (fluidState.isEmpty()) continue;
                int blockStateId = Block.getId(state);
                boolean water = fluidState.getType() instanceof WaterFluid;
                boolean source = fluidState.isSource();
                int amount = fluidState.getAmount();
                boolean falling = false;
                if (fluidState.getType() instanceof FlowingFluid) {
                    falling = fluidState.getValue(FlowingFluid.FALLING);
                }
                result.put(blockStateId, new FluidState(water, source, amount, falling));
            }
        });

        try (Writer out = new FileWriter(DataGenerator.outputFile("fluidStates.json"))) {
            DataGenerator.gson.toJson(result, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Dumped fluidStates.json");
    }

    record FluidState(boolean water, boolean source, int amount, boolean falling) { }
}
