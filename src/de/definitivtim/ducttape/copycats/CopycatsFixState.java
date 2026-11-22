// © 2026 DefinitivTim
package de.definitivtim.ducttape.copycats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bookkeeping for the Copycats+ material guard. Only used to leave a few log lines behind,
 * so it is visible in latest.log that the guard caught a missing material entry.
 */
public final class CopycatsFixState {
    private static final int MAX_LOGGED = 5;
    private static final AtomicInteger CAUGHT = new AtomicInteger();

    private CopycatsFixState() {
    }

    /** Called when a copycat block entity had no material entry for its default property. */
    public static void noteMissingMaterial(String property) {
        int n = CAUGHT.incrementAndGet();
        if (n <= MAX_LOGGED) {
            System.err.println("[Duct Tape] Copycats+: no material entry for property '" + property
                    + "', using air instead of crashing (" + n + (n == MAX_LOGGED ? ", further ones are not logged" : "") + ")");
        }
    }
}
