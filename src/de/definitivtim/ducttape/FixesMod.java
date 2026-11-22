// © 2026 DefinitivTim
package de.definitivtim.ducttape;

import net.neoforged.fml.common.Mod;

/**
 * DefinitivTim's Duct Tape: small workarounds for third party mods.
 * The mod class only exists so that NeoForge loads the mixin configuration.
 */
@Mod("definitivtim_duct_tape")
public class FixesMod {
    public FixesMod() {
        de.definitivtim.ducttape.Log.info("[Duct Tape] 1.3.6 loaded (Copycats+ material guard and #419 redraw, Cosmonautics sky)");
        de.definitivtim.ducttape.sky.SkyProbe.startLater();
    }
}
