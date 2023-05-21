package mods.thecomputerizer.dimhoppertweaks.common.objects.entity.boss.phase;

import mods.thecomputerizer.dimhoppertweaks.common.objects.entity.boss.EntityFinalBoss;
import mods.thecomputerizer.dimhoppertweaks.common.objects.entity.boss.phase.actions.*;

public class PhaseSeven extends PhaseBase {

    public PhaseSeven(EntityFinalBoss boss) {
        super(boss,7);
    }

    @Override
    protected Action[] orderedActions() {
        MovePlayers move = new MovePlayers(15,true,this.phase);
        DelayedAOE aoe = new DelayedAOE(10,false,this.phase,3);
        Teleport teleport = new Teleport(10,false,this.phase);
        Charge charge = new Charge(50,false,this.phase,15,3d);
        Beam beam = new Beam(160,false,this.phase,80);
        IndiscriminateAOE indiscriminate = new IndiscriminateAOE(160,false,this.phase,26,
                10d,10,7, 2);
        return new Action[]{move,aoe,aoe,teleport,aoe,teleport,charge,charge,charge,teleport,teleport,charge,beam,teleport,teleport,teleport,indiscriminate,teleport};
    }

    @Override
    protected boolean checkPhaseComplete() {
        boolean done = this.boss.boom;
        this.boss.boom = false;
        return done;
    }

    @Override
    protected boolean dropShieldWhenCompleted() {
        return true;
    }
}
