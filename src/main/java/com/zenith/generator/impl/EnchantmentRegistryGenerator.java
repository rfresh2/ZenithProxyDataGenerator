package com.zenith.generator.impl;

import com.palantir.javapoet.CodeBlock;
import com.zenith.DataGenerator;
import com.zenith.generator.DynamicRegistryGenerator;
import com.zenith.mc.enchantment.EnchantmentData;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentRegistryGenerator extends DynamicRegistryGenerator<EnchantmentData> {

    public EnchantmentRegistryGenerator() {
        super(EnchantmentData.class, EnchantmentData.class.getPackage().getName(), "EnchantmentRegistry");
    }

    @Override
    public List<EnchantmentData> buildDataList() {
        final List<EnchantmentData> enchants = new ArrayList<>();
        Registry<Enchantment> registry = DataGenerator.SERVER_INSTANCE.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
        registry.stream().forEach(enchant ->
            enchants.add(new EnchantmentData(
                registry.getId(enchant),
                registry.getKey(enchant).getPath(),
                enchant.getMaxLevel()
            )));
        return enchants;
    }

    @Override
    public CodeBlock dataInitializer(final EnchantmentData enchant) {
        return CodeBlock.of("new $T($L, $S, $L)",
            this.dataType,
            enchant.id(),
            enchant.name(),
            enchant.maxLevel()
        );
    }
}
