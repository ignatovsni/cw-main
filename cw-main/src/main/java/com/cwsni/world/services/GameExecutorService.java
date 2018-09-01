package com.cwsni.world.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

@Component
public class GameExecutorService {

	private ExecutorService aiExecutorService;

	@PostConstruct
	private void init() {
		aiExecutorService = Executors.newSingleThreadExecutor();
	}

	@PreDestroy
	private void destroy() {
		aiExecutorService.shutdownNow();
	}

	public void processAI(Runnable task) {
		aiExecutorService.submit(task);
	}

}
