package com.cwsni.world.client.desktop;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Component
@JsonPropertyOrder({ "useAutoSave", "autoSaveTurnStep", "autoSaveMaxFiles", "multithreadingAIThreads" })
public class ApplicationSettings extends AbstractSettings {

	private boolean isUseAutoSave = true;
	private int autoSaveTurnStep = 10;
	private int autoSaveMaxFiles = 10;
	private int multithreadingAIThreads = 8;
	private String language = "";

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

	@JsonIgnore
	public int getAIScriptsPoolSize() {
		return getMultithreadingAIThreads() * 10;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
