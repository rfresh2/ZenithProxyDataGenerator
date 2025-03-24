package com.zenith.mixin;

import com.zenith.extension.IItemProperties;
import com.zenith.mc.item.ToolType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class MixinItem implements IItemProperties {
    @Unique
    private ToolMaterial toolMaterial;
    @Unique
    private ToolType toolType;

    @Override
    public @Nullable ToolMaterial getToolMaterial() {
        return this.toolMaterial;
    }

    @Override
    public void setToolMaterial(final ToolMaterial toolMaterial) {
        this.toolMaterial = toolMaterial;
    }

    @Override
    public @Nullable ToolType getToolType() {
        return this.toolType;
    }

    @Override
    public void setToolType(final ToolType toolType) {
        this.toolType = toolType;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void captureToolMaterial(final Item.Properties properties, final CallbackInfo ci) {
        var material = ((IItemProperties) properties).getToolMaterial();
        setToolMaterial(material);
        var type = ((IItemProperties) properties).getToolType();
        setToolType(type);
    }
}
