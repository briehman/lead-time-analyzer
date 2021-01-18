package com.briehman.leadtimeanalyzer;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class DataLoaderApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(DataLoaderApplication.class)
                .profiles("data-loader")
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
