package dev.jacobruby.minimapmod;

import dev.jacobruby.minimapmod.map.MiniMapRenderer;
import dev.jacobruby.minimapmod.map.MiniMapData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Event handler class. Contains all event handlers used in this mod.
 */
public class MiniMapEvents {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private final MiniMapMod mod = MiniMapMod.instance();

    /**
     * When the player joins or enters a new dimension, create a new mini-map.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void worldLoadEvent(WorldEvent.Load event) {
        MapData mapData = this.mod.virtualMap = new MiniMapData();
        mapData.scale = 0;
    }

    /**
     * Used to scan the world and update the mini-map texture data.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void worldTickEvent(ClientTickEvent event) {
        EntityPlayer player = MC.thePlayer;

        if (player == null || event.phase != TickEvent.Phase.END) {
            return;
        }

        World worldIn = player.worldObj;
        MiniMapData mapData = this.mod.virtualMap;

        mapData.update(worldIn, player);

        if (this.mod.isRendererPrepared()) {
            this.mod.getRenderer().postTextureData(mapData);
        }
    }

    /**
     * Used to render the mini-map graphics to the screen.
     */
    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        MiniMapRenderer renderer = this.mod.getRenderer();

        EntityPlayer player = MC.thePlayer;

        double rotation = -player.rotationYaw + 180D;
        renderer.updateTexture();

        double xOffset = this.mod.lerp(player.lastTickPosX, player.posX, event.partialTicks) - renderer.xCenter;
        double zOffset = this.mod.lerp(player.lastTickPosZ, player.posZ, event.partialTicks) - renderer.zCenter;

        // Offset V for the render is the player's Z position, not to be confused.
        renderer.render(xOffset, zOffset, rotation);
    }
}
