package com.misanthropy.fastali.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.misanthropy.fastali.AliOptimizations;
import com.misanthropy.fastali.FastAliConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Stream;

@Mixin(targets = "com.yanny.ali.plugin.server.TooltipUtils", remap = false)
public abstract class TooltipUtilsMixin {
	@WrapOperation(
			method = "applyEnchantRandomlyItemStackModifier",
			at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"))
	private static List<Enchantment> fastali$stopAfterTwoMatches(Stream<Enchantment> enchantments,
			Operation<List<Enchantment>> original, @Local(argsOnly = true) ItemStack itemStack) {
		if (!FastAliConfig.OPTIMIZE_ALI || !FastAliConfig.OPTIMIZE_ALI_ENCHANT_SCAN) {
			return original.call(enchantments);
		}
		boolean cacheable = FastAliConfig.OPTIMIZE_ALI_ENCHANT_CACHE && !itemStack.hasTag();
		if (cacheable) {
			List<Enchantment> cached = AliOptimizations.getCachedEnchantScan(itemStack.getItem());
			if (cached != null) {
				return cached;
			}
		}
		AliOptimizations.recordEnchantScan();
		List<Enchantment> result = original.call(enchantments.limit(2));
		if (cacheable) {
			AliOptimizations.cacheEnchantScan(itemStack.getItem(), result);
		}
		return result;
	}
}
