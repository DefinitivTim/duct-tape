// © 2026 DefinitivTim
package de.definitivtim.ducttape.mixin;

import de.definitivtim.ducttape.sky.SkyFixState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.devce.rocketnautics.client.SkyHandler", remap = false)
public abstract class SkyHandlerMixin {

    @Inject(method = "ensureStarPlasmaTexture", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dt$skipPlasmaRebuild(CallbackInfo ci) {
        if (SkyFixState.shouldSkip()) {
            ci.cancel();
        }
    }

    // Called every frame from DeepSpaceHandler.renderUniverse without checking enableCustomSky.
    // At night the star brightness is above zero, so the whole star field gets drawn.
    @Inject(method = "renderSpaceStars", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dt$skipSpaceStars(CallbackInfo ci) {
        if (SkyFixState.shouldSkipSkyDraw()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCosmicNebula", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void dt$skipNebula(CallbackInfo ci) {
        if (SkyFixState.shouldSkipSkyDraw()) {
            ci.cancel();
        }
    }
}
