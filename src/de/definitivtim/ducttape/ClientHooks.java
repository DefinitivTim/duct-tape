// © 2026 DefinitivTim
package de.definitivtim.ducttape;

import de.definitivtim.ducttape.copycats.CopycatRedraw;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Only loaded on the client. */
final class ClientHooks {
    private ClientHooks() {}

    static void register() {
        NeoForge.EVENT_BUS.addListener(ClientHooks::onTick);
    }

    private static void onTick(ClientTickEvent.Post event) {
        CopycatRedraw.tick();
    }
}
