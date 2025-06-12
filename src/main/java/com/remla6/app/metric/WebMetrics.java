package com.remla6.app.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebMetrics {

    private final MeterRegistry registry;

    private Counter inferenceRequests;
    private Counter inferenceFailures;
    private AtomicInteger storedResponses;
    private Gauge storedResponsesGauge;
    private Timer inferenceLatency;
    private Counter cacheHits;

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

        inferenceLatency = Timer.builder("app_inference_latency_seconds")
                .description("Distribution of sentiment inference latency")
                .publishPercentileHistogram()
                .tags("application", "app")
                .register(registry);

        cacheHits = Counter.builder("app_cache_hits_total")
                .description("Total number of sentiment inferences served from cache")
                .tags("application", "app")
                .register(registry);
    }

    // Called at the start of inference
    public Timer.Sample startInferenceTimer() {
        inferenceRequests.increment();
        return Timer.start(registry);
    }

    // Called after inference completes (success or failure)
    public void stopInferenceTimer(Timer.Sample sample) {
        sample.stop(inferenceLatency);
    }

    // Increment cache hit counter
    public void recordCacheHit() {
        cacheHits.increment();
    }

    // On a caught exception during inference
    public void recordInferenceFailure() {
        inferenceFailures.increment();
    }

    // After you load all responses, update the gauge
    public void updateStoredResponsesCount(int count) {
        storedResponses.set(count);
    }
}
