package com.cwsni.world.services;

import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.cwsni.world.settings.ApplicationSettings;

@Component
public class GameExecutorService {

	@Autowired
	private ApplicationSettings applicationSettings;

	private ThreadPoolTaskExecutor gameHandlerThreadExecutor;
	private ThreadPoolTaskExecutor aiTaskExecutor;

	@PostConstruct
	private void init() {
		gameHandlerThreadExecutor = new ThreadPoolTaskExecutor();
		gameHandlerThreadExecutor.initialize();
		gameHandlerThreadExecutor.setCorePoolSize(1);
		gameHandlerThreadExecutor.setMaxPoolSize(1);
		gameHandlerThreadExecutor.setQueueCapacity(1);
		gameHandlerThreadExecutor.setDaemon(true);
		gameHandlerThreadExecutor.setThreadGroupName("gameHandlerThreadGroup");
		gameHandlerThreadExecutor.setThreadNamePrefix("gameHandlerThread");

		aiTaskExecutor = new ThreadPoolTaskExecutor();
		aiTaskExecutor.initialize();
		aiTaskExecutor.setCorePoolSize(applicationSettings.getMultithreadingAIThreads());
		aiTaskExecutor.setMaxPoolSize(applicationSettings.getMultithreadingAIThreads());
		aiTaskExecutor.setQueueCapacity(10000);
		aiTaskExecutor.setDaemon(true);
		aiTaskExecutor.setThreadGroupName("aiTaskThreadGroup");
		aiTaskExecutor.setThreadNamePrefix("aiTaskThread");
	}

	@PreDestroy
	public void stop() {
		gameHandlerThreadExecutor.shutdown();
		aiTaskExecutor.shutdown();
	}

	public Future<?> processOneAI(Runnable task) {
		return aiTaskExecutor.submit(task);
	}

	public Future<?> processManagerThread(Runnable task) {
		return gameHandlerThreadExecutor.submit(task);
	}

}
