package com.misanthropy.fastali;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class FastAliMixinPlugin implements IMixinConfigPlugin {
	private static final String ALI_PROBE_CLASS = "com.yanny.ali.api.IPlugin";

	private static volatile boolean aliPresent;

	public static boolean isAliPresent() {
		return aliPresent;
	}

	@Override
	public void onLoad(String mixinPackage) {
		boolean present;
		try {
			Class.forName(ALI_PROBE_CLASS, false, FastAliMixinPlugin.class.getClassLoader());
			present = true;
		} catch (Throwable t) {
			present = false;
		}
		aliPresent = present;
		if (present) {
			Fastali.LOGGER.info("[FastALI] Advanced Loot Info detected - applying ALI optimizations");
		} else {
			Fastali.LOGGER.info("[FastALI] Advanced Loot Info not present - ALI optimizations skipped");
		}
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return aliPresent;
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
