package com.misanthropy.fastali.mixin;

import com.misanthropy.fastali.AliOptimizations;
import com.misanthropy.fastali.FastAliConfig;
import com.yanny.ali.api.IDataNode;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.yanny.ali.network.AbstractServer$PageTable", remap = false)
public abstract class PageTableMixin {
	@Shadow
	@Final
	private LootTable lootTable;

	@Inject(method = "getPlainNode", at = @At("RETURN"))
	private void fastali$rememberPlainNode(CallbackInfoReturnable<IDataNode> cir) {
		if (FastAliConfig.OPTIMIZE_ALI && FastAliConfig.OPTIMIZE_ALI_PARSE_REUSE) {
			AliOptimizations.rememberPlainNode(lootTable, cir.getReturnValue());
		}
	}
}
