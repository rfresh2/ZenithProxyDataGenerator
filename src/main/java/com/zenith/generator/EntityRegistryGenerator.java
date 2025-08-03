package com.zenith.generator;

import com.palantir.javapoet.CodeBlock;
import com.zenith.DataGenerator;
import com.zenith.mc.entity.EntityData;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;

import java.util.ArrayList;
import java.util.List;

public class EntityRegistryGenerator extends RegistryGenerator<EntityData> {
    public EntityRegistryGenerator() {
        super(EntityData.class, EntityData.class.getPackage().getName(), "EntityRegistry");
    }

    @Override
    public List<EntityData> buildDataList() {
        List<EntityData> entities = new ArrayList<>();
        Registry<EntityType<?>> entityTypeRegistry = BuiltInRegistries.ENTITY_TYPE;
        entityTypeRegistry.forEach(entity -> {
            var registryKey = entityTypeRegistry.getKey(entity);
            Entity instance = entity.create(DataGenerator.SERVER_INSTANCE.overworld(), EntitySpawnReason.MOB_SUMMONED);
            if (instance == null && entity != EntityType.PLAYER) { // expected for player type
                throw new RuntimeException("Failed to create entity instance: " + entity);
            }
            entities.add(new EntityData(
                entityTypeRegistry.getId(entity),
                registryKey.getPath(),
                entity.getDimensions().width(),
                entity.getDimensions().height(),
                instance == null ? true : instance.isAttackable(),
                instance == null ? true : instance.isPickable(), // TODO: there is much more logic in the entity class hierarchy about when this is true
                instance == null ? true : instance instanceof LivingEntity,
                instance == null ? true : instance instanceof AgeableMob,
                instance == null ? true : instance.blocksBuilding,
                org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType.valueOf(
                    registryKey.getPath().toUpperCase())
            ));
        });


        return entities;
    }

    @Override
    public CodeBlock dataInitializer(final EntityData data) {
        return CodeBlock.of("new $T($L, $S, $Lf, $Lf, $L, $L, $L, $L, $L, $T.$L)",
                            EntityData.class,
                            data.id(),
                            data.name(),
                            data.width(),
                            data.height(),
                            data.attackable(),
                            data.pickable(),
                            data.livingEntity(),
                            data.ageableMob(),
                            data.blocksBuilding(),
                            org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType.class,
                            data.mcplType()
        );
    }
}
