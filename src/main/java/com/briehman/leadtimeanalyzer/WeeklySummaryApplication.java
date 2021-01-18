package com.briehman.leadtimeanalyzer;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class WeeklySummaryApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(WeeklySummaryApplication.class)
                .profiles("weekly-summary")
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
