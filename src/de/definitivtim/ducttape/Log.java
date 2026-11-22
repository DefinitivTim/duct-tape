// © 2026 DefinitivTim
package de.definitivtim.ducttape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Logs through the game logger so lines always end up in latest.log. */
public final class Log {
    private static final Logger LOGGER = LoggerFactory.getLogger("DuctTape");

    private Log() {}

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void warn(String msg, Throwable t) {
        LOGGER.warn(msg, t);
    }
}
