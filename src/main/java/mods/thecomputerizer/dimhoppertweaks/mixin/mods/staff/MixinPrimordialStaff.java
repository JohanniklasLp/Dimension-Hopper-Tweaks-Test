package mods.thecomputerizer.dimhoppertweaks.mixin.mods.staff;

import net.tslat.aoa3.item.weapon.staff.PrimordialStaff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = PrimordialStaff.class, remap = false)
public class MixinPrimordialStaff {

    /**
     * @author The_Computerizer
     * @reason Increase base staff damage
     */
    @Overwrite
    public float getDmg() {
        return 75f;
    }
}
