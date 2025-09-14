package com.zenith.generator;

import com.palantir.javapoet.CodeBlock;
import com.zenith.extension.IItemProperties;
import com.zenith.mc.item.ItemData;
import com.zenith.mc.item.ToolTag;
import com.zenith.mc.item.ToolTier;
import com.zenith.mc.item.ToolType;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemRegistryGenerator extends RegistryGenerator<ItemData> {
    public ItemRegistryGenerator() {
        super(ItemData.class, ItemData.class.getPackage().getName(), "ItemRegistry");
    }

    private static final Map<ToolMaterial, ToolTier> tierMap = Map.of(
        ToolMaterial.WOOD, ToolTier.WOOD,
        ToolMaterial.STONE, ToolTier.STONE,
        ToolMaterial.IRON, ToolTier.IRON,
        ToolMaterial.DIAMOND, ToolTier.DIAMOND,
        ToolMaterial.GOLD, ToolTier.GOLD,
        ToolMaterial.NETHERITE, ToolTier.NETHERITE
    );

    @Override
    public List<ItemData> buildDataList() {
        final List<ItemData> items = new ArrayList<>();
        DefaultedRegistry<Item> registry = BuiltInRegistries.ITEM;

        registry.stream().forEach(item -> {
            ToolType toolType = null;
            ToolTier toolTier = null;
            if (item instanceof AxeItem i) {
                toolType = ToolType.AXE;
                toolTier = tierMap.get(((IItemProperties) i).getToolMaterial());
            } else if (item instanceof HoeItem i) {
                toolType = ToolType.HOE;
                toolTier = tierMap.get(((IItemProperties) i).getToolMaterial());
            } else if (item instanceof PickaxeItem i) {
                toolType = ToolType.PICKAXE;
                toolTier = tierMap.get(((IItemProperties) i).getToolMaterial());
            } else if (item instanceof ShovelItem i) {
                toolType = ToolType.SHOVEL;
                toolTier = tierMap.get(((IItemProperties) i).getToolMaterial());
            } else if (item instanceof SwordItem i) {
                toolType = ToolType.SWORD;
                toolTier = tierMap.get(((IItemProperties) i).getToolMaterial());
            }
            ToolTag toolTag = null;
            if (toolType != null && toolTier != null) {
                toolTag = new ToolTag(toolTier, toolType);
            }
            var maxDamageComponent = item.components().get(DataComponents.MAX_DAMAGE);
            items.add(new ItemData(
                registry.getId(item),
                registry.getKey(item).getPath(),
                item.getDefaultMaxStackSize(),
                toolTag)
            );
        });
        return items;
    }

    @Override
    public CodeBlock dataInitializer(final ItemData item) {
        if (item.toolTag() == null) {
            return CodeBlock.of("new $T($L, $S, $L)",
                                this.dataType,
                                item.id(),
                                item.name(),
                                item.stackSize());
        } else {
            return CodeBlock.of("new $T($L, $S, $L, new $T($T.$L, $T.$L))",
                                this.dataType,
                                item.id(),
                                item.name(),
                                item.stackSize(),
                                ToolTag.class,
                                ToolTier.class,
                                item.toolTag().tier(),
                                ToolType.class,
                                item.toolTag().type());
        }
    }
}
