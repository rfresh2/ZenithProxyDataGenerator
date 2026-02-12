package com.zenith.generator.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.zenith.DataGenerator;
import com.zenith.extension.IItemProperties;
import com.zenith.generator.JsonRegistryGenerator;
import com.zenith.mc.item.*;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.*;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponent;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

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
            items.add(new ItemData(
                registry.getId(item),
                registry.getKey(item).getPath(),
                extractMcplComponents(item.getDefaultInstance().getComponents()),
                toolTag)
            );
        });
        return items;
    }

    @Override
    public void dumpJson(List<ItemData> dataList) {
        var jacksonModule = new SimpleModule();
        jacksonModule.addSerializer(DataComponents.class, new DataComponentsSerializer());
        var mapper = JsonMapper.builder()
            .addModule(jacksonModule)
            .build();
        try (Writer out = new FileWriter(DataGenerator.outputFile(jsonFileName))) {
            mapper.writer().writeValue(out, dataList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("Dumped {}", jsonFileName);
    }

    DataComponents extractMcplComponents(DataComponentMap components) {
        var mcplComponents = new DataComponents(new HashMap<>());
        for (TypedDataComponent component : components) {
            var type = component.type();
            var componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getId(type);
            var buf = ByteBufAllocator.DEFAULT.buffer();
            var rbuf = new RegistryFriendlyByteBuf(buf, DataGenerator.SERVER_INSTANCE.registryAccess());
            component.type().streamCodec().encode(rbuf, component.value());
            DataComponentType mcplType = DataComponentTypes.from(componentId);
            DataComponent mcplComponent = mcplType.readDataComponent(buf);

            mcplComponents.getDataComponents().put(mcplType, mcplComponent);
            buf.release();
        }
        return mcplComponents;
    }

    static class DataComponentsSerializer extends StdSerializer<DataComponents> {

        protected DataComponentsSerializer() {
            super(DataComponents.class);
        }

        @Override
        public void serialize(final DataComponents components, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
            Int2ObjectArrayMap<String> serializedComponents = new Int2ObjectArrayMap<>();
            for (var entry : components.getDataComponents().entrySet()) {
                DataComponentType type = entry.getKey();
                var componentId = type.getId();
                var buf = ByteBufAllocator.DEFAULT.buffer();
                type.writeDataComponent(buf, entry.getValue().getValue());

                var bytes = new byte[buf.readableBytes()];
                buf.markReaderIndex();
                buf.readBytes(bytes);
                var encoder = Base64.getEncoder();
                var base64String = encoder.encodeToString(bytes);
                serializedComponents.put(componentId,  base64String);
                buf.release();
            }
            jsonGenerator.writeObject(serializedComponents);
        }
    }
}
