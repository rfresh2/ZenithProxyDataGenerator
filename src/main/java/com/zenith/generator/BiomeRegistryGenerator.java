package com.zenith.generator;

import com.palantir.javapoet.CodeBlock;
import com.zenith.DataGenerator;
import com.zenith.mc.biome.Biome;
import net.minecraft.core.registries.Registries;

import java.util.ArrayList;
import java.util.List;

public class BiomeRegistryGenerator extends DynamicRegistryGenerator<Biome> {
    public BiomeRegistryGenerator() {
        super(Biome.class, Biome.class.getPackage().getName(), "BiomeRegistry");
    }

    @Override
    public List<Biome> buildDataList() {
        List<Biome> result = new ArrayList<>();
        var registry = DataGenerator.SERVER_INSTANCE.registryAccess()
            .lookupOrThrow(Registries.BIOME);
        var holderIdMap = registry.asHolderIdMap();
        for (int id = 0; id < holderIdMap.size(); id++) {
            var holder = holderIdMap.byId(id);
            var biome = holder.value();
            result.add(new Biome(
                id,
                holder.unwrapKey().get().location().getPath()
            ));
        }
        return result;
    }

    @Override
    public CodeBlock dataInitializer(final Biome data) {
        return CodeBlock.of("new $T($L, $S)", this.dataType, data.id(), data.name());
    }
}
