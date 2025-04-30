package com.remla6.app.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ModelService {

    private final RestClient restClient;

    public ModelService(RestClient.Builder restClientBuilder,
                        @Value("${MODEL_URL}") String modelBaseURL) {
        this.restClient = restClientBuilder.baseUrl(modelBaseURL).build();
    }

}
