package com.zenith.mixin;

import com.zenith.extension.IItemProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public class MixinItem implements IItemProperties {
    @Unique private ToolMaterial toolMaterial;

    @Override
    public @Nullable ToolMaterial getToolMaterial() {
        return this.toolMaterial;
    }

    @Override
    public void setToolMaterial(final ToolMaterial toolMaterial) {
        this.toolMaterial = toolMaterial;
    }
}
