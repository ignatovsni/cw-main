package com.cwsni.world.client.desktop;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Component
public class UserPreferences {

	private static final Log logger = LogFactory.getLog(UserPreferences.class);

	private static final String USER_PROP_FILE_NAME = "user.properties";

	private static final String MAIN_WINDOW_HEIGHT = "main.window.height";
	private static final String MAIN_WINDOW_WIDTH = "main.window.width";
	private static final String MAIN_WINDOW_POS_X = "main.window.pos.x";
	private static final String MAIN_WINDOW_POS_Y = "main.window.pos.y";
	private static final String MAIN_WINDOW_MAXIMIZED = "main.window.maximized";
	private static final String TIME_CONTROL_AUTO_TURN = "time-control.auto-turn";
	private static final String TIME_CONTROL_PAUSE_BETWEEN_TURNS = "time-control.pause-between-turns";
	private static final String INFO_PANE_GLOBALE_MINIMIZED = "info.pane.global.minimized";
	private static final String INFO_PANE_PROVINCE_MINIMIZED = "info.pane.province.minimized";
	private static final String INFO_PANE_PROVINCE_SCIENCE_MINIMIZED = "info.pane.province-science.minimized";
	private static final String INFO_PANE_PROVINCE_EVENTS_MINIMIZED = "info.pane.province-events.minimized";

	private Properties props;

	@PostConstruct
	public void init() {
		props = new Properties();
		try {
			FileInputStream in = new FileInputStream(USER_PROP_FILE_NAME);
			props.load(in);
			in.close();
		} catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace("Failed to load " + USER_PROP_FILE_NAME, ex);
			} else {
				logger.info("Failed to load " + USER_PROP_FILE_NAME);
			}
		}
		setDefaultProps();
	}

	private void setDefaultProps() {
		setDefaultProp(MAIN_WINDOW_HEIGHT, "600");
		setDefaultProp(MAIN_WINDOW_WIDTH, "800");
		setDefaultProp(MAIN_WINDOW_POS_X, "100");
		setDefaultProp(MAIN_WINDOW_POS_Y, "100");
		setDefaultProp(MAIN_WINDOW_MAXIMIZED, "0");
		setDefaultProp(TIME_CONTROL_AUTO_TURN, "1");
		setDefaultProp(TIME_CONTROL_PAUSE_BETWEEN_TURNS, "1");
		setDefaultProp(INFO_PANE_GLOBALE_MINIMIZED, "0");
		setDefaultProp(INFO_PANE_PROVINCE_MINIMIZED, "0");
		setDefaultProp(INFO_PANE_PROVINCE_SCIENCE_MINIMIZED, "0");
		setDefaultProp(INFO_PANE_PROVINCE_EVENTS_MINIMIZED, "0");
	}

	private void setDefaultProp(String code, String value) {
		if (!props.containsKey(code)) {
			props.setProperty(code, value);
		}
	}

	private void setProp(String code, Object value) {
		if (value != null) {
			props.setProperty(code, value.toString());
		} else {
			props.setProperty(code, null);
		}
	}

	private Integer getIntegerOrNull(String code) {
		if (!props.containsKey(code)) {
			return null;
		}
		String value = props.getProperty(code);
		if (value == null) {
			return null;
		}
		return Integer.valueOf(value);
	}

	public void saveUserProperties() {
		try {
			FileOutputStream out = new FileOutputStream(USER_PROP_FILE_NAME);
			props.store(out, "User preferences");
			out.close();
		} catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace("Failed to save " + USER_PROP_FILE_NAME, ex);
			} else {
				logger.info("Failed to save " + USER_PROP_FILE_NAME);
			}
		}
	}

	public Integer getMainWindowWidth() {
		return getIntegerOrNull(MAIN_WINDOW_WIDTH);
	}

	public Integer getMainWindowHeight() {
		return getIntegerOrNull(MAIN_WINDOW_HEIGHT);
	}

	public Integer getMainWindowPosX() {
		return getIntegerOrNull(MAIN_WINDOW_POS_X);
	}

	public Integer getMainWindowPosY() {
		return getIntegerOrNull(MAIN_WINDOW_POS_Y);
	}

	public boolean isMainWindowMaximized() {
		return getIntegerOrNull(MAIN_WINDOW_MAXIMIZED) == 1;
	}

	public boolean isTimeControlAutoTurn() {
		return getIntegerOrNull(TIME_CONTROL_AUTO_TURN) == 1;
	}

	public boolean isTimeControlPauseBetweenTurns() {
		return getIntegerOrNull(TIME_CONTROL_PAUSE_BETWEEN_TURNS) == 1;
	}

	public void setMainWindowWidth(double v) {
		setProp(MAIN_WINDOW_WIDTH, (int) v);
	}

	public void setMainWindowHeight(double v) {
		setProp(MAIN_WINDOW_HEIGHT, (int) v);
	}

	public void setMainWindowPositionX(double v) {
		setProp(MAIN_WINDOW_POS_X, (int) v);
	}

	public void setMainWindowPositionY(double v) {
		setProp(MAIN_WINDOW_POS_Y, (int) v);
	}

	public void setMainWindowMaximazed(boolean maximized) {
		setProp(MAIN_WINDOW_MAXIMIZED, maximized ? 1 : 0);
	}

	public void setTimeControlAutoTurn(boolean v) {
		setProp(TIME_CONTROL_AUTO_TURN, v ? 1 : 0);
	}

	public void setTimeControlPauseBetweenTurns(boolean v) {
		setProp(TIME_CONTROL_PAUSE_BETWEEN_TURNS, v ? 1 : 0);
	}

	public void setInfoPaneGlobalMinimized(boolean v) {
		setProp(INFO_PANE_GLOBALE_MINIMIZED, v ? 1 : 0);
	}

	public boolean isInfoPaneGlobalMinimized() {
		return getIntegerOrNull(INFO_PANE_GLOBALE_MINIMIZED) == 1;
	}

	public void setInfoPaneProvinceMinimized(boolean v) {
		setProp(INFO_PANE_PROVINCE_MINIMIZED, v ? 1 : 0);
	}

	public boolean isInfoPaneProvinceMinimized() {
		return getIntegerOrNull(INFO_PANE_PROVINCE_MINIMIZED) == 1;
	}

	public void setInfoPaneProvinceEventsMinimized(boolean v) {
		setProp(INFO_PANE_PROVINCE_EVENTS_MINIMIZED, v ? 1 : 0);
	}

	public boolean isInfoPaneProvinceEventsMinimized() {
		return getIntegerOrNull(INFO_PANE_PROVINCE_EVENTS_MINIMIZED) == 1;
	}
	
	public void setInfoPaneProvinceScienceMinimized(boolean v) {
		setProp(INFO_PANE_PROVINCE_SCIENCE_MINIMIZED, v ? 1 : 0);
	}

	public boolean isInfoPaneProvinceScienceMinimized() {
		return getIntegerOrNull(INFO_PANE_PROVINCE_SCIENCE_MINIMIZED) == 1;
	}

}
