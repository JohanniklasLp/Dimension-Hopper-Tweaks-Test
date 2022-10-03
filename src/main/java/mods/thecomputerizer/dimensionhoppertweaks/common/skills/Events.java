package mods.thecomputerizer.dimensionhoppertweaks.common.skills;

import codersafterdark.reskillable.api.event.LevelUpEvent;
import mods.thecomputerizer.dimensionhoppertweaks.DimensionHopperTweaks;
import mods.thecomputerizer.dimensionhoppertweaks.common.objects.items.SkillToken;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

@Mod.EventBusSubscriber(modid = DimensionHopperTweaks.MODID)
public class Events {

    public static final ResourceLocation SKILL_CAPABILITY = new ResourceLocation(DimensionHopperTweaks.MODID, "skills");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityPlayerMP) {
            event.addCapability(SKILL_CAPABILITY, new SkillCapabilityProvider());
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static ISkillCapability getSkillCapability(EntityPlayer player) {
        return player.getCapability(SkillCapabilityProvider.SKILL_CAPABILITY,null);
    }

    public static void updateTokenDrainValues(String skill, int levels, EntityPlayerMP player) {
        getSkillCapability(player).setDrainSelection(skill,levels);
        updateTokens(player);
    }

    public static void updateTokens(EntityPlayer player) {
        if(player instanceof EntityPlayerMP) getSkillCapability(player).syncSkills((EntityPlayerMP) player);
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if(stack.getItem() instanceof SkillToken) {
                SkillToken token = (SkillToken) stack.getItem();
                ISkillCapability cap = getSkillCapability(player);
                token.updateSkills(stack, cap.getCurrentValues(),cap.getDrainSelection(),cap.getDrainLevels());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelUp(LevelUpEvent.Post event) {
        if(event.getEntityPlayer() instanceof EntityPlayerMP) {
            getSkillCapability(event.getEntityPlayer()).syncSkills((EntityPlayerMP) event.getEntityPlayer());
            updateTokens(event.getEntityPlayer());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.player instanceof EntityPlayerMP)
            getSkillCapability(event.player).syncSkills((EntityPlayerMP)event.player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if(event.side==Side.SERVER && event.phase==TickEvent.Phase.END) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            if(player.isSprinting() && getSkillCapability(player).checkTick()) {
                getSkillCapability(player).addSkillXP("agility",getSkillCapability(player).getSkillXpMultiplier(1f),player);
                updateTokens(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pickUpItem(PlayerEvent.ItemPickupEvent event) {
        if(event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            getSkillCapability(player).addSkillXP("gathering",getSkillCapability(player).getSkillXpMultiplier(1f), player);
            updateTokens(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void breakSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        if(event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
            event.setNewSpeed(event.getOriginalSpeed()*(1f+getSkillCapability(player).getBreakSpeedMultiplier()));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blockBreak(BlockEvent.BreakEvent event) {
        if(!event.getWorld().isRemote && event.getPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getPlayer();
            if(player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
                getSkillCapability(player).addSkillXP("mining",getSkillCapability(player).getSkillXpMultiplier(1f), player);
                updateTokens(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blockPlace(BlockEvent.PlaceEvent event) {
        if(!event.getWorld().isRemote && event.getPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getPlayer();
            getSkillCapability(player).addSkillXP("building",getSkillCapability(player).getSkillXpMultiplier(1f),player);
            updateTokens(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if(!event.getEntityLiving().world.isRemote && event.getSource() != DamageSource.OUT_OF_WORLD) {
            if (event.getEntityLiving() instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
                event.setAmount(Math.max(0f,event.getAmount()-getSkillCapability(player).getDamageReduction()));
                getSkillCapability(player).addSkillXP("defense",getSkillCapability(player).getSkillXpMultiplier(Math.max(0f,(event.getAmount() / 2f))), player);
                updateTokens(player);
            } else if (!(event.getEntityLiving() instanceof EntityPlayer) && event.getSource().getTrueSource() instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) event.getSource().getTrueSource();
                event.setAmount(event.getAmount()+getSkillCapability(player).getDamageMultiplier());
                getSkillCapability(player).addSkillXP("attack",getSkillCapability(player).getSkillXpMultiplier(Math.max(0f,(event.getAmount() / 2f))), player);
                updateTokens(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if(event.getEntityLiving() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
            getSkillCapability(player).addSkillXP("agility",getSkillCapability(player).getSkillXpMultiplier(2f),player);
            updateTokens(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHoe(UseHoeEvent event) {
        if(!event.getWorld().isRemote && event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
            if(event.getResult()==Event.Result.ALLOW)  {
                getSkillCapability(player).addSkillXP("farming",getSkillCapability(player).getSkillXpMultiplier(3f),player);
                updateTokens(player);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void pickupXP(PlayerPickupXpEvent event) {
        if(event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
            getSkillCapability(player).addSkillXP("magic",getSkillCapability(player).getSkillXpMultiplier(1f),player);
            updateTokens(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChangedDimensions(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.player;
            getSkillCapability(player).addSkillXP("void",getSkillCapability(player).getSkillXpMultiplier(5f),player);
            updateTokens(player);
            if(event.toDim==7) player.setSpawnChunk(new BlockPos(player.posX,100,player.posZ),false,7);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAdvancement(AdvancementEvent event) {
        if(event.getEntityPlayer() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
            getSkillCapability(player).addSkillXP("research",getSkillCapability(player).getSkillXpMultiplier(5f),player);
            updateTokens(player);
        }
    }
}
