package mods.thecomputerizer.dimhoppertweaks.common.commands;

import mcp.MethodsReturnNonnullByDefault;
import mods.thecomputerizer.dimhoppertweaks.network.PacketQueryGenericClient;
import mods.thecomputerizer.dimhoppertweaks.network.PacketTileEntityClassQuery;
import net.darkhax.gamestages.GameStageHelper;
import net.darkhax.gamestages.data.GameStageSaveHandler;
import net.darkhax.gamestages.data.IStageData;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DHDebugCommands extends DHTCommand {

    public DHDebugCommands() {
        super("dhd");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        int option = 0;
        try {
            option = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendMessage(sender,true,"number",args[0]);
        }
        if(option==1) {
            executeBlockData(server,sender,getOrNull(1,args),getOrNull(2,args));
            return;
        }
        if(option==2) {
            executeTileClass(server,sender,getOrNull(1,args));
            return;
        }
        if(option==3) {
            executeGamestage(server,sender,getOrNull(1,args));
            return;
        }
        if(option==4) {
            executeGive(server,sender,getOrNull(1,args),getOrNull(2,args));
            return;
        }
        if(option==5) {
            executeQuery(server,sender,getOrNull(1,args));
            return;
        }
        sendMessage(sender,true,"options."+(sender instanceof Entity ? "entity" : "server"));
    }

    private void executeBlockData(MinecraftServer server, ICommandSender sender, @Nullable String arg1,
                                  @Nullable String arg2) throws CommandException {
        int offset = -1;
        if(Objects.nonNull(arg1)) {
            try {
                offset = Integer.parseInt(arg1);
            } catch (NumberFormatException ex) {
                sendMessage(sender,true, "number",arg1);
            }
        }
        Entity entity = sender instanceof Entity ? (Entity)sender : null;
        if(Objects.nonNull(arg2)) {
            try {
                entity = getEntity(server,sender,arg2);
            } catch (EntityNotFoundException ex) {
                sendMessage(sender,true, "entity",arg2);
            }
        }
        if(Objects.isNull(entity)) {
            sendMessage(sender,true,"blockdata");
            return;
        }
        int x = entity.getPosition().getX();
        int y = entity.getPosition().getY()+offset;
        int z = entity.getPosition().getZ();
        buildAndExecuteCommand(server,sender,"blockdata",x,y,z,"{}");
        sendMessage(sender,false,"blockdata",x,y,z);
    }

    private void executeTileClass(MinecraftServer server, ICommandSender sender, @Nullable String arg1)
            throws CommandException {
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if(Objects.nonNull(arg1)) {
            try {
                player = getPlayer(server,sender,arg1);
            } catch (PlayerNotFoundException ex) {
                sendMessage(sender,true,"tileclass.player");
            }
        }
        if(Objects.nonNull(player)) {
            new PacketTileEntityClassQuery().addPlayers(player).send();
            sendMessage(sender,false,"tileclass");
        }
    }

    private void executeGamestage(MinecraftServer server, ICommandSender sender, @Nullable String stage)
            throws CommandException {
        if(Objects.isNull(stage)) {
            sendMessage(sender,true,"gamestage");
            return;
        }
        EntityPlayerMP player = sender instanceof EntityPlayerMP ? (EntityPlayerMP)sender : null;
        if(Objects.isNull(player)) {
            sendMessage(sender,true,"gamestage.player");
            return;
        }
        IStageData data = GameStageHelper.getPlayerData(player);
        if(Objects.isNull(data) || data==GameStageSaveHandler.EMPTY_STAGE_DATA) {
            sendMessage(sender, true, "gamestage.data",player.getName());
            return;
        }
        String type = data.hasStage(stage) ? "remove" : "add";
        buildAndExecuteCommand(server,sender,"gamestage",type,"@s",stage);
        sendMessage(sender,false,"gamestage",type,stage);
    }

    private void executeGive(MinecraftServer server, ICommandSender sender, @Nullable String type,
                             @Nullable String qualifier) throws CommandException {
        if(Objects.isNull(type)) {
            sendMessage(sender,true,"give");
            return;
        }
        if(type.equals("mob")) {
            if(Objects.isNull(qualifier)) {
                sendMessage(sender,true,"give.mob");
                return;
            }
            giveMob(stack -> {
                World world = sender.getEntityWorld();
                if(!world.isRemote) {
                    Vec3d posVec = sender.getPositionVector();
                    EntityItem item = new EntityItem(sender.getEntityWorld(), posVec.x, posVec.y, posVec.z, stack);
                    item.setNoPickupDelay();
                    item.setOwner(sender.getName());
                    world.spawnEntity(item);
                }
            },qualifier);
        }
    }

    private void giveMob(Consumer<ItemStack> itemEntityCreator, String mob) {
        if(!mob.contains(":")) mob = "minecraft:"+mob;
        ItemStack stack = new ItemStack(Items.SPAWN_EGG);
        ItemMonsterPlacer.applyEntityIdToItemStack(stack,new ResourceLocation(mob));
        itemEntityCreator.accept(stack);
    }

    private void executeQuery(MinecraftServer server, ICommandSender sender, @Nullable String type) throws CommandException {
        if(Objects.isNull(type)) {
            sendMessage(sender,true,"query");
            return;
        }
        if(sender instanceof EntityPlayerMP)
            new PacketQueryGenericClient(type).addPlayers((EntityPlayerMP)sender).send();
    }
}
