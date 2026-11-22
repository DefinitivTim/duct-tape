// © 2026 DefinitivTim
package de.definitivtim.ducttape.mixin;

import com.copycatsplus.copycats.foundation.copycat.multistate.IMultiStateCopycatBlockEntity;
import com.copycatsplus.copycats.foundation.copycat.multistate.MaterialItemStorage;
import de.definitivtim.ducttape.copycats.CopycatsFixState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Copycats+ 3.0.9: IMultiStateCopycatBlockEntity.getMaterial() does
 * getMaterialItem(defaultProperty).material() without a null check. While a copycat block is
 * being changed or removed (for example by quickly breaking several with a wrench) the storage can
 * briefly have no entry for the property of the block that is currently at that position, and
 * Sodium's chunk meshing thread crashes with a NullPointerException.
 *
 * The sibling method IMultiStateCopycatBlock.getMaterial(BlockGetter, BlockPos, String) already
 * returns air for a missing entry. This override does the same. The method is merged into the
 * target class, where it takes precedence over the interface default.
 */
@Mixin(targets = "com.copycatsplus.copycats.content.copycat.cogwheel.CopycatCogWheelBlockEntity", remap = false)
public abstract class CopycatCogWheelBlockEntityMixin implements IMultiStateCopycatBlockEntity {

    @Override
    public BlockState getMaterial() {
        String property = this.getBlock().defaultProperty();
        MaterialItemStorage.MaterialItem item = this.getMaterialItemStorage().getMaterialItem(property);
        if (item == null) {
            CopycatsFixState.noteMissingMaterial(property);
            return Blocks.AIR.defaultBlockState();
        }
        return item.material();
    }
}
