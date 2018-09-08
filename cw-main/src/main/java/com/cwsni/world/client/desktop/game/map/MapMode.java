package com.cwsni.world.client.desktop.game.map;

import com.cwsni.world.util.CwException;

public class MapMode {
	private static final String EVENT_CODE_PREFIX = "event.";

	public static final MapMode DEFAULT_MODE = new MapMode();

	private String code;
	private MapModeEnum mode;
	private String label;
	private String toolTip;

	public MapMode(GsMapModePanel gsMapModePanel, String code, MapModeEnum mapMode, String label, String tooltip) {
		this.setCode(code);
		this.setMode(mapMode);
		this.setLabel(label);
		setToolTip(tooltip);
	}

	private MapMode() {
		this.mode = MapModeEnum.GEO;
		this.code = "geo";
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public MapModeEnum getMode() {
		return mode;
	}

	public void setMode(MapModeEnum mapMode) {
		this.mode = mapMode;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MapMode)) {
			return false;
		}
		return ((MapMode) obj).getCode().equals(getCode());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ". mode=" + mode + ", code=" + code;
	}

	public boolean isEventType() {
		return MapModeEnum.EVENT.equals(mode);
	}

	public String getEventScriptCode() {
		if (!isEventType()) {
			throw new CwException("It is not event map mode!");
		}
		return code.substring(EVENT_CODE_PREFIX.length());
	}

	public static String createEventMapCode(String code) {
		return EVENT_CODE_PREFIX + code;
	}

}