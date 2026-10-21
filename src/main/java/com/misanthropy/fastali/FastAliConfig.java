package com.misanthropy.fastali;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Fastali.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class FastAliConfig {
	private FastAliConfig() {}

	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.BooleanValue OPTIMIZE_ALI_CFG = BUILDER
			.comment(
					"Master toggle for the Advanced Loot Info (ALI) optimizations.",
					"Only has any effect when Advanced Loot Info (modid 'ali') is installed - FastALI patches",
					"ALI at runtime, no ALI code is bundled. When ALI is absent this does nothing.")
			.define("optimizeAli", true);

	private static final ForgeConfigSpec.BooleanValue OPTIMIZE_ALI_PARSE_REUSE_CFG = BUILDER
			.comment(
					"Parse each loot table once instead of twice when a loot modifier applies to it.",
					"ALI parses a table once to match loot modifiers against its items, then parses it again",
					"from scratch only to add the modifier's changes. FastALI applies them to the first parse. Safe.")
			.define("optimizeAliParseReuse", true);

	private static final ForgeConfigSpec.BooleanValue OPTIMIZE_ALI_EMPTINESS_CHECK_CFG = BUILDER
			.comment(
					"Decide whether a loot table's node tree is empty without collecting every item in it.",
					"ALI allocates the full item list of every node tree only to call isEmpty() on it. Safe.")
			.define("optimizeAliEmptinessCheck", true);

	private static final ForgeConfigSpec.BooleanValue OPTIMIZE_ALI_ENCHANT_SCAN_CFG = BUILDER
			.comment(
					"Stop scanning the enchantment registry once two enchantments match the item being",
					"described by an enchant_randomly loot function. ALI scans every enchantment to build a",
					"list it only ever asks 'is there exactly one?' and 'give me the first', so the answer is",
					"already decided by the second match. This is the single biggest cost of ALI's loot pass",
					"on a pack with many modded enchantments. Safe.")
			.define("optimizeAliEnchantScan", true);

	private static final ForgeConfigSpec.BooleanValue OPTIMIZE_ALI_ENCHANT_CACHE_CFG = BUILDER
			.comment(
					"Remember the enchant_randomly registry scan per item, so each item is scanned once per loot",
					"pass instead of once per loot table entry. Only used for items without NBT.",
					"Disable if a mod's enchantments check more than the item itself.")
			.define("optimizeAliEnchantCache", true);

	private static final ForgeConfigSpec.BooleanValue OPTIMIZE_ALI_ENTITY_CACHE_CFG = BUILDER
			.comment(
					"Cache the throwaway sample entities ALI builds while resolving which entity owns a loot",
					"table, so a /reload or datapack reload doesn't reconstruct every mob from scratch.",
					"Dropped whenever the level changes or unloads. Safe.")
			.define("optimizeAliEntityCache", true);

	private static final ForgeConfigSpec.BooleanValue LOG_TIMINGS = BUILDER
			.comment("Log how long each FastALI-accelerated phase took and what it avoided.")
			.define("logTimings", true);

	public static final ForgeConfigSpec SPEC = BUILDER.build();

	public static volatile boolean OPTIMIZE_ALI = true;
	public static volatile boolean OPTIMIZE_ALI_PARSE_REUSE = true;
	public static volatile boolean OPTIMIZE_ALI_EMPTINESS_CHECK = true;
	public static volatile boolean OPTIMIZE_ALI_ENCHANT_SCAN = true;
	public static volatile boolean OPTIMIZE_ALI_ENCHANT_CACHE = true;
	public static volatile boolean OPTIMIZE_ALI_ENTITY_CACHE = true;
	public static volatile boolean TIMINGS = true;

	@SubscribeEvent
	static void onConfigLoad(final ModConfigEvent event) {
		if (event.getConfig().getSpec() != SPEC) {
			return;
		}
		OPTIMIZE_ALI = OPTIMIZE_ALI_CFG.get();
		OPTIMIZE_ALI_PARSE_REUSE = OPTIMIZE_ALI_PARSE_REUSE_CFG.get();
		OPTIMIZE_ALI_EMPTINESS_CHECK = OPTIMIZE_ALI_EMPTINESS_CHECK_CFG.get();
		OPTIMIZE_ALI_ENCHANT_SCAN = OPTIMIZE_ALI_ENCHANT_SCAN_CFG.get();
		OPTIMIZE_ALI_ENCHANT_CACHE = OPTIMIZE_ALI_ENCHANT_CACHE_CFG.get();
		OPTIMIZE_ALI_ENTITY_CACHE = OPTIMIZE_ALI_ENTITY_CACHE_CFG.get();
		TIMINGS = LOG_TIMINGS.get();
	}
}
