package com.remla6.app.service;

import com.remla6.app.dto.PredictRequest;
import com.remla6.app.dto.PredictResponse;
import com.remla6.app.exception.InferenceFailedException;
import com.remla6.app.model.SentimentModel;
import com.remla6.app.repository.SentimentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ModelServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    // these are the fluent interfaces returned by restClient.post(), .uri(), .body(), .retrieve(), .onStatus()
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private SentimentRepository sentimentRepository;

    private ModelService modelService;

    @BeforeEach
    void setUp() {
        // Make the builder return restClient when baseUrl(...).build() is called
        String baseUrl = "http://model";
        given(restClientBuilder.baseUrl(baseUrl)).willReturn(restClientBuilder);
        given(restClientBuilder.build()).willReturn(restClient);

        // Chain the fluent REST calls
        given(restClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri("/predict")).willReturn(requestBodySpec);
        given(requestBodySpec.body(any(PredictRequest.class))).willReturn(requestBodySpec);
        given(requestBodySpec.retrieve()).willReturn(responseSpec);
        // onStatus should by default return the same responseSpec (no error)
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);

        // Re-create the service WITH our baseUrl
        modelService = new ModelService(restClientBuilder, sentimentRepository, baseUrl);
    }

    @Test
    void processSentiment_successfulResponse_savesAndReturnsModel() {
        // given
        String input = "I love this!";
        PredictResponse predictResponse = new PredictResponse("pos");
        ResponseEntity<PredictResponse> fakeResponse =
                new ResponseEntity<>(predictResponse, HttpStatus.OK);

        given(responseSpec.toEntity(PredictResponse.class)).willReturn(fakeResponse);

        // stub repository.save(...) to echo back the saved entity
        given(sentimentRepository.save(any(SentimentModel.class)))
                .willAnswer(invocation -> invocation.getArgument(0, SentimentModel.class));

        // when
        SentimentModel result = modelService.processSentiment(input);

        // then
        assertThat(result.getInputString()).isEqualTo(input);
        assertThat(result.getSentiment()).isEqualTo("pos");
        then(responseSpec).should().toEntity(PredictResponse.class);
        then(sentimentRepository).should().save(any(SentimentModel.class));
    }

    @Test
    void processSentiment_nullBody_throwsInferenceFailed() {
        // given: response entity with null body
        ResponseEntity<PredictResponse> nullResponse =
                new ResponseEntity<>(null, HttpStatus.OK);
        given(responseSpec.toEntity(PredictResponse.class)).willReturn(nullResponse);

        // when / then
        assertThatThrownBy(() -> modelService.processSentiment("oops"))
                .isInstanceOf(InferenceFailedException.class)
                .hasMessageContaining("Model currently unavailable");
    }

    @Test
    void processSentiment_errorStatus_throwsInferenceFailed() {
        // simulate onStatus triggering by having onStatus(...) return a responseSpec
        // but then toEntity itself throws the exception mapped by onStatus
        InferenceFailedException failure = new InferenceFailedException("down");
        willThrow(failure)
                .given(responseSpec)
                .toEntity(PredictResponse.class);

        // when / then
        assertThatThrownBy(() -> modelService.processSentiment("test"))
                .isSameAs(failure);
    }

    @Test
    void getAllPreviousResults_returnsRepositoryData() {
        // given
        SentimentModel a = new SentimentModel("foo", "pos");
        SentimentModel b = new SentimentModel("bar", "neg");
        List<SentimentModel> list = Arrays.asList(a, b);
        given(sentimentRepository.findAll()).willReturn(list);

        // when
        List<SentimentModel> result = modelService.getAllPreviousResults();

        // then
        assertThat(result).containsExactly(a, b);
        then(sentimentRepository).should().findAll();
    }
}