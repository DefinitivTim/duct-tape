// © 2026 DefinitivTim
package de.definitivtim.ducttape.sky;

import de.definitivtim.ducttape.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnosis only, changes nothing. 90 seconds after start (client only) it logs whether Mixin knows the
 * definitivtim_duct_tape config, whether the config lists the Cosmonautics SkyHandler as a target, and whether the
 * SkyHandler class actually contains the merged dt$ handler methods.
 */
public final class SkyProbe {
    private static final String SKY_HANDLER = "dev.devce.rocketnautics.client.SkyHandler";

    private SkyProbe() {
    }

    public static void startLater() {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(90_000L);
            } catch (InterruptedException e) {
                return;
            }
            probe();
        }, "ducttape-sky-probe");
        t.setDaemon(true);
        t.start();
    }

    private static void probe() {
        ClassLoader cl = SkyProbe.class.getClassLoader();
        try {
            Class.forName("net.minecraft.client.Minecraft", false, cl);
        } catch (Throwable t) {
            return; // server: nothing to check
        }
        // 1. Does Mixin know our config, and does it list SkyHandler as a target?
        try {
            Class<?> mixins = Class.forName("org.spongepowered.asm.mixin.Mixins");
            Class<?> cfgClass = Class.forName("org.spongepowered.asm.mixin.transformer.Config");
            Class<?> iface = Class.forName("org.spongepowered.asm.mixin.extensibility.IMixinConfig");
            Method getName = cfgClass.getMethod("getName");
            Method getConfig = cfgClass.getMethod("getConfig");
            Method getTargets = iface.getMethod("getTargets");
            Method isRequired = iface.getMethod("isRequired");
            boolean found = false;
            for (Object c : (Iterable<?>) mixins.getMethod("getConfigs").invoke(null)) {
                String name = String.valueOf(getName.invoke(c));
                if (!name.contains("definitivtim")) {
                    continue;
                }
                found = true;
                Object cfg = getConfig.invoke(c);
                String targets = String.valueOf(getTargets.invoke(cfg));
                Log.info("[Duct Tape Diag] Sky probe: Mixin config '" + name + "' is registered, required="
                        + isRequired.invoke(cfg) + ", lists SkyHandler as target=" + targets.contains("SkyHandler")
                        + ", targets=" + targets);
            }
            if (!found) {
                Log.info("[Duct Tape Diag] Sky probe: Mixin does NOT know a config with 'definitivtim' in its name");
            }
        } catch (Throwable t) {
            Log.warn("[Duct Tape Diag] Sky probe: config check failed", t);
        }
        // 2. Is the handler merged into the SkyHandler class?
        try {
            Class<?> sky = Class.forName(SKY_HANDLER, false, cl);
            List<String> merged = new ArrayList<>();
            for (Method m : sky.getDeclaredMethods()) {
                if (m.getName().startsWith("dt$")) {
                    merged.add(m.getName());
                }
            }
            Log.info("[Duct Tape Diag] Sky probe: SkyHandler merged dt$ methods=" + merged + ", class loader=" + sky.getClassLoader());
        } catch (Throwable t) {
            Log.warn("[Duct Tape Diag] Sky probe: SkyHandler check failed", t);
        }
    }
}
