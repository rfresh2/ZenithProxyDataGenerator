package com.zenith.mixin;

import com.zenith.extension.IBlockProperties;
import com.zenith.mc.block.BlockOffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.Properties.class)
public class MixinBlockProperties implements IBlockProperties {
    @Unique private BlockOffsetType offsetType = BlockOffsetType.NONE;
    @Override
    public BlockOffsetType getOffsetType() {
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
}
