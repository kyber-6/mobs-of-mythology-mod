package net.pixeldreamstudios.mobs_of_mythology;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibMod;
import mod.azure.azurelib.common.internal.common.config.format.ConfigFormats;
import net.pixeldreamstudios.mobs_of_mythology.entity.client.renderer.AutomatonRenderer;
import net.pixeldreamstudios.mobs_of_mythology.entity.client.renderer.ChupacabraRenderer;
import net.pixeldreamstudios.mobs_of_mythology.registry.BlockRegistry;
import net.pixeldreamstudios.mobs_of_mythology.registry.CreativeTabRegistry;
import net.pixeldreamstudios.mobs_of_mythology.registry.EntityRegistry;
import net.pixeldreamstudios.mobs_of_mythology.registry.ItemRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MobsOfMythology {
    public static final String MOD_ID = "mobs_of_mythology";
    public static MobsOfMythologyConfig config;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        AzureLib.initialize();
        config = AzureLibMod.registerConfig(MobsOfMythologyConfig.class, ConfigFormats.yaml()).getConfigInstance();

        EntityRegistry.init();
        ItemRegistry.init();
        BlockRegistry.init();
        CreativeTabRegistry.init();
    }

    public static void initClient() {
        LOGGER.info("KOKOS CLIENT INIT");
        EntityRendererRegistry.register(EntityRegistry.AUTOMATON, AutomatonRenderer::new);
        EntityRendererRegistry.register(EntityRegistry.CHUPACABRA, ChupacabraRenderer::new);
    }
}
