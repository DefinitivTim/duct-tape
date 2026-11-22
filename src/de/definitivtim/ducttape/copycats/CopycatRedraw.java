// © 2026 DefinitivTim
package de.definitivtim.ducttape.copycats;

import com.copycatsplus.copycats.utility.BlockEntityUtils;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client only. Fix for Copycats+ issue #419 (https://github.com/copycats-plus/copycats/issues/419):
 * a copycat without a material stays invisible after it was moved by a contraption (or placed/disassembled),
 * until a block update or chunk reload. The chunk is meshed before the block entity data is there; when the data
 * arrives, Copycats+ only redraws if the material changed. An unskinned copycat reads back the same default
 * material, so it never redraws. This redraws after every data packet read on the client.
 */
public final class CopycatRedraw {
    private static int logged;
    private static int readsLogged;
    private static int loadsLogged;

    private CopycatRedraw() {}

    /** Diagnosis: shows through which path copycat block entities get their data. */
    public static void noteRead(BlockEntity be, boolean clientPacket) {
        if (readsLogged < 10 && (clientPacket || be.getLevel() != null)) {
            readsLogged++;
            de.definitivtim.ducttape.Log.info("[Duct Tape] copycat read: " + be.getClass().getSimpleName()
                    + " clientPacket=" + clientPacket + " level=" + (be.getLevel() == null ? "null" : (be.getLevel().isClientSide() ? "client" : "server")));
        }
    }

    /** Called when a copycat block entity was added to a level (chunk load, contraption placement). */
    public static void afterLoad(BlockEntity be) {
        if (be.getLevel() == null || !be.getLevel().isClientSide() || be.isRemoved()) {
            return;
        }
        try {
            BlockEntityUtils.redraw(be);
            if (++loadsLogged <= 10) {
                de.definitivtim.ducttape.Log.info("[Duct Tape] Copycats+ redraw on load " + be.getBlockPos().getX() + " "
                        + be.getBlockPos().getY() + " " + be.getBlockPos().getZ() + " " + be.getClass().getSimpleName());
            }
        } catch (Throwable t) {
            // never break loading
        }
    }

    public static void afterClientRead(BlockEntity be) {
        if (be.getLevel() == null || !be.getLevel().isClientSide() || be.isRemoved()) {
            return;
        }
        try {
            BlockEntityUtils.redraw(be);
            if (++logged <= 10) {
                de.definitivtim.ducttape.Log.info("[Duct Tape] Copycats+ #419 fix: redraw after data packet" + " " + be.getBlockPos().getX() + " " + be.getBlockPos().getY() + " " + be.getBlockPos().getZ() + " " + be.getClass().getSimpleName() + (logged == 10 ? " (further ones are not logged)" : ""));
            }
        } catch (Throwable t) {
            // never break packet handling
        }
    }
}
