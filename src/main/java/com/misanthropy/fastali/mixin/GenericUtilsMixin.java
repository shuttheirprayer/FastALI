package com.misanthropy.fastali.mixin;

import com.misanthropy.fastali.AliOptimizations;
import com.misanthropy.fastali.FastAliConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.yanny.ali.compatibility.common.GenericUtils", remap = false)
public abstract class GenericUtilsMixin {
	@Inject(method = "processData", at = @At("HEAD"))
	private static void fastali$beginTiming(CallbackInfo ci) {
		if (FastAliConfig.OPTIMIZE_ALI && FastAliConfig.TIMINGS) {
			AliOptimizations.beginProcessData();
		}
	}

	@Inject(method = "processData", at = @At("RETURN"))
	private static void fastali$logTiming(CallbackInfo ci) {
		if (FastAliConfig.OPTIMIZE_ALI && FastAliConfig.TIMINGS) {
			AliOptimizations.logProcessDataSummary();
		}
	}
}
