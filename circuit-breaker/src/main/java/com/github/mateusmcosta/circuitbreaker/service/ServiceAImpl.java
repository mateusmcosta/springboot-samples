package com.github.mateusmcosta.circuitbreaker.service;


import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import com.github.mateusmcosta.circuitbreaker.exception.BusinessException;
import io.vavr.control.Try;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static io.github.resilience4j.bulkhead.annotation.Bulkhead.Type;

/**
 * This Service shows how to use the CircuitBreaker annotation.
 */
@Component(value = "ServiceAImpl")
public class ServiceAImpl implements Service {

    private static Logger LOG = LoggerFactory.getLogger(ServiceAImpl.class);

    private static final String GROUP_A = "groupA";

    @Override
    @CircuitBreaker(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    @Retry(name = GROUP_A)
    public String failure() {
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception");
    }

    @Override
    @CircuitBreaker(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    public String ignoreException() {
        throw new BusinessException("This exception is ignored by the CircuitBreaker of service A Impl");
    }

    @Override
    @CircuitBreaker(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    @Retry(name = GROUP_A)
    public String success() {
        return "Hello World from service A Impl";
    }

    @Override
    @CircuitBreaker(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    public String successException() {
        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "This is a remote client exception");
    }

    @Override
    @CircuitBreaker(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    @Retry(name = GROUP_A)
    public Flux<String> fluxFailure() {
        return Flux.error(new IOException("BAM!"));
    }

    @Override
    @TimeLimiter(name = GROUP_A)
    @CircuitBreaker(name = GROUP_A, fallbackMethod = "fluxFallback")
    public Flux<String> fluxTimeout() {
        return Flux.
                just("Hello World from service A Impl")
                .delayElements(Duration.ofSeconds(10));
    }

    @Override
    @TimeLimiter(name = GROUP_A)
    @CircuitBreaker(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    @Retry(name = GROUP_A)
    public Mono<String> monoSuccess() {
        return Mono.just("Hello World from service A Impl");
    }

    @Override
    @CircuitBreaker(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    @Retry(name = GROUP_A)
    public Mono<String> monoFailure() {
        return Mono.error(new IOException("BAM!"));
    }

    @Override
    @TimeLimiter(name = GROUP_A)
    @Bulkhead(name = GROUP_A)
    @CircuitBreaker(name = GROUP_A, fallbackMethod = "monoFallback")
    public Mono<String> monoTimeout() {
        return Mono.just("Hello World from service A Impl")
                .delayElement(Duration.ofSeconds(10));
    }

    @Override
    @TimeLimiter(name = GROUP_A)
    @CircuitBreaker(name = GROUP_A)
    @Retry(name = GROUP_A)
    public Flux<String> fluxSuccess() {
        return Flux.just("Hello", "World");
    }

    @Override
    @CircuitBreaker(name = GROUP_A, fallbackMethod = "fallback")
    public String failureWithFallback() {
        return failure();
    }

    @Override
    @Bulkhead(name = GROUP_A, type = Type.THREADPOOL)
    @TimeLimiter(name = GROUP_A)
    @CircuitBreaker(name = GROUP_A)
    @Retry(name = GROUP_A)
    public CompletableFuture<String> futureSuccess() {
        LOG.info("Calling futureSuccess");
        return CompletableFuture.completedFuture("Hello World from service A Impl");
    }

    @Override
    @Bulkhead(name = GROUP_A, type = Type.THREADPOOL)
    @TimeLimiter(name = GROUP_A)
    @CircuitBreaker(name = GROUP_A)
    @Retry(name = GROUP_A)
    public CompletableFuture<String> futureFailure() {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new IOException("BAM!"));
        return future;
    }

    @Override
    @Bulkhead(name = GROUP_A, type = Type.THREADPOOL)
    @TimeLimiter(name = GROUP_A)
    @CircuitBreaker(name = GROUP_A, fallbackMethod = "futureFallback")
    public CompletableFuture<String> futureTimeout() {
        LOG.info("Calling futureTimeout");
        Try.run(() -> Thread.sleep(8000));
        return CompletableFuture.completedFuture("Hello World from service A Impl");
    }

    private String fallback(HttpServerErrorException ex) {
        return "Recovered HttpServerErrorException: " + ex.getMessage();
    }

    private String fallback(Exception ex) {
        return "Recovered: " + ex.toString();
    }

    private CompletableFuture<String> futureFallback(TimeoutException ex) {
         LOG.info("Calling futureFallback");
        return CompletableFuture.completedFuture("Recovered specific TimeoutException: " + ex.toString());
    }

    private CompletableFuture<String> futureFallback(BulkheadFullException ex) {
        return CompletableFuture.completedFuture("Recovered specific BulkheadFullException: " + ex.toString());
    }

    private CompletableFuture<String> futureFallback(CallNotPermittedException ex) {
        return CompletableFuture.completedFuture("Recovered specific CallNotPermittedException: " + ex.toString());
    }

    private Mono<String> monoFallback(Exception ex) {
        return Mono.just("Recovered: " + ex.toString());
    }

    private Flux<String> fluxFallback(Exception ex) {
        return Flux.just("Recovered: " + ex.toString());
    }
}
