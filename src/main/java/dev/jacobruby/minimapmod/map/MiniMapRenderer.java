package dev.jacobruby.minimapmod.map;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

/**
 * Mini-map renderer class. Used to render the mini-map texture, overlay, and compass to the screen.
 */
public class MiniMapRenderer {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private final TextureManager textureManager;

    private final DynamicTexture texture;
    private final ResourceLocation resourceLocation;
    private final int[] textureData;

    private final ResourceLocation playerResourceLocation;

    public int xCenter, zCenter;

    public MiniMapRenderer() {
        this.textureManager = MC.getTextureManager();

        this.texture = new DynamicTexture(128, 128);
        this.resourceLocation = this.textureManager.getDynamicTextureLocation("minimap", this.texture);
        this.textureData = this.texture.getTextureData();

        Arrays.fill(this.textureData, 0);

        this.playerResourceLocation = new ResourceLocation("minimap", "player_icon.png");
    }

    /**
     * Used to update the texture data to the given {@code mapData}. This method should only be called immediately after
     * {@code mapData} has been updated.
     *
     * @param mapData the map to set the texture to.
     */
    public void postTextureData(MapData mapData) {
        for (int i = 0; i < 16384; ++i) {
            int j = mapData.colors[i] & 255;

            if (j / 4 == 0) {
                this.textureData[i] = (i + i / 128 & 1) * 8 + 0xFF << 24;
            } else {
                this.textureData[i] = MapColor.mapColorArray[j / 4].func_151643_b(j & 3);
            }
        }

        this.xCenter = mapData.xCenter;
        this.zCenter = mapData.zCenter;
    }

    /**
     * Updates the OpenGL texture. Must be called with an OpenGL context.
     */
    public void updateTexture() {
        this.texture.updateDynamicTexture();
    }

    /**
     * Renders the texture, overlay, and compass to the screen using the current OpenGL context.
     *
     * @param uOffset the amount of offset to apply to the texture's u coordinate. Used to create a smooth movement
     *                effect. The measurement is in meters, or blocks.
     * @param vOffset the amount of offset to apply to the texture's v coordinate. Used to create a smooth movement
     *                effect. The measurement is in meters, or blocks.
     * @param rotation the rotation of the texture and direction of the compass. The context is reverse and backward
     *                 from the standard player yaw. Meaning {@code rotation == -player.rotationYaw + 180}. North is
     *                 calculated as {@code rotation - 90}.
     */
    public void render(double uOffset, double vOffset, double rotation) {
        ScaledResolution sr = new ScaledResolution(MC);
        int scaledHeight = sr.getScaledHeight();
        int scaledWidth = sr.getScaledWidth();

        int resolution = 128;

        /* Mini-map size */
        int size = 128;
        double scale = size / (double) resolution;
        int top = 10;
        int left = scaledWidth - size - 10;

        double radius = size / 2D;
        double xCenter = left + radius;
        double yCenter = top + radius;

        GL11.glPushMatrix();
        GL11.glTranslated(xCenter, yCenter, 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        { /* Render mini-map texture */
            GL11.glPushMatrix();
            GL11.glRotated(rotation, 0, 0, 1);

            this.textureManager.bindTexture(this.resourceLocation);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

            worldRenderer.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);

            for (double angle = 360.0D; angle >= 0.0D; angle -= 2.0D) {
                double radians = Math.toRadians(angle);

                double cos = Math.cos(radians);
                double sin = Math.sin(radians);

                double x = cos * radius;
                double y = sin * radius;
                double u = cos * 0.5 + 0.5 + (uOffset / resolution);
                double v = sin * 0.5 + 0.5 + (vOffset / resolution);

                worldRenderer.pos(x, y, 0.0D).tex(u, v).endVertex();
            }

            tessellator.draw();
            GL11.glPopMatrix();
        }

        { /* Render player icon */
            double height = 15 * scale / 2;
            double width = 11 * scale / 2;

            double x = -(width / 2D);
            double y = -(height / 2D);

            this.textureManager.bindTexture(this.playerResourceLocation);

            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(x, (y + height), 0.0D).tex(0, 1).endVertex();
            worldRenderer.pos((x + width), (y + height), 0.0D).tex(1, 1).endVertex();
            worldRenderer.pos((x + width), y, 0.0D).tex(1, 0).endVertex();
            worldRenderer.pos(x, y, 0.0D).tex(0, 0).endVertex();
            tessellator.draw();
        }

        { /* Render border */
            GlStateManager.disableTexture2D();

            GlStateManager.color(0.3f, 0.3f, 0.3f, 1f);

            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            double borderWidth = 2.5D;
            double segmentLength = 2.0D;

            double x1 = 0D, y1 = 0D, xi1 = 0D, yi1 = 0D;

            for (double angle = 360.0D; angle >= 0.0D; angle -= segmentLength) {
                double radians = Math.toRadians(angle);

                double cos = Math.cos(radians);
                double sin = Math.sin(radians);

                double x2 = cos * (radius + borderWidth);
                double y2 = sin * (radius + borderWidth);

                double xi2 = cos * (radius - borderWidth);
                double yi2 = sin * (radius - borderWidth);

                if (angle != 360.0D) {
                    worldRenderer.pos(x1, y1, 0D).endVertex();
                    worldRenderer.pos(x2, y2, 0D).endVertex();
                    worldRenderer.pos(xi2, yi2, 0D).endVertex();
                    worldRenderer.pos(xi1, yi1, 0D).endVertex();
                }

                x1 = x2;
                y1 = y2;
                xi1 = xi2;
                yi1 = yi2;
            }

            tessellator.draw();

            GlStateManager.enableTexture2D();
        }

        { /* Render compass */
            FontRenderer font = MC.fontRendererObj;

            char[] compass = new char[]{'N', 'E', 'S', 'W'};

            double angle = rotation - 90D;

            double letterRadius = radius + 1D;

            for (char letter : compass) {
                double radians = Math.toRadians(angle);

                double cos = Math.cos(radians);
                double sin = Math.sin(radians);

                double x = cos * letterRadius;
                double y = sin * letterRadius;

                String letterString = String.valueOf(letter);
                int color = letter == 'N' ? 0xFFFF7777 : 0xFFFFFFFF;

                font.drawString(String.valueOf(letter), (float) (x - (font.getStringWidth(letterString) / 2)), (float) y - 4, color, true);

                angle += 90D;
            }
        }

        GL11.glPopMatrix();
    }
}
