package mods.thecomputerizer.dimhoppertweaks.mixin.mods.infernalmobs;

import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.MM_1UP;
import mods.thecomputerizer.dimhoppertweaks.mixin.DelayedModAccess;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MM_1UP.class, remap = false)
public abstract class MixinMM_1UP extends MobModifier {
    
    @Inject(at = @At("HEAD"), method = "onUpdate", cancellable = true)
    private void dimhoppertweaks$onUpdate(EntityLivingBase mob, CallbackInfoReturnable<Boolean> cir) {
        if(DelayedModAccess.isInfernalDistracted(getMobTarget())) cir.setReturnValue(false);
    }
}