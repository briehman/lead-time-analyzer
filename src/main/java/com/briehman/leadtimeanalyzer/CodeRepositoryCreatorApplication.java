package com.briehman.leadtimeanalyzer;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class CodeRepositoryCreatorApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(CodeRepositoryCreatorApplication.class)
                .profiles("code-repository")
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
