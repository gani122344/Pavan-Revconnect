package org.revature.revconnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AiConfig {

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ai.ollama.model:llama3.2}")
    private String defaultModel;

    @Value("${ai.ollama.timeout:60}")
    private int timeoutSeconds;

    @Bean
    public WebClient ollamaWebClient() {
        return WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }
}
