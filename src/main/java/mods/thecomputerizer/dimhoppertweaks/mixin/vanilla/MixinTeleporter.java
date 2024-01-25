package mods.thecomputerizer.dimhoppertweaks.mixin.vanilla;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Teleporter;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Teleporter.class)
public abstract class MixinTeleporter {

    @Unique private static final ResourceLocation COMPRESSED_OBSIDIAN = new ResourceLocation("overloaded:compressed_obsidian");

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;"+
            "getDefaultState()Lnet/minecraft/block/state/IBlockState;", ordinal = 0), method = "placeInPortal")
    private IBlockState dimhoppertweaks$replaceObsidian1(Block block) {
        return dimhoppertweaks$getCompressedObsidian().getDefaultState();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;"+
            "getDefaultState()Lnet/minecraft/block/state/IBlockState;", ordinal = 0), method = "makePortal")
    private IBlockState dimhoppertweaks$replaceObsidian2(Block block) {
        return dimhoppertweaks$getCompressedObsidian().getDefaultState();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;"+
            "getDefaultState()Lnet/minecraft/block/state/IBlockState;", ordinal = 2), method = "makePortal")
    private IBlockState dimhoppertweaks$replaceObsidian3(Block block) {
        return dimhoppertweaks$getCompressedObsidian().getDefaultState();
    }


    @Unique
    private Block dimhoppertweaks$getCompressedObsidian() {
        return ForgeRegistries.BLOCKS.containsKey(COMPRESSED_OBSIDIAN) ?
                ForgeRegistries.BLOCKS.getValue(COMPRESSED_OBSIDIAN) : Blocks.OBSIDIAN;
    }
}