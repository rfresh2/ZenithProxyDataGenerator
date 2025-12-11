package com.zenith.mixin;

import com.zenith.extension.IBlockProperties;
import com.zenith.mc.block.BlockOffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.Properties.class)
public class MixinBlockProperties implements IBlockProperties {
    @Shadow float destroyTime;
    @Shadow boolean requiresCorrectToolForDrops;
    @Shadow boolean replaceable;
    @Shadow float friction;
    @Shadow float speedFactor;
    @Shadow float jumpFactor;
    @Shadow boolean isAir;
    @Unique private BlockOffsetType offsetType = BlockOffsetType.NONE;
    @Override
    public BlockOffsetType dg$getOffsetType() {
        return offsetType;
    }

    @Inject(method = "offsetType", at = @At("HEAD"))
    public void onOffsetTypeSet(final BlockBehaviour.OffsetType type, final CallbackInfoReturnable<BlockBehaviour.Properties> cir) {
        switch (type) {
            case NONE -> offsetType = BlockOffsetType.NONE;
            case XZ -> offsetType = BlockOffsetType.XZ;
            case XYZ -> offsetType = BlockOffsetType.XYZ;
        }
    }

    @Override
    public float dg$getDestroySpeed() {
        return this.destroyTime;
    }

    @Override
    public boolean dg$requiresCorrectToolForDrops() {
        return this.requiresCorrectToolForDrops;
    }

    @Override
    public boolean dg$isReplaceable() {
        return this.replaceable;
    }

    @Override
    public float dg$getFriction() {
        return this.friction;
    }

    @Override
    public float dg$getSpeedFactor() {
        return this.speedFactor;
    }

    @Override
    public float dg$getJumpFactor() {
        return this.jumpFactor;
    }

    @Override
    public boolean dg$isAir() {
        return this.isAir;
    }
}
