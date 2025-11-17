package com.zenith.generator;

import com.mojang.authlib.GameProfile;
import com.zenith.DataGenerator;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;

import java.io.FileWriter;
import java.io.Writer;
import java.util.UUID;

public class EntityAttachmentsGenerator implements Generator {
    @Override
    public void generate() {
        final Int2ObjectLinkedOpenHashMap<DoubleList> attachmentData = new Int2ObjectLinkedOpenHashMap<>();
        Registry<EntityType<?>> entityTypeRegistry = BuiltInRegistries.ENTITY_TYPE;
        entityTypeRegistry.forEach(entity -> {
            var registryKey = entityTypeRegistry.getKey(entity);
            Entity instance = createEntity(entity);
            if (instance == null && entity != EntityType.PLAYER) { // expected for player type
                throw new RuntimeException("Failed to create entity instance: " + entity);
            }
            if (instance instanceof VehicleEntity || instance instanceof Player) {
                var data = new DoubleArrayList();
                var attachments = instance.getAttachments();
                var passengerAttachment = attachments.attachments.get(EntityAttachment.PASSENGER);
                if (passengerAttachment.size() != 1) throw new RuntimeException("Passenger attachment too many positions");
                if (passengerAttachment.getFirst().x() != 0 || passengerAttachment.getFirst().z() != 0) throw new RuntimeException("non-zero xz passenger attachment");
                data.add(passengerAttachment.getFirst().y());
                var vehicleAttachment = attachments.attachments.get(EntityAttachment.VEHICLE);
                if (vehicleAttachment.size() != 1) throw new RuntimeException("Vehicle attachment too many positions");
                if (vehicleAttachment.getFirst().x() != 0 || vehicleAttachment.getFirst().z() != 0) throw new RuntimeException("non-zero xz vehicle attachment");
                data.add(vehicleAttachment.getFirst().y());
                attachmentData.put(entityTypeRegistry.getId(entity), data);
            }
        });
        try (Writer out = new FileWriter(DataGenerator.outputFile("entityAttachments.json"))) {
            DataGenerator.gson.toJson(attachmentData, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        DataGenerator.LOG.info("Dumped entityAttachments.json");
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
}
