package com.zenith.extension;

import com.zenith.mc.item.ToolType;
import net.minecraft.world.item.ToolMaterial;
import org.jetbrains.annotations.Nullable;

public interface IItemProperties {
    @Nullable ToolMaterial getToolMaterial();
    void setToolMaterial(ToolMaterial toolMaterial);

    @Nullable ToolType getToolType();
    void setToolType(ToolType toolType);
}
