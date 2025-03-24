package com.zenith.mixin;

import com.zenith.extension.IItemProperties;
import com.zenith.mc.item.ToolType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.Properties.class)
public class MixinItemProperties implements IItemProperties {
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

    @Inject(method = "tool", at = @At("HEAD"))
    public void captureToolMaterial(final ToolMaterial toolMaterial, final TagKey<Block> tagKey, final float f, final float g, final float h, final CallbackInfoReturnable<Item.Properties> cir) {
        setToolMaterial(toolMaterial);
    }

    @Inject(method = "pickaxe", at = @At("HEAD"))
    public void capturePickaxeType(final ToolMaterial toolMaterial, final float f, final float g, final CallbackInfoReturnable<Item.Properties> cir) {
        setToolType(ToolType.PICKAXE);
    }

    @Inject(method = "axe", at = @At("HEAD"))
    public void captureAxeType(final ToolMaterial toolMaterial, final float f, final float g, final CallbackInfoReturnable<Item.Properties> cir) {
        setToolType(ToolType.AXE);
    }

    @Inject(method = "hoe", at = @At("HEAD"))
    public void captureHoeType(final ToolMaterial toolMaterial, final float f, final float g, final CallbackInfoReturnable<Item.Properties> cir) {
        setToolType(ToolType.HOE);
    }

    @Inject(method = "shovel", at = @At("HEAD"))
    public void captureShovelType(final ToolMaterial toolMaterial, final float f, final float g, final CallbackInfoReturnable<Item.Properties> cir) {
        setToolType(ToolType.SHOVEL);
    }

    @Inject(method = "sword", at = @At("HEAD"))
    public void captureSwordMaterialAndType(final ToolMaterial toolMaterial, final float f, final float g, final CallbackInfoReturnable<Item.Properties> cir) {
        setToolMaterial(toolMaterial);
        setToolType(ToolType.SWORD);
    }
}
