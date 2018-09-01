package com.cwsni.world.client.desktop;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

@Component
public class UserUIPreferences extends AbstractSettings {

	private Integer mainWindowWidth = 800;
	private Integer mainWindowHeight = 600;
	private Integer mainWindowPosX = 0;
	private Integer mainWindowPosY = 0;
	private boolean isMainWindowMaximized = false;
	private boolean isTimeControlAutoTurn = true;
	private boolean isTimeControlPauseBetweenTurns = true;
	private boolean isInfoPaneGlobalMinimized = false;
	private boolean isInfoPaneProvinceMinimized = false;
	private boolean isInfoPaneProvinceEventsMinimized = false;
	private boolean isInfoPaneProvinceScienceMinimized = false;
	private boolean isInfoPaneCountryMinimized = false;
	private boolean isInfoPaneProvinceArmiesMinimized = false;

	@Override
	@PostConstruct
	public void init() {
		filePath = "settings_ui.ini";
		super.init();
	}

	public Integer getMainWindowWidth() {
		return mainWindowWidth;
	}

	public void setMainWindowWidth(Integer mainWindowWidth) {
		this.mainWindowWidth = mainWindowWidth;
	}

	public Integer getMainWindowHeight() {
		return mainWindowHeight;
	}

	public void setMainWindowHeight(Integer mainWindowHeight) {
		this.mainWindowHeight = mainWindowHeight;
	}

	public Integer getMainWindowPosX() {
		return mainWindowPosX;
	}

	public void setMainWindowPosX(Integer mainWindowPosX) {
		this.mainWindowPosX = mainWindowPosX;
	}

	public Integer getMainWindowPosY() {
		return mainWindowPosY;
	}

	public void setMainWindowPosY(Integer mainWindowPosY) {
		this.mainWindowPosY = mainWindowPosY;
	}

	public boolean isMainWindowMaximized() {
		return isMainWindowMaximized;
	}

	public void setMainWindowMaximized(boolean isMainWindowMaximized) {
		this.isMainWindowMaximized = isMainWindowMaximized;
	}

	public boolean isTimeControlAutoTurn() {
		return isTimeControlAutoTurn;
	}

	public void setTimeControlAutoTurn(boolean isTimeControlAutoTurn) {
		this.isTimeControlAutoTurn = isTimeControlAutoTurn;
	}

	public boolean isTimeControlPauseBetweenTurns() {
		return isTimeControlPauseBetweenTurns;
	}

	public void setTimeControlPauseBetweenTurns(boolean isTimeControlPauseBetweenTurns) {
		this.isTimeControlPauseBetweenTurns = isTimeControlPauseBetweenTurns;
	}

	public boolean isInfoPaneGlobalMinimized() {
		return isInfoPaneGlobalMinimized;
	}

	public void setInfoPaneGlobalMinimized(boolean isInfoPaneGlobalMinimized) {
		this.isInfoPaneGlobalMinimized = isInfoPaneGlobalMinimized;
	}

	public boolean isInfoPaneProvinceMinimized() {
		return isInfoPaneProvinceMinimized;
	}

	public void setInfoPaneProvinceMinimized(boolean isInfoPaneProvinceMinimized) {
		this.isInfoPaneProvinceMinimized = isInfoPaneProvinceMinimized;
	}

	public boolean isInfoPaneProvinceEventsMinimized() {
		return isInfoPaneProvinceEventsMinimized;
	}

	public void setInfoPaneProvinceEventsMinimized(boolean isInfoPaneProvinceEventsMinimized) {
		this.isInfoPaneProvinceEventsMinimized = isInfoPaneProvinceEventsMinimized;
	}

	public boolean isInfoPaneProvinceScienceMinimized() {
		return isInfoPaneProvinceScienceMinimized;
	}

	public void setInfoPaneProvinceScienceMinimized(boolean isInfoPaneProvinceScienceMinimized) {
		this.isInfoPaneProvinceScienceMinimized = isInfoPaneProvinceScienceMinimized;
	}

	public boolean isInfoPaneCountryMinimized() {
		return isInfoPaneCountryMinimized;
	}

	public void setInfoPaneCountryMinimized(boolean isInfoPaneCountryMinimized) {
		this.isInfoPaneCountryMinimized = isInfoPaneCountryMinimized;
	}

	public boolean isInfoPaneProvinceArmiesMinimized() {
		return isInfoPaneProvinceArmiesMinimized;
	}

	public void setInfoPaneProvinceArmiesMinimized(boolean isInfoPaneProvinceArmiesMinimized) {
		this.isInfoPaneProvinceArmiesMinimized = isInfoPaneProvinceArmiesMinimized;
	}

}
