// © 2026 DefinitivTim
package de.definitivtim.ducttape.copycats;

import com.copycatsplus.copycats.foundation.copycat.model.neoforge.CopycatModelNeoForge;
import com.copycatsplus.copycats.foundation.copycat.multistate.IMultiStateCopycatBlockEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Diagnosis for copycat blocks that are invisible until a block update. Client only.
 * Logs (with prefix [Duct Tape Diag]) when the mesher asks for a copycat model but the model data holds no
 * material, what the block entity looks like at that moment, and when data arrives later for such a position.
 */
public final class CopycatDiag {
    private static final int MAX_LINES = 60;
    private static final AtomicInteger LINES = new AtomicInteger();
    private static final Map<Long, Long> EMPTY_SINCE = new ConcurrentHashMap<>();

    private CopycatDiag() {}

    /** Called by the mesher thread before Copycats+ builds the model data of a block. */
    public static void onGather(BlockAndTintGetter getter, BlockPos pos, BlockState state, ModelData data) {
        try {
            Map<String, BlockState> materials = CopycatModelNeoForge.getMaterials(data);
            boolean noMaterialProp = data.get(CopycatModelNeoForge.MATERIAL_PROPERTY) == null;
            boolean empty = (materials == null || materials.isEmpty()) && noMaterialProp;
            if (!empty) {
                return;
            }
            long key = pos.asLong();
            if (EMPTY_SINCE.putIfAbsent(key, System.currentTimeMillis()) != null) {
                return; // already reported for this position, waiting for data
            }
            if (EMPTY_SINCE.size() > 4000) {
                EMPTY_SINCE.clear();
            }
            BlockEntity be = getter.getBlockEntity(pos);
            String beInfo;
            if (be == null) {
                beInfo = "no block entity yet";
            } else if (be instanceof IMultiStateCopycatBlockEntity multi) {
                beInfo = "block entity present, storage=" + multi.getMaterialItemStorage().getMaterialMap();
            } else {
                beInfo = "block entity present (" + be.getClass().getSimpleName() + ")";
            }
            log("model data has NO material at " + fmt(pos) + " state=" + state + " thread="
                    + Thread.currentThread().getName() + " | " + beInfo);
        } catch (Throwable t) {
            log("diagnosis failed: " + t);
        }
    }

    /** Called on the client when a copycat block entity read its data. */
    public static void onRead(BlockEntity be) {
        try {
            BlockPos pos = be.getBlockPos();
            Long since = EMPTY_SINCE.remove(pos.asLong());
            if (since != null) {
                log("data arrived at " + fmt(pos) + " " + (System.currentTimeMillis() - since)
                        + " ms after the mesher saw it empty");
            }
        } catch (Throwable ignored) {
        }
    }

    private static String fmt(BlockPos p) {
        return p.getX() + " " + p.getY() + " " + p.getZ();
    }

    private static void log(String msg) {
        int n = LINES.incrementAndGet();
        if (n <= MAX_LINES) {
            de.definitivtim.ducttape.Log.info("[Duct Tape Diag] " + msg + (n == MAX_LINES ? " (limit reached, no more lines)" : ""));
        }
    }
}
