package mods.thecomputerizer.dimhoppertweaks.mixin.vanilla;

import mods.thecomputerizer.dimhoppertweaks.core.Constants;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.TupleIntJsonSerializable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;

@Mixin(StatisticsManager.class)
public class MixinStatisticManager {

    @Final
    @Shadow
    protected Map<StatBase, TupleIntJsonSerializable> statsData;

    @Inject(at = @At(value = "HEAD"), method = "readStat", cancellable = true)
    private void dimhoppertweaks_readStat(StatBase stat, CallbackInfoReturnable<Integer> cir) {
        try {
            TupleIntJsonSerializable tupleintjsonserializable = statsData.get(stat);
            cir.setReturnValue(tupleintjsonserializable == null ? 0 : tupleintjsonserializable.getIntegerValue());
        } catch (Exception e) {
            if(Objects.nonNull(stat))
                Constants.LOGGER.error("Stat with ID {} errored",stat.statId,e);
            else Constants.LOGGER.error("Stat was null and could not sync",e);
            cir.setReturnValue(0);
        }
    }
}
