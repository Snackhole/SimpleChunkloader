package com.snackhole.simplechunkloader;

import com.snackhole.simplechunkloader.chunkloading.ChunkloadManager;
import com.snackhole.simplechunkloader.commands.CommandChunkload;
import com.snackhole.simplechunkloader.proxy.IProxy;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = SimpleChunkloaderMain.MODID, version = SimpleChunkloaderMain.VERSION)
public class SimpleChunkloaderMain {
    public static final String MODID = "simplechunkloader";
    public static final String VERSION = "1";
    @SidedProxy(clientSide = "com.snackhole.simplechunkloader.proxy.ClientProxy", serverSide = "com.snackhole.simplechunkloader.proxy.ServerProxy")
    public static IProxy proxy;
    @Mod.Instance
    public static SimpleChunkloaderMain simpleChunkloaderMainInstance;
    public static ChunkloadManager chunkloadManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        chunkloadManager = new ChunkloadManager();
        ForgeChunkManager.setForcedChunkLoadingCallback(simpleChunkloaderMainInstance, chunkloadManager);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandChunkload());
    }

    public SimpleChunkloaderMain() {
        simpleChunkloaderMainInstance = this;
    }
}
