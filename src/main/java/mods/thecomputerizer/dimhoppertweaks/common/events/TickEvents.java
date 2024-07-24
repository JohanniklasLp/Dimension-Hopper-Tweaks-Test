package mods.thecomputerizer.dimhoppertweaks.common.events;

import mods.thecomputerizer.dimhoppertweaks.common.capability.player.ISkillCapability;
import mods.thecomputerizer.dimhoppertweaks.common.capability.player.SkillWrapper;
import mods.thecomputerizer.dimhoppertweaks.common.commands.DHDebugCommands;
import mods.thecomputerizer.dimhoppertweaks.mixin.api.IItemTimeInABottle;
import mods.thecomputerizer.dimhoppertweaks.util.ItemUtil;
import mods.thecomputerizer.dimhoppertweaks.util.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.*;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Objects;

import static bedrockcraft.ModItems.blackTotem;
import static bedrockcraft.ModWorlds.VOID_WORLD;
import static lumien.randomthings.item.ModItems.timeInABottle;
import static mods.thecomputerizer.dimhoppertweaks.core.DHTRef.MODID;
import static net.minecraft.init.MobEffects.SPEED;
import static net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST;
import static net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END;
import static net.minecraftforge.fml.relauncher.Side.SERVER;
import static zollerngalaxy.core.dimensions.ZGDimensions.CALIGRO;

@EventBusSubscriber(modid = MODID)
public class TickEvents {

    private static final MutableInt TICK_DELAY = new MutableInt();

    @SubscribeEvent(priority = LOWEST)
    public static void playerTick(PlayerTickEvent event) {
        if(event.isCanceled()) return;
        if(event.phase==END) {
            if(event.side==SERVER) {
                if(TICK_DELAY.getAndAdd(1)>=20) {
                    EntityPlayerMP player = (EntityPlayerMP) event.player;
                    if(player.isSprinting()) {
                        int speedFactor = player.isPotionActive(SPEED) ? Objects.requireNonNull(
                                player.getActivePotionEffect(SPEED)).getAmplifier()+2 : 1;
                        SkillWrapper.addActionSP(player,"agility",speedFactor);
                    }
                    ISkillCapability cap = SkillWrapper.getSkillCapability(player);
                    if(Objects.nonNull(cap)) cap.decrementGatheringItems(20);
                    if(player.dimension==CALIGRO.getId() && player.posY<0 &&
                            ItemUtil.isHolding(player,blackTotem))
                        WorldUtil.teleportDimY(player,VOID_WORLD.getId(),256d);
                    TICK_DELAY.setValue(0);
                }
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public static void worldTick(WorldTickEvent event) {
        if(event.isCanceled()) return;
        if(event.phase.ordinal()==1) {
            int dim = event.world.provider.getDimension();
            if(dim==44 || dim==45) {
                synchronized(event.world.playerEntities) {
                    EntityPlayerMP wakingUp = null;
                    for(EntityPlayer p : event.world.playerEntities) {
                        EntityPlayerMP player = (EntityPlayerMP) p;
                        int ticks = checkTime(player.getHeldItemOffhand()) || checkTime(player.getHeldItemMainhand()) ? -1 : 1;
                        if(SkillWrapper.ticKDreamer(player, ticks)) {
                            wakingUp = player;
                            break;
                        }
                    }
                    //This needs to be outside the loop to avoid a cmod error since teleporting removes the player from the list
                    if(Objects.nonNull(wakingUp)) wakeUp(wakingUp);
                }
            }
        }
    }

    private static boolean checkTime(ItemStack stack) {
        return stack.getItem()==timeInABottle &&
                ((IItemTimeInABottle)stack.getItem()).dimhoppertweaks$hasTime(stack);
    }

    @SuppressWarnings("ConstantValue")
    private static void wakeUp(EntityPlayerMP player) {
        ISkillCapability cap = SkillWrapper.getSkillCapability(player);
        if(Objects.nonNull(cap)) cap.resetDreamTimer();
        int respawnDim = player.getSpawnDimension();
        BlockPos respawnPos = player.getBedLocation(respawnDim);
        if(Objects.isNull(respawnPos)) respawnPos = player.getPosition();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        server.getCommandManager().executeCommand(server, DHDebugCommands.buildRawCommand("tpx",player.getName(),
                respawnPos.getX(),respawnPos.getY(),respawnPos.getZ(),respawnDim));
    }
}
