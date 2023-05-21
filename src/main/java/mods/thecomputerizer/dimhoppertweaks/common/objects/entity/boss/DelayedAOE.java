package mods.thecomputerizer.dimhoppertweaks.common.objects.entity.boss;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

public class DelayedAOE {

    private final List<Vec3d> vecList;
    private final MutableInt timer;
    private final int range;
    private final int phaseFired;

    public DelayedAOE(List<Vec3d> vecList, int time, int range, int phase) {
        this.vecList = vecList;
        this.timer = new MutableInt(time);
        this.range = range;
        this.phaseFired = phase;
    }

    public boolean tick(EntityFinalBoss boss) {
        boolean ret = this.timer.decrementAndGet()<=0;
        if(ret && boss.phase==this.phaseFired)
            for(Vec3d posVec : this.vecList)
                boss.aoeAttack(posVec,this.range);
        return ret;
    }
}
