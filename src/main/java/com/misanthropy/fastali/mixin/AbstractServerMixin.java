package com.misanthropy.fastali.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.misanthropy.fastali.AliOptimizations;
import com.misanthropy.fastali.FastAliConfig;
import com.yanny.ali.api.IDataNode;
import com.yanny.ali.api.IOperation;
import com.yanny.ali.manager.AliServerRegistry;
import com.yanny.ali.plugin.common.NodeUtils;
import com.yanny.ali.plugin.common.nodes.LootTableNode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(targets = "com.yanny.ali.network.AbstractServer", remap = false)
public abstract class AbstractServerMixin {
	@Inject(method = "readLootTables(Lnet/minecraft/world/level/storage/loot/LootDataManager;)V", at = @At("HEAD"))
	private void fastali$begin(CallbackInfo ci) {
		AliOptimizations.clearEnchantScans();
		AliOptimizations.clearPlainNode();
		if (FastAliConfig.OPTIMIZE_ALI && FastAliConfig.TIMINGS) {
			AliOptimizations.beginReadLootTables();
		}
	}

	@Inject(method = "readLootTables(Lnet/minecraft/world/level/storage/loot/LootDataManager;)V", at = @At("RETURN"))
	private void fastali$end(CallbackInfo ci) {
		AliOptimizations.clearEnchantScans();
		AliOptimizations.clearPlainNode();
		if (FastAliConfig.OPTIMIZE_ALI && FastAliConfig.TIMINGS) {
			AliOptimizations.logReadLootTablesSummary();
		}
	}

	@WrapOperation(
			method = "parseNode",
			at = @At(
					value = "INVOKE",
					target = "Lcom/yanny/ali/manager/AliServerRegistry;parseTable(Ljava/util/List;Lnet/minecraft/world/level/storage/loot/LootTable;)Lcom/yanny/ali/api/IDataNode;"))
	private static IDataNode fastali$reusePlainParse(AliServerRegistry registry, List<IOperation> operations, LootTable lootTable,
			Operation<IDataNode> original) {
		if (FastAliConfig.OPTIMIZE_ALI && FastAliConfig.OPTIMIZE_ALI_PARSE_REUSE
				&& AliOptimizations.takePlainNode(lootTable) instanceof LootTableNode node) {
			NodeUtils.processOperations(operations, node);
			return node;
		}
		return original.call(registry, operations, lootTable);
	}

	@WrapOperation(
			method = "removeEmptyLootTable",
			at = @At(
					value = "INVOKE",
					target = "Lcom/yanny/ali/network/AbstractServer;collectItems(Lcom/yanny/ali/api/IDataNode;)Ljava/util/List;"))
	private static List<ItemStack> fastali$emptinessCheckWithoutItems(IDataNode node, Operation<List<ItemStack>> original) {
		if (!FastAliConfig.OPTIMIZE_ALI || !FastAliConfig.OPTIMIZE_ALI_EMPTINESS_CHECK) {
			return original.call(node);
		}
		AliOptimizations.recordEmptinessCheck();
		return AliOptimizations.hasItems(node) ? Collections.singletonList(ItemStack.EMPTY) : Collections.emptyList();
	}
}
