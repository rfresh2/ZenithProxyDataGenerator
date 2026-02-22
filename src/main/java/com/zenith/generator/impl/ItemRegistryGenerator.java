package com.zenith.generator.impl;

import com.zenith.DataGenerator;
import com.zenith.extension.IItemProperties;
import com.zenith.generator.JsonRegistryGenerator;
import com.zenith.mc.item.*;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import lombok.SneakyThrows;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponent;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.smile.SmileMapper;

import java.util.*;

import static com.zenith.DataGenerator.LOG;

public class ItemRegistryGenerator extends JsonRegistryGenerator<ItemData> {
    private final IdentityHashMap<DataComponents, Int2ObjectArrayMap<byte[]>> encodedComponentBytesByDataComponents = new IdentityHashMap<>();

    public ItemRegistryGenerator() {
        super(ItemData.class, "ItemRegistry", ItemRegistrySpec.class, "items.smile");
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
        encodedComponentBytesByDataComponents.clear();
        final List<ItemData> items = new ArrayList<>();
        DefaultedRegistry<Item> registry = BuiltInRegistries.ITEM;

        registry.stream().forEach(item -> {
            ToolType toolType = ((IItemProperties) item).getToolType();
            ToolMaterial material = ((IItemProperties) item).getToolMaterial();
            ToolTier toolTier = Optional.ofNullable(material).map(tierMap::get).orElse(null);
            ToolTag toolTag = null;
            if (toolType != null && toolTier != null) {
                toolTag = new ToolTag(toolTier, toolType);
            }
            items.add(new ItemData(
                registry.getId(item),
                registry.getKey(item).getPath(),
                extractSerializedComponents(item.getDefaultInstance().getComponents()),
                getZenithItemTags(item),
                toolTag)
            );
        });
        return items;
    }

    @SneakyThrows
    private EnumSet<ItemTags> getZenithItemTags(final Item item) {
        var set = EnumSet.noneOf(ItemTags.class);

        var itemRegRef = BuiltInRegistries.ITEM.getOrThrow(item.builtInRegistryHolder().key());
        var tagsOnItem = itemRegRef.tags()
            .filter(tag -> tag.location().getNamespace().equals("minecraft"))
            .toList();
        var tagFields = Arrays.stream(net.minecraft.tags.ItemTags.class.getDeclaredFields())
            .filter(field -> field.getType().equals(TagKey.class))
            .toList();
        for (var tag : tagsOnItem) {
            for (var field : tagFields) {
                var fieldValue = (TagKey<Item>) field.get(null);
                if (fieldValue.location().equals(tag.location())) {
                    set.add(ItemTags.valueOf(field.getName()));
                    break;
                }
            }
        }
        if (set.size() != tagsOnItem.size()) {
            throw new RuntimeException("Failed to find all tags for item " + item.builtInRegistryHolder().key().identifier());
        }

        if (set.isEmpty()) return EnumSet.noneOf(ItemTags.class);
        return set;
    }

    @Override
    public void dumpJson(List<ItemData> dataList) {
        var jacksonModule = new SimpleModule();
        jacksonModule.addSerializer(DataComponents.class, new DataComponentsSerializer(encodedComponentBytesByDataComponents));
        var mapper = SmileMapper.builder()
            .addModule(jacksonModule)
            .build();
        try {
            mapper.writer().writeValue(DataGenerator.outputFile(jsonFileName), dataList);
        } finally {
            encodedComponentBytesByDataComponents.clear();
        }
        LOG.info("Dumped {}", jsonFileName);
    }

    DataComponents extractSerializedComponents(DataComponentMap components) {
        var encodedComponents = new Int2ObjectArrayMap<byte[]>();
        for (TypedDataComponent component : components) {
            var type = component.type();
            var componentId = BuiltInRegistries.DATA_COMPONENT_TYPE.getId(type);
            var buf = ByteBufAllocator.DEFAULT.buffer();
            try {
                var rbuf = new RegistryFriendlyByteBuf(buf, DataGenerator.SERVER_INSTANCE.registryAccess());
                component.type().streamCodec().encode(rbuf, component.value());
                var bytes = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), bytes);
                encodedComponents.put(componentId, bytes);
            } finally {
                buf.release();
            }
        }
        var placeholder = new DataComponents(new HashMap<>());
        encodedComponentBytesByDataComponents.put(placeholder, encodedComponents);
        return placeholder;
    }

    static class DataComponentsSerializer extends StdSerializer<DataComponents> {
        private final IdentityHashMap<DataComponents, Int2ObjectArrayMap<byte[]>> encodedComponentBytesByDataComponents;

        protected DataComponentsSerializer(final IdentityHashMap<DataComponents, Int2ObjectArrayMap<byte[]>> encodedComponentBytesByDataComponents) {
            super(DataComponents.class);
            this.encodedComponentBytesByDataComponents = encodedComponentBytesByDataComponents;
        }

        @Override
        public void serialize(final DataComponents components, final JsonGenerator jsonGenerator, final SerializationContext provider) throws JacksonException {
            var preEncoded = encodedComponentBytesByDataComponents.get(components);
            if (preEncoded != null) {
                jsonGenerator.writePOJO(preEncoded);
                return;
            }

            Int2ObjectArrayMap<byte[]> serializedComponents = new Int2ObjectArrayMap<>();
            for (var entry : components.getDataComponents().entrySet()) {
                DataComponentType type = entry.getKey();
                var componentId = type.getId();
                var buf = ByteBufAllocator.DEFAULT.buffer();
                type.writeDataComponent(buf, entry.getValue().getValue());

                var bytes = new byte[buf.readableBytes()];
                buf.markReaderIndex();
                buf.readBytes(bytes);
                serializedComponents.put(componentId, bytes);
                buf.release();
            }
            jsonGenerator.writePOJO(serializedComponents);
        }
    }
}
