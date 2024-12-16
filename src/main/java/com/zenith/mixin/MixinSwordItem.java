package com.zenith.mixin;

import com.zenith.extension.IItemProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ToolMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwordItem.class)
public class MixinSwordItem {

    @Inject(method = "<init>", at = @At("RETURN"))
    public void captureToolMaterial(final ToolMaterial toolMaterial, final float f, final float g, final Item.Properties properties, final CallbackInfo ci) {
        ((IItemProperties) this).setToolMaterial(toolMaterial);
    }
}
