package com.zenith.generator.impl;

import com.zenith.DataGenerator;
import com.zenith.generator.Generator;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import net.minecraft.world.level.material.MapColor;

import static com.zenith.DataGenerator.LOG;

public class MapColorIdToColor implements Generator {
    @Override
    public void generate() {
        var map = new Int2IntLinkedOpenHashMap();
        var colors = MapColor.MATERIAL_COLORS;
        for (int i = 0; i < colors.length; i++) {
            var color = colors[i];
            if (color != null) {
                map.put(i, color.col);
            } else {
                map.put(i, 0);
            }
        }
        DataGenerator.writeSmile("mapColorIdToColor.smile", map);
        LOG.info("Dumped mapColorIdToColor.smile");
    }
}
