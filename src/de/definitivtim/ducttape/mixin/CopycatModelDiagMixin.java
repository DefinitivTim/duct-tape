// © 2026 DefinitivTim
package de.definitivtim.ducttape.mixin;

import de.definitivtim.ducttape.copycats.CopycatDiag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client only, diagnosis. Looks at the model data the mesher hands to Copycats+. Changes nothing. */
@Mixin(targets = "com.copycatsplus.copycats.foundation.copycat.model.neoforge.CopycatModelNeoForge", remap = false)
public abstract class CopycatModelDiagMixin {
    @Inject(method = "gatherModelData", at = @At("HEAD"), remap = false)
    private void dt$diag(ModelData.Builder builder, BlockAndTintGetter level, BlockPos pos, BlockState state,
                               ModelData data, CallbackInfoReturnable<ModelData.Builder> cir) {
        CopycatDiag.onGather(level, pos, state, data);
    }
}
