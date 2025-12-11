package com.zenith.extension;

import com.zenith.mc.block.BlockOffsetType;

public interface IBlockProperties {
    BlockOffsetType dg$getOffsetType();
    float dg$getDestroySpeed();
    boolean dg$requiresCorrectToolForDrops();
    boolean dg$isReplaceable();
    float dg$getFriction();
    float dg$getSpeedFactor();
    float dg$getJumpFactor();
    boolean dg$isAir();
}
