package com.zenith.extension;

import com.zenith.mc.block.BlockOffsetType;

public interface IBlockProperties {
    BlockOffsetType dg$getOffsetType();

    float dg$getDestroySpeed();

    boolean dg$requiresCorrectToolForDrops();
}
