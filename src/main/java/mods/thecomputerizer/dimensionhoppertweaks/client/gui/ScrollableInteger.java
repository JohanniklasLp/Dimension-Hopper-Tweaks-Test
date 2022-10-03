package mods.thecomputerizer.dimensionhoppertweaks.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class ScrollableInteger extends CircularScrollableElement {

    private int currentLevel;

    public ScrollableInteger(TokenExchangeGui parent, int centerX, int centerY, int radius, int resolution,
                             int conversionRate, String displayString) {
       super(parent, centerX, centerY, radius, resolution, displayString);
       this.currentLevel = conversionRate;
    }

    @Override
    public void handleScroll() {
        this.currentLevel = translateScroll(this.currentLevel);
        this.setCenterString(""+this.currentLevel);
        this.getParentScreen().setConversionRate(this.currentLevel);
    }

    private int translateScroll(int original) {
        int mouseScroll = Mouse.getEventDWheel();
        if (mouseScroll == 0 || !this.hover) return original;
        if (mouseScroll > 0) return Math.min(100, original + 1);
        return Math.max(1, original - 1);
    }

    private boolean isWithinRadius(int mouseX, int mouseY) {
        if(!(Math.abs(this.centerX-mouseX)<=this.radius)) return false;
        return Math.abs(this.centerY - mouseY) <= this.radius;
    }

    public void render(Minecraft mc, int mouseX, int mouseY, int r, int g, int b, int a) {
        this.hover = isWithinRadius(mouseX, mouseY);
        if(this.hover) {
            a = 255;
            g = 255;
            b = 255;
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        float startAngle = (float) Math.toRadians(180);
        float endAngle = (float) Math.toRadians(540);
        float angle = endAngle - startAngle;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for(int i=0;i<this.resolution;i++) {
            float angle1 = startAngle+(i/(float)this.resolution)*angle;
            float angle2 = startAngle+((i+1)/(float)this.resolution)*angle;
            float xOut = this.centerX+this.radius*(float)Math.cos(angle1);
            float yOut = this.centerY+this.radius*(float)Math.sin(angle1);
            float xOut2 = this.centerX+this.radius*(float)Math.cos(angle2);
            float yOut2 = this.centerY+this.radius*(float)Math.sin(angle2);
            buffer.pos(xOut, yOut, this.zLevel).color(r, g, b, a).endVertex();
            buffer.pos(this.centerX, this.centerY, this.zLevel).color(r, g, b, a).endVertex();
            buffer.pos(xOut2, yOut2, this.zLevel).color(r, g, b, a).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        int color = this.hover ? 16777120 : 14737632;
        this.drawCenteredString(mc.fontRenderer,this.displayString,this.centerX,this.centerY,color);
    }
}
