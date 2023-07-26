package net.pixeldream.mythicmobs;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.pixeldream.mythicmobs.config.MythicMobsConfigs;
import net.pixeldream.mythicmobs.event.EntityEvents;
import net.pixeldream.mythicmobs.registry.*;
import net.pixeldream.mythicmobs.world.gen.WorldGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;

public class MythicMobs implements ModInitializer {
	public static final String MOD_ID = "mythicmobs";
	public static final String MOD_NAME = "Mythic Mobs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello from " + MOD_NAME + "!");

		GeckoLib.initialize();
		MidnightConfig.init(MOD_ID, MythicMobsConfigs.class);

		ItemRegistry.initialize();
		BlockRegistry.initialize();
		ItemGroupRegistry.initialize();
		EntityRegistry.initialize();
		ParticleRegistry.initialize();
		BlockEntityRegistry.initialize();
		SoundRegistry.initialize();
		WorldGen.generateWorldGen();

		LOGGER.info("Replacing Iron Golems with Automata from " + MOD_NAME);
		EntityEvents.replaceNaturallySpawningIronGolemsWithAutomata();
		MythicMobs.LOGGER.info("Checking for un-spawned Automata from " + MythicMobs.MOD_NAME);
		EntityEvents.checkForUnSpawnedGolem();
	}
}