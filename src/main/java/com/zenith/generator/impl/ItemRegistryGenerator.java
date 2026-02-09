package com.zenith.generator.impl;

import com.zenith.DataGenerator;
import com.zenith.extension.IItemProperties;
import com.zenith.generator.JsonRegistryGenerator;
import com.zenith.mc.item.*;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.zenith.DataGenerator.LOG;

public class ItemRegistryGenerator extends JsonRegistryGenerator<ItemData> {
    public ItemRegistryGenerator() {
        super(ItemData.class, "ItemRegistry2", ItemRegistrySpec.class, "items.json");
    }

    private static final Map<ToolMaterial, ToolTier> tierMap = Map.of(
        ToolMaterial.WOOD, ToolTier.WOOD,
        ToolMaterial.STONE, ToolTier.STONE,
        ToolMaterial.IRON, ToolTier.IRON,
        ToolMaterial.DIAMOND, ToolTier.DIAMOND,
        ToolMaterial.GOLD, ToolTier.GOLD,
        ToolMaterial.NETHERITE, ToolTier.NETHERITE
    );

    // build dummy list
    @Override
    public List<ItemData> buildDataList() {
        final List<ItemData> items = new ArrayList<>();
        DefaultedRegistry<Item> registry = BuiltInRegistries.ITEM;
        registry.stream().forEach(item -> {
            items.add(new ItemData(
                registry.getId(item),
                registry.getKey(item).getPath(),
                0,
                null,
                null
            ));
        });
        return items;
    }

    // real list
    public List<SerializedItemData> buildSerializableDataList() {
        final List<SerializedItemData> items = new ArrayList<>();
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
            Int2ObjectArrayMap<String> serializedComponents = new Int2ObjectArrayMap<>();
            var components = item.getDefaultInstance().getComponents();
            for (TypedDataComponent component : components) {
                var type = component.type();
                var componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getId(type);
                var buf = ByteBufAllocator.DEFAULT.buffer();
                var rbuf = new RegistryFriendlyByteBuf(buf, DataGenerator.SERVER_INSTANCE.registryAccess());
                component.type().streamCodec().encode(rbuf, component.value());
                var bytes = new byte[buf.readableBytes()];
                buf.markReaderIndex();
                buf.readBytes(bytes);
                var encoder = Base64.getEncoder();
                var base64String = encoder.encodeToString(bytes);
                serializedComponents.put(componentId,  base64String);
                buf.release();
            }
            items.add(new SerializedItemData(
                registry.getId(item),
                registry.getKey(item).getPath(),
                item.getDefaultMaxStackSize(),
                serializedComponents,
                toolTag)
            );
        });
        return items;
    }

    @Override
    public void dumpJson(List<ItemData> unused) {
        List<SerializedItemData> dataList = buildSerializableDataList();
        try (Writer out = new FileWriter(DataGenerator.outputFile(jsonFileName))) {
            DataGenerator.gson.toJson(dataList, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Dumped {}", jsonFileName);
    }
}
