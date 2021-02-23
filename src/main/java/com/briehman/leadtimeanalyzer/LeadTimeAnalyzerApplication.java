package com.briehman.leadtimeanalyzer;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class LeadTimeAnalyzerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(DataLoaderApplication.class)
                .run(args);
    }

    @Bean
    @Scope("singleton")
    public Supplier<JiraRestClient> jiraRestClient(Environment environment) {
        return () ->  {
            String uri = environment.getProperty("jira.url");
            String username = environment.getProperty("jira.credentials.username");
            String password = environment.getProperty("jira.credentials.password");

            URI url;
            try {
                url = new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }

            return new AsynchronousJiraRestClientFactory()
                    .createWithBasicHttpAuthentication(url, username, password);
        };
    }
}
