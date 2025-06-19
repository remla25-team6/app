package com.remla6.app.service;

import com.remla6.app.dto.PredictRequest;
import com.remla6.app.dto.PredictResponse;
import com.remla6.app.exception.InferenceFailedException;
import com.remla6.app.model.SentimentModel;
import com.remla6.app.repository.SentimentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class ModelService {

    private final RestClient restClient;
    private final SentimentRepository sentimentRepository;

    public ModelService(RestClient.Builder restClientBuilder,
                        SentimentRepository sentimentRepository,
                        @Value("${MODEL_URL}") String modelBaseURL) {
        this.restClient = restClientBuilder.baseUrl(modelBaseURL).build();
        this.sentimentRepository = sentimentRepository;
    }

    /**
     * Method invoced by the ModelController if a user submits a review to be analysed.
     *
     * @param text a String representing the review submitted by the user
     * @return SentimentModel containing the persisted result of the inference.
     * @throws InferenceFailedException if model-service inference fails. Caught & handled by Spring
     */
    public SentimentModel processSentiment(String text) throws InferenceFailedException {
        PredictRequest request = new PredictRequest(text);

        ResponseEntity<PredictResponse> response = restClient
                .post()
                .uri("/predict")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new InferenceFailedException("Model currently unavailable, try again later.");
                })
                .toEntity(PredictResponse.class);

        // Check if response = 200
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new InferenceFailedException("Model currently unavailable, try again later.");
        }

        String sentiment = response.getBody().getSentiment();
        SentimentModel model = new SentimentModel();
        model.setSentiment(sentiment);
        model.setInputString(text);

        return sentimentRepository.save(model);
    }

    public SentimentModel findById(Long id){
        return sentimentRepository.findById(id).orElse(null);
    }
    /**
     * Method to retrieve all inference results from the persistence repository.
     * @return List of SentinentModel
     */
    public List<SentimentModel> getAllPreviousResults(){
        return sentimentRepository.findAll();
    }

}
