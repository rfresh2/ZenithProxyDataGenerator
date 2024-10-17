package com.zenith.generator;

import com.squareup.javapoet.CodeBlock;
import com.zenith.mc.food.FoodData;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;

import java.util.ArrayList;
import java.util.List;

public class FoodRegistryGenerator extends RegistryGenerator<FoodData> {
    public FoodRegistryGenerator() {
        super(FoodData.class, FoodData.class.getPackage().getName(), "FoodRegistry");
    }

    @Override
    public List<FoodData> buildDataList() {
        List<FoodData> foodList = new ArrayList<>();

        DefaultedRegistry<Item> registry = BuiltInRegistries.ITEM;
        registry.stream()
            .filter(item -> item.components().has(DataComponents.FOOD))
            .forEach(food -> {
                FoodProperties foodComponent = food.components().get(DataComponents.FOOD);
                Consumable consumableComponent = food.components().get(DataComponents.CONSUMABLE);
                int foodPoints = foodComponent.nutrition();
                float saturationRatio = foodComponent.saturation() * 2.0F;
                float saturation = foodPoints * saturationRatio;
                boolean isSafeFood = true;
                if (consumableComponent != null) {
                    List<ConsumeEffect> effects = consumableComponent.onConsumeEffects();
                    for (var effect : effects) {
                        if (effect instanceof ApplyStatusEffectsConsumeEffect applyEffect) {
                            for (var effectInstance : applyEffect.effects()) {
                                if (effectInstance.getEffect() == MobEffects.POISON || effectInstance.getEffect() == MobEffects.HUNGER) {
                                    isSafeFood = false;
                                    break;
                                }
                            }
                        } else if (effect instanceof TeleportRandomlyConsumeEffect tpEffect) {
                            isSafeFood = false;
                            break;
                        }
                    }
                }
                var data = new FoodData(
                    registry.getId(food),
                    registry.getKey(food).getPath(),
                    food.getDefaultMaxStackSize(),
                    foodPoints,
                    saturation,
                    isSafeFood
                );
                foodList.add(data);
            });
        return foodList;
    }

    @Override
    public CodeBlock dataInitializer(final FoodData data) {
        return CodeBlock.of(
            "new $T($L, $S, $L, $Lf, $Lf, $L)",
            FoodData.class,
            data.id(),
            data.name(),
            data.stackSize(),
            data.foodPoints(),
            data.saturation(),
            data.isSafeFood()
        );
    }
}
