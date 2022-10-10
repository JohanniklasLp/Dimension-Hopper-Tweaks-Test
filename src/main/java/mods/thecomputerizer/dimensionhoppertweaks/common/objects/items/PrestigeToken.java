package mods.thecomputerizer.dimensionhoppertweaks.common.objects.items;

import mods.thecomputerizer.dimensionhoppertweaks.common.skills.Events;
import mods.thecomputerizer.dimensionhoppertweaks.common.skills.ISkillCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PrestigeToken extends EpicItem {

    private final int level;
    public PrestigeToken(int level) {
        this.level = level;
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand) {
        ItemStack stack = playerIn.getHeldItemMainhand();
        if (playerIn instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) playerIn;
            ISkillCapability cap = Events.getSkillCapability(player);
            String skill = cap.getDrainSelection();
            if(cap.getPrestigeLevel(skill)<level && hand==EnumHand.MAIN_HAND) {
                cap.setPrestigeLevel(skill,level);
                Events.updateTokens(player);
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0F, 1.0F);
                world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1.0F, 1.0F);
                stack.shrink(1);
                return ActionResult.newResult(EnumActionResult.SUCCESS,stack);
            }
        } return ActionResult.newResult(EnumActionResult.PASS,stack);
    }
}
