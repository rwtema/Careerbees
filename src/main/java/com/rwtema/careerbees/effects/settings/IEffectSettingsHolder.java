package com.rwtema.careerbees.effects.settings;

public interface IEffectSettingsHolder {
	<V> V getValue(Setting<V, ?> setting);

	IEffectSettingsHolder DEFAULT_INSTANCE = Setting::getDefault;
}
