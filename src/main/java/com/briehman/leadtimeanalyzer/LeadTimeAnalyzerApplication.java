package com.briehman.leadtimeanalyzer;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LeadTimeAnalyzerApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(DataLoaderApplication.class)
                .run(args);
    }

    @Bean
    public JiraRestClient jiraRestClient(@Value("${jira.url}") String uri,
            @Value("${jira.credentials.username}") String username,
            @Value("${jira.credentials.password}") String password) {
        URI url;
        try {
            url = new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        return new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(url, username, password);
    }
}
