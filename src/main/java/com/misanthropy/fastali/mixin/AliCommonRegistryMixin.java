package com.misanthropy.fastali.mixin;

import com.misanthropy.fastali.AliOptimizations;
import com.misanthropy.fastali.FastAliConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "com.yanny.ali.manager.AliCommonRegistry", remap = false)
public abstract class AliCommonRegistryMixin {
	@Inject(method = "createEntities", at = @At("HEAD"), cancellable = true)
	private void fastali$entityCacheLookup(EntityType<?> type, Level level, CallbackInfoReturnable<List<Entity>> cir) {
		if (!FastAliConfig.OPTIMIZE_ALI || !FastAliConfig.OPTIMIZE_ALI_ENTITY_CACHE) {
			return;
		}
		List<Entity> cached = AliOptimizations.getCachedEntities(type, level);
		if (cached != null) {
			AliOptimizations.recordCacheHit();
			cir.setReturnValue(cached);
		}
	}

	@Inject(method = "createEntities", at = @At("RETURN"))
	private void fastali$entityCacheStore(EntityType<?> type, Level level, CallbackInfoReturnable<List<Entity>> cir) {
		if (!FastAliConfig.OPTIMIZE_ALI || !FastAliConfig.OPTIMIZE_ALI_ENTITY_CACHE) {
			return;
		}
		AliOptimizations.cacheEntities(type, level, cir.getReturnValue());
	}
}
