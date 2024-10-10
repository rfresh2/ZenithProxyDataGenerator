package com.zenith.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockBehaviour.class)
public interface AccessorBlockBehavior {
    @Invoker
    float invokeGetMaxHorizontalOffset();

    @Invoker
    float invokeGetMaxVerticalOffset();
}
