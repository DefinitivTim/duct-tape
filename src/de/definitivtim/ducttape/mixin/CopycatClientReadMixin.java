// © 2026 DefinitivTim
package de.definitivtim.ducttape.mixin;

import de.definitivtim.ducttape.copycats.CopycatDiag;
import de.definitivtim.ducttape.copycats.CopycatRedraw;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client only. Redraws a copycat block entity after it read a data packet (Copycats+ issue #419). */
@Mixin(targets = {
        "com.copycatsplus.copycats.foundation.copycat.CCCopycatBlockEntity",
        "com.copycatsplus.copycats.foundation.copycat.multistate.MultiStateCopycatBlockEntity",
        "com.copycatsplus.copycats.content.copycat.cogwheel.CopycatCogWheelBlockEntity",
        "com.copycatsplus.copycats.content.copycat.fluid_pipe.CopycatStraightPipeBlockEntity",
        "com.copycatsplus.copycats.content.copycat.fluid_pipe.CopycatFluidPipeBlockEntity",
        "com.copycatsplus.copycats.content.copycat.sliding_door.CopycatSlidingDoorBlockEntity",
        "com.copycatsplus.copycats.content.copycat.shaft.CopycatShaftBlockEntity"
}, remap = false)
public abstract class CopycatClientReadMixin {
    @Inject(method = "read", at = @At("RETURN"), remap = false)
    private void dt$afterRead(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        BlockEntity be = (BlockEntity) (Object) this;
        CopycatDiag.onRead(be);
        CopycatRedraw.noteRead(be, clientPacket);
        if (clientPacket) {
            CopycatRedraw.afterClientRead(be);
        }
    }

    @Inject(method = "onLoad", at = @At("RETURN"), remap = false)
    private void dt$afterLoad(CallbackInfo ci) {
        CopycatRedraw.afterLoad((BlockEntity) (Object) this);
    }
}
