package com.grapheople;

import com.google.adk.web.AdkWebServer;
import com.grapheople.config.CustomConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
//@Import(CustomConfig.class)
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
//        AdkWebServer.main(args);
    }
}
