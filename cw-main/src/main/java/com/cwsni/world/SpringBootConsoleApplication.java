package com.cwsni.world;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.cwsni.world.client.desktop.GraphicsScalingScrollingApp;

@SpringBootApplication
//@ComponentScan
//@Configuration
//@EnableAutoConfiguration
public class SpringBootConsoleApplication implements CommandLineRunner {
	
	private static final Log logger = LogFactory.getLog(SpringBootConsoleApplication.class);

    public static void main(String[] args) throws Exception {

        SpringApplication app = new SpringApplication(SpringBootConsoleApplication.class);
        //app.setAddCommandLineProperties(false);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setBannerMode(Banner.Mode.LOG);
        app.run(args);
        // test change - 5
        // git@github.com:ignatovsni/cw-main.git

    }

    @Override
    public void run(String... args) throws Exception {
    	logger.info("my info log, yeah!");
    	GraphicsScalingScrollingApp mainWindow = new GraphicsScalingScrollingApp();
    	mainWindow.main(args);
    	logger.info("Memory: " + Runtime.getRuntime().totalMemory());
    }
}
