package com.zenith.generator;

import com.mojang.authlib.GameProfile;
import com.palantir.javapoet.CodeBlock;
import com.zenith.DataGenerator;
import com.zenith.mc.entity.EntityData;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            Entity instance = createEntity(entity);
            if (instance == null) {
                throw new RuntimeException("Failed to create entity instance: " + entity);
            }
            entities.add(new EntityData(
                entityTypeRegistry.getId(entity),
                registryKey.getPath(),
                entity.getDimensions().width(),
                entity.getDimensions().height(),
                instance.isAttackable(),
                instance.isPickable(), // TODO: there is much more logic in the entity class hierarchy about when this is true
                instance instanceof LivingEntity,
                instance instanceof AgeableMob,
                instance.blocksBuilding,
                org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType.valueOf(
                    registryKey.getPath().toUpperCase())
            ));
        });

        return entities;
    }

    private Entity createEntity(EntityType<?> type) {
        if (type == EntityType.PLAYER) {
            CommonListenerCookie commonListenerCookie = CommonListenerCookie.createInitial(new GameProfile(
                UUID.randomUUID(), "test-mock-player"), false);
            return new ServerPlayer(
                DataGenerator.SERVER_INSTANCE, DataGenerator.SERVER_INSTANCE.overworld(), commonListenerCookie.gameProfile(), commonListenerCookie.clientInformation()
            ) {
                @Override
                public boolean isSpectator() {
                    return false;
                }

                @Override
                public boolean isCreative() {
                    return true;
                }
            };
        }
        return type.create(DataGenerator.SERVER_INSTANCE.overworld(), EntitySpawnReason.MOB_SUMMONED);
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
