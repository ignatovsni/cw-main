package com.cwsni.world.client.desktop;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Component
@JsonPropertyOrder({ "useAutoSave", "autoSaveTurnStep", "autoSaveMaxFiles", "multithreadingAIThreads" })
public class ApplicationSettings extends AbstractSettings {

	private boolean isUseAutoSave = true;
	private int autoSaveTurnStep = 10;
	private int autoSaveMaxFiles = 10;
	private int multithreadingAIThreads = 8;
	private int multithreadingMaxWaitAllTasksPerTurnSeconds = 2;
	private String language = "";
	private int scriptsMaxStackDeep = 10;
	private int aiScriptsPoolSize = 16;
	private int eventsScriptsPoolSize = 16;
	private int eventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns = 100;

	@Override
	@PostConstruct
	public void init() {
		filePath = "settings.ini";
		super.init();
	}

	public boolean isUseAutoSave() {
		return isUseAutoSave;
	}

	public void setUseAutoSave(boolean isUseAutoSave) {
		this.isUseAutoSave = isUseAutoSave;
	}

	public int getAutoSaveTurnStep() {
		return autoSaveTurnStep;
	}

	public void setAutoSaveTurnStep(int autoSaveTurnStep) {
		this.autoSaveTurnStep = Math.max(1, autoSaveTurnStep);
	}

	public int getAutoSaveMaxFiles() {
		return autoSaveMaxFiles;
	}

	public void setAutoSaveMaxFiles(int autoSaveMaxFiles) {
		this.autoSaveMaxFiles = Math.max(1, autoSaveMaxFiles);
	}

	public int getMultithreadingAIThreads() {
		return multithreadingAIThreads;
	}

	public void setMultithreadingAIThreads(int multithreadingAIThreads) {
		this.multithreadingAIThreads = Math.min(64, Math.max(1, multithreadingAIThreads));
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public int getAiScriptsPoolSize() {
		return aiScriptsPoolSize;
	}

	public void setAiScriptsPoolSize(int aiScriptsPoolSize) {
		this.aiScriptsPoolSize = aiScriptsPoolSize;
	}

	public int getScriptsMaxStackDeep() {
		return scriptsMaxStackDeep;
	}

	public void setScriptsMaxStackDeep(int scriptsMaxStackDeep) {
		this.scriptsMaxStackDeep = scriptsMaxStackDeep;
	}

	public int getEventsScriptsPoolSize() {
		return eventsScriptsPoolSize;
	}

	public void setEventsScriptsPoolSize(int eventsScriptsPoolSize) {
		this.eventsScriptsPoolSize = eventsScriptsPoolSize;
	}

	public int getMultithreadingMaxWaitAllTasksPerTurnSeconds() {
		return multithreadingMaxWaitAllTasksPerTurnSeconds;
	}

	public void setMultithreadingMaxWaitAllTasksPerTurnSeconds(int multithreadingMaxWaitAllTasksPerTurnSeconds) {
		this.multithreadingMaxWaitAllTasksPerTurnSeconds = multithreadingMaxWaitAllTasksPerTurnSeconds;
	}

	public int getEventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns() {
		return eventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns;
	}

	public void setEventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns(
			int eventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns) {
		this.eventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns = eventsRemoveWhileLoadingIfDidNotTouchMoreThanTurns;
	}

}
