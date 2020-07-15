package dev.jacobruby.minimapmod;

import dev.jacobruby.minimapmod.map.MiniMapData;
import dev.jacobruby.minimapmod.map.MiniMapRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = MiniMapMod.MOD_ID, version = MiniMapMod.VERSION)
public class MiniMapMod {
    public static final String MOD_ID = "minimap";
    public static final String VERSION = "1.0";

    private static MiniMapMod INSTANCE;

    public static MiniMapMod instance() {
        return INSTANCE;
    }

    private MiniMapEvents events;
    private MiniMapRenderer renderer;

    MiniMapData virtualMap;

    public MiniMapMod() {
        INSTANCE = this;
        this.events = new MiniMapEvents();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this.events);
    }

    public boolean isRendererPrepared() {
        return this.renderer != null;
    }

    public MiniMapRenderer getRenderer() {
        if (this.renderer == null) {
            this.renderer = new MiniMapRenderer();
        }

        return this.renderer;
    }
    
    public float lerp(float f1, float f2, float progress) {
        return f1 + (f2 - f1) * progress;
    }

    public double lerp(double d1, double d2, double progress) {
        return d1 + (d2 - d1) * progress;
    }
}
