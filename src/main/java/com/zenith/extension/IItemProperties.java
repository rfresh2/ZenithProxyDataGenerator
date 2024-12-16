package com.zenith.extension;

import net.minecraft.world.item.ToolMaterial;
import org.jetbrains.annotations.Nullable;

public interface IItemProperties {
    @Nullable ToolMaterial getToolMaterial();
    void setToolMaterial(ToolMaterial toolMaterial);
}
