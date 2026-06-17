package com.zenith.generator.impl;

import com.mojang.authlib.GameProfile;
import com.zenith.DataGenerator;
import com.zenith.generator.JsonRegistryGenerator;
import com.zenith.mc.entity.EntityAttachment;
import com.zenith.mc.entity.EntityData;
import com.zenith.mc.entity.EntityRegistrySpec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityRegistryGenerator extends JsonRegistryGenerator<EntityData> {
    public EntityRegistryGenerator() {
        super(EntityData.class, "EntityRegistry", EntityRegistrySpec.class, "entities.smile");
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
            EntityAttachment attachment = null;
            if (instance instanceof VehicleEntity || instance instanceof Player) {
                var attachments = instance.getAttachments();
                var passengerAttachment = attachments.attachments.get(net.minecraft.world.entity.EntityAttachment.PASSENGER);
                if (passengerAttachment.size() != 1) throw new RuntimeException("Passenger attachment too many positions");
                if (passengerAttachment.getFirst().x() != 0 || passengerAttachment.getFirst().z() != 0) throw new RuntimeException("non-zero xz passenger attachment");
                var vehicleAttachment = attachments.attachments.get(net.minecraft.world.entity.EntityAttachment.VEHICLE);
                if (vehicleAttachment.size() != 1) throw new RuntimeException("Vehicle attachment too many positions");
                if (vehicleAttachment.getFirst().x() != 0 || vehicleAttachment.getFirst().z() != 0) throw new RuntimeException("non-zero xz vehicle attachment");
                attachment = new EntityAttachment(passengerAttachment.getFirst().y(), vehicleAttachment.getFirst().y());
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
                org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType.valueOf(registryKey.getPath().toUpperCase()),
                attachment
            ));
        });

        return entities;
    }

    private Entity createEntity(EntityType<?> type) {
        if (type == EntityTypes.PLAYER) {
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
}
