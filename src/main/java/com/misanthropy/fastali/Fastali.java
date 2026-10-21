package com.misanthropy.fastali;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Fastali.MODID)
public class Fastali {
	public static final String MODID = "fastali";
	public static final Logger LOGGER = LogUtils.getLogger();

	public Fastali(FMLJavaModLoadingContext context) {
		context.registerConfig(ModConfig.Type.COMMON, FastAliConfig.SPEC);
		context.getModEventBus().addListener((FMLCommonSetupEvent event) ->
				event.enqueueWork(Fastali::validateAliTargets)
		);
	}

	private static void validateAliTargets() {
		if (!FastAliMixinPlugin.isAliPresent()) {
			LOGGER.info("[FastALI] Advanced Loot Info not present - optimizations are inactive");
			return;
		}
		String[] targets = {
				"com.yanny.ali.compatibility.common.GenericUtils",
				"com.yanny.ali.manager.AliCommonRegistry",
				"com.yanny.ali.network.AbstractServer",
				"com.yanny.ali.network.AbstractServer$PageTable",
				"com.yanny.ali.plugin.server.TooltipUtils"
		};
		ClassLoader loader = Fastali.class.getClassLoader();
		boolean allOk = true;
		for (String target : targets) {
			try {
				Class.forName(target, false, loader);
			} catch (Throwable t) {
				allOk = false;
				LOGGER.error("[FastALI] Advanced Loot Info is present but target {} could not be loaded - " +
						"this ALI version may be unsupported. ALI optimizations for this class will not work.", target, t);
			}
		}
		if (allOk) {
			LOGGER.info("[FastALI] Advanced Loot Info optimization targets loaded and patched successfully");
		}
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static final class LevelHooks {
		private LevelHooks() {}

		@SubscribeEvent
		public static void onLevelUnload(LevelEvent.Unload event) {
			if (event.getLevel() instanceof Level level) {
				AliOptimizations.invalidate(level);
			}
		}
	}
}
