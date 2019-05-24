package com.cwsni.world.settings;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

abstract public class AbstractSettings {

	protected final Log logger = LogFactory.getLog(getClass());

	protected String filePath;

	public void init() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.readerForUpdating(this).readValue(new File(filePath));
		} catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace("Failed to load " + filePath, ex);
			} else {
				logger.info("Failed to load " + filePath);
			}
		}
	}

	public void savePropertiesToFile() {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			objectMapper.writeValue(new File(filePath), this);
		} catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace("Failed to save " + filePath, ex);
			} else {
				logger.info("Failed to save " + filePath);
			}
		}
	}
}