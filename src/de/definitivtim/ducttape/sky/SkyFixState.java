// © 2026 DefinitivTim
package de.definitivtim.ducttape.sky;

import de.definitivtim.ducttape.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Decides whether Cosmonautics may skip parts of its sky rendering that run even though the
 * custom sky option is off (plasma texture rebuild, space stars, nebula).
 * Uses reflection only, so there is no compile-time dependency on Cosmonautics or NeoForge.
 * Every failure falls back to "do not skip", which keeps the original behaviour.
 */
public final class SkyFixState {
    private static final String ROCKET_CONFIG = "dev.devce.rocketnautics.RocketConfig";
    private static final String SKY_HANDLER = "dev.devce.rocketnautics.client.SkyHandler";
    private static final String DEEP_SPACE_HELPER = "dev.devce.rocketnautics.api.orbit.DeepSpaceHelper";

    private static boolean resolved;
    private static boolean usable;
    private static Object customSkyValue;
    private static Method valueGet;
    private static Field plasmaId;
    private static Method isDeepSpace;

    private SkyFixState() {
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> cfg = Class.forName(ROCKET_CONFIG);
            Object client = cfg.getField("CLIENT").get(null);
            customSkyValue = client.getClass().getField("enableCustomSky").get(client);
            valueGet = customSkyValue.getClass().getMethod("get");

            Class<?> sky = Class.forName(SKY_HANDLER);
            plasmaId = sky.getField("STAR_PLASMA_TEXTURE_ID");

            Class<?> helper = Class.forName(DEEP_SPACE_HELPER);
            isDeepSpace = helper.getMethod("isDeepSpace");
            usable = true;
            Log.info("[Duct Tape Diag] Sky: hooked into Cosmonautics (config, SkyHandler, DeepSpaceHelper found)");
        } catch (Throwable t) {
            usable = false;
            Log.warn("[Duct Tape Diag] Sky: could not hook into Cosmonautics, leaving it unchanged", t);
        }
    }

    // Diagnosis (changes nothing): the first call proves that the mixin handler is running, later lines only appear
    // when the decision changes. Maximum 12 lines per session.
    private static int diagLines;
    private static Boolean lastPlasma;
    private static Boolean lastDraw;

    private static void diag(String what, boolean result, String why) {
        if (diagLines >= 12) {
            return;
        }
        diagLines++;
        Log.info("[Duct Tape Diag] Sky: " + what + " -> " + (result ? "SKIP" : "run") + " (" + why + ")");
    }

    private static String state() {
        try {
            return "enableCustomSky=" + valueGet.invoke(customSkyValue) + ", deepSpace=" + isDeepSpace.invoke(null)
                    + ", plasmaTexture=" + (plasmaId.get(null) == null ? "not built yet" : "built");
        } catch (Throwable t) {
            return "state unreadable: " + t;
        }
    }

    /** True if the plasma texture rebuild can be skipped for this tick. */
    public static boolean shouldSkip() {
        resolve();
        boolean result = false;
        String why = "hook not usable";
        if (usable) {
            try {
                // Texture not created yet: let it be built once so nothing ever reads a missing texture.
                if (plasmaId.get(null) == null) {
                    why = "texture not built yet";
                } else {
                    result = customSkyOffOutsideDeepSpace();
                    why = state();
                }
            } catch (Throwable t) {
                why = "exception " + t;
                result = false;
            }
        }
        if (lastPlasma == null || lastPlasma != result) {
            lastPlasma = result;
            diag("ensureStarPlasmaTexture", result, why);
        }
        return result;
    }

    /**
     * True if the Overworld space star and nebula drawing can be skipped for this frame.
     * Cosmonautics draws them every frame at night even with enableCustomSky = false.
     */
    public static boolean shouldSkipSkyDraw() {
        resolve();
        boolean result = false;
        String why = "hook not usable";
        if (usable) {
            try {
                result = customSkyOffOutsideDeepSpace();
                why = state();
            } catch (Throwable t) {
                why = "exception " + t;
                result = false;
            }
        }
        if (lastDraw == null || lastDraw != result) {
            lastDraw = result;
            diag("renderSpaceStars/renderCosmicNebula", result, why);
        }
        return result;
    }

    private static boolean customSkyOffOutsideDeepSpace() throws ReflectiveOperationException {
        // Custom sky enabled: keep the original behaviour.
        if (Boolean.TRUE.equals(valueGet.invoke(customSkyValue))) {
            return false;
        }
        // Deep space renders planets and suns regardless of the option: keep everything there.
        return !Boolean.TRUE.equals(isDeepSpace.invoke(null));
    }
}
