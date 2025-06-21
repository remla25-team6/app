package com.remla6.app.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebMetrics {

    private final MeterRegistry registry;

    private Counter inferenceRequests;
    private Counter inferenceFailures;
    private AtomicInteger storedResponses;
    private Gauge storedResponsesGauge;
    private Gauge storedResponsesGaugePos;
    private Gauge storedResponsesGaugeNeg;


    // // For labeled metrics
    private final Map<String, AtomicInteger> storedResponsesBySentiment = new ConcurrentHashMap<>();
    private final Map<String, Timer> latencyTimers = new ConcurrentHashMap<>();

    public WebMetrics(MeterRegistry registry) { // If IDE complains abt non-existent bean it can be ignored.
        this.registry = registry;
    }

    @PostConstruct
    public void initMetrics() {
        inferenceRequests = Counter.builder("app_inference_requests_total")
                .description("Total number of sentiment inferences attempted")
                .tags("application", "app")
                .register(registry);

        inferenceFailures = Counter.builder("app_inference_failures_total")
                .description("Total number of failed sentiment inferences")
                .tags("application", "app")
                .register(registry);

        storedResponses = new AtomicInteger(0);
        storedResponsesGauge = Gauge.builder("app_stored_responses_current", storedResponses, AtomicInteger::get)
                .description("Current number of stored sentiment responses")
                .tags("application", "app")
                .register(registry);

        // Stored response labels by sentiment (positive/negative)
        storedResponsesGaugePos = Gauge.builder("app_stored_responses_by_sentiment_current",
                      storedResponsesBySentiment.computeIfAbsent("pos", k -> new AtomicInteger(0)),
                      AtomicInteger::get)
                .description("Current number of stored responses, by sentiment")
                .tag("sentiment", "pos")
                .register(registry);

        storedResponsesGaugeNeg = Gauge.builder("app_stored_responses_by_sentiment_current",
                      storedResponsesBySentiment.computeIfAbsent("neg", k -> new AtomicInteger(0)),
                      AtomicInteger::get)
                .description("Current number of stored responses, by sentiment")
                .tag("sentiment", "neg")
                .register(registry);
    }
    
    public void recordSuccessfulInference(String sentiment) {
        Counter.builder("app_inference_success_total")
                .description("Total number of successful inferences, labeled by sentiment.")
                .tag("sentiment", sentiment)
                .register(registry)
                .increment();
    }

    // Called at the start of inference
    public Timer.Sample startInferenceTimer() {
        inferenceRequests.increment();
        return Timer.start(registry);
    }

    // Called after inference completes (success or failure)
    public void stopInferenceTimer(Timer.Sample sample, String outcome) {
        Timer timer = latencyTimers.computeIfAbsent(outcome, o ->
                Timer.builder("app_inference_latency_seconds")
                        .description("Distribution of sentiment inference latency")
                        .publishPercentileHistogram()
                        .tag("outcome", o) // The label for this timer
                        .register(registry)
        );
        sample.stop(timer);
    }

    // On a caught exception during inference
    public void recordInferenceFailure() {
        inferenceFailures.increment();
    }

    // After you load all responses, update the gauge
    public void updateStoredResponsesCount(int count) {
        storedResponses.set(count);
    }
    // Store/update sentiment counter
    public void updateStoredResponsesBySentiment(long positiveCount, long negativeCount) {
    storedResponsesBySentiment.get("pos").set((int) positiveCount);
    storedResponsesBySentiment.get("neg").set((int) negativeCount);
    }
}
