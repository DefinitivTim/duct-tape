package com.copycatsplus.copycats.foundation.copycat.multistate;
import net.minecraft.world.level.block.state.BlockState;
public interface IMultiStateCopycatBlockEntity {
    MaterialItemStorage getMaterialItemStorage();
    default IMultiStateCopycatBlock getBlock() { return null; }
    default BlockState getMaterial() { return null; }
}
