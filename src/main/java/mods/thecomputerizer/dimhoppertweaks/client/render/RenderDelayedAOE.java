package mods.thecomputerizer.dimhoppertweaks.client.render;

import codechicken.lib.colour.ColourRGBA;
import mods.thecomputerizer.dimhoppertweaks.client.DHTClient;
import mods.thecomputerizer.dimhoppertweaks.registry.entities.boss.EntityFinalBoss;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.dimhoppertweaks.client.DHTClient.ATTACK;
import static net.minecraft.client.renderer.GlStateManager.SourceFactor.ONE;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;
import static org.lwjgl.opengl.GL11.GL_QUADS;

@SuppressWarnings("DataFlowIssue")
public class RenderDelayedAOE {

    private final Vec3d posVec;
    private final int delayTicks;
    private final int range;
    private final MutableInt timer;
    private final EntityFinalBoss boss;
    private final int phaseFired;

    public RenderDelayedAOE(Vec3d posVec, int delayTicks, int range, EntityFinalBoss boss, int phase) {
        this.posVec = posVec;
        this.delayTicks = delayTicks;
        this.range = range;
        this.timer = new MutableInt(delayTicks);
        this.boss = boss;
        this.phaseFired = phase;
    }

    public boolean tick() {
        this.timer.decrement();
        return this.timer.getValue()<=0;
    }

    private boolean canRender() {
        return this.timer.getValue()>0 && Objects.nonNull(this.boss) && this.boss.isEntityAlive() && this.boss.isPhase(this.phaseFired);
    }

    private float getRenderSize(float partialTicks) {
        float curTime = this.timer.getValue();
        if(curTime<=1) return (float)this.range;
        float min = 0.1f;
        float max = ((float)this.range)/2f;
        float scaleTime = ((float)this.delayTicks)/4f;
        if(curTime>scaleTime) return min;
        float prevSize = (1f-((curTime-1f)/scaleTime))*(max-min);
        float curSize = (1f-(curTime/scaleTime))*(max-min);
        return prevSize+((curSize-prevSize)*partialTicks);
    }
    
    @SideOnly(CLIENT)
    public void render(float partialTicks) {
        if(this.canRender()) {
            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(ONE,DestFactor.ONE);
            EntityPlayer viewingPlayer = Minecraft.getMinecraft().player;
            double translationXLT = this.posVec.x-viewingPlayer.prevPosX;
            double translationYLT = this.posVec.y-viewingPlayer.prevPosY;
            double translationZLT = this.posVec.z-viewingPlayer.prevPosZ;
            double translationX = translationXLT+(((this.posVec.x-viewingPlayer.posX)-translationXLT)*partialTicks);
            double translationY = translationYLT+(((this.posVec.y-viewingPlayer.posY)-translationYLT)*partialTicks);
            double translationZ = translationZLT+(((this.posVec.z-viewingPlayer.posZ)-translationZLT)*partialTicks);
            GlStateManager.translate(translationX,translationY,translationZ);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ATTACK);
            float scale = getRenderSize(partialTicks);
            GlStateManager.scale(scale,scale,scale);
            GlStateManager.color(1f,1f,1f,1f);
            ColourRGBA color = scale >= this.range ? new ColourRGBA(1f,0f,0f,0.5f) :
                    new ColourRGBA(0.9f,0.4f,0f,0.2f);
            renderOBJ(DHTClient.getBakedForcefield().getQuads(null,null,0),color.argb());
            GlStateManager.popMatrix();
        }
    }

    private void renderOBJ(List<BakedQuad> listQuads, int ARGB) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        for(BakedQuad quad : listQuads) {
            buffer.begin(GL_QUADS,ITEM);
            buffer.addVertexData(quad.getVertexData());
            buffer.putColor4(ARGB);
            Vec3i vec3i = quad.getFace().getDirectionVec();
            buffer.putNormal((float)vec3i.getX(),(float)vec3i.getY(),(float)vec3i.getZ());
            tessellator.draw();
        }
    }
}
