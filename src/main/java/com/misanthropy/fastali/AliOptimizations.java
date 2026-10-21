package com.misanthropy.fastali;

import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IItemNode;
import com.yanny.ali.api.ListNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class AliOptimizations {
	private AliOptimizations() {}

	private static final Map<Level, Map<EntityType<?>, List<Entity>>> ENTITY_CACHE = new ConcurrentHashMap<>();

	public static List<Entity> getCachedEntities(EntityType<?> type, Level level) {
		Map<EntityType<?>, List<Entity>> byType = ENTITY_CACHE.get(level);
		return byType != null ? byType.get(type) : null;
	}

	public static void cacheEntities(EntityType<?> type, Level level, List<Entity> entities) {
		if (entities != null) {
			ENTITY_CACHE.computeIfAbsent(level, k -> new ConcurrentHashMap<>()).put(type, entities);
		}
	}

	public static void invalidate(Level level) {
		ENTITY_CACHE.remove(level);
	}

	private static LootTable plainTable;
	private static IDataNode plainNode;

	public static void rememberPlainNode(LootTable table, IDataNode node) {
		plainTable = table;
		plainNode = node;
	}

	public static IDataNode takePlainNode(LootTable table) {
		if (table == null || table != plainTable) {
			return null;
		}
		IDataNode node = plainNode;
		clearPlainNode();
		parseReuse.incrementAndGet();
		return node;
	}

	public static void clearPlainNode() {
		plainTable = null;
		plainNode = null;
	}

	private static final Map<Item, List<Enchantment>> ENCHANT_SCANS = new IdentityHashMap<>();

	public static List<Enchantment> getCachedEnchantScan(Item item) {
		List<Enchantment> cached = ENCHANT_SCANS.get(item);
		if (cached != null) {
			enchantCacheHits.incrementAndGet();
		}
		return cached;
	}

	public static void cacheEnchantScan(Item item, List<Enchantment> enchantments) {
		ENCHANT_SCANS.put(item, enchantments);
	}

	public static void clearEnchantScans() {
		ENCHANT_SCANS.clear();
	}

	public static boolean hasItems(IDataNode node) {
		if (node instanceof ListNode listNode) {
			for (IDataNode child : listNode.nodes()) {
				if (hasItems(child)) {
					return true;
				}
			}
			return false;
		}
		return node instanceof IItemNode itemNode && !itemNode.getItems().isEmpty();
	}

	private static final AtomicInteger cacheHits = new AtomicInteger();
	private static final AtomicInteger parseReuse = new AtomicInteger();
	private static final AtomicInteger enchantCacheHits = new AtomicInteger();
	private static final AtomicInteger emptinessChecks = new AtomicInteger();
	private static final AtomicInteger enchantScans = new AtomicInteger();
	private static final AtomicLong startNanos = new AtomicLong();
	private static volatile long clientStartNanos;

	public static void recordCacheHit() {
		cacheHits.incrementAndGet();
	}

	public static void recordEmptinessCheck() {
		emptinessChecks.incrementAndGet();
	}

	public static void recordEnchantScan() {
		enchantScans.incrementAndGet();
	}

	public static void beginReadLootTables() {
		cacheHits.set(0);
		parseReuse.set(0);
		enchantCacheHits.set(0);
		emptinessChecks.set(0);
		enchantScans.set(0);
		startNanos.set(System.nanoTime());
	}

	public static void logReadLootTablesSummary() {
		long totalMs = (System.nanoTime() - startNanos.get()) / 1_000_000L;
		Fastali.LOGGER.info(
				"[FastALI] readLootTables {}ms | {} enchantment scans stopped at 2 matches | {} enchantment scans from cache | " +
						"{} loot tables parsed once instead of twice | " +
						"{} node trees emptiness-checked without materializing items | {} entity builds from cache",
				totalMs, enchantScans.get(), enchantCacheHits.get(), parseReuse.get(), emptinessChecks.get(), cacheHits.get());
	}

	public static void beginProcessData() {
		cacheHits.set(0);
		clientStartNanos = System.nanoTime();
	}

	public static void logProcessDataSummary() {
		long totalMs = (System.nanoTime() - clientStartNanos) / 1_000_000L;
		Fastali.LOGGER.info("[FastALI] processData {}ms | {} entity builds from cache", totalMs, cacheHits.get());
	}
}
