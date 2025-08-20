package com.healthcare.staffing.orchestration.client;

import com.healthcare.staffing.orchestration.config.ServiceProperties;
import com.healthcare.staffing.orchestration.dto.BlockAvailabilityRequest;
import com.healthcare.staffing.orchestration.dto.UnblockAvailabilityRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class CarerServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(CarerServiceClient.class);
    
    private final WebClient webClient;
    private final ServiceProperties serviceProperties;
    
    @Autowired
    public CarerServiceClient(WebClient.Builder webClientBuilder, ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
        this.webClient = webClientBuilder
            .baseUrl(serviceProperties.getCarerService().getBaseUrl())
            .build();
    }
    
    @CircuitBreaker(name = "carer-service", fallbackMethod = "blockAvailabilityFallback")
    @Retry(name = "carer-service")
    public void blockAvailability(UUID carerId, UUID bookingId, LocalDateTime startTime, 
                                 LocalDateTime endTime, String blockedBy) {
        logger.info("Calling carer service to block availability for carer {} for booking {}", carerId, bookingId);
        
        BlockAvailabilityRequest request = new BlockAvailabilityRequest(bookingId, startTime, endTime, blockedBy);
        
        try {
            webClient.put()
                .uri("/api/carers/{carerId}/availability/block", carerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(serviceProperties.getCarerService().getTimeout()))
                .block();
                
            logger.info("Successfully blocked availability for carer {} for booking {}", carerId, bookingId);
        } catch (WebClientResponseException e) {
            logger.error("Carer service returned error {} for carer {}: {}", 
                e.getStatusCode(), carerId, e.getResponseBodyAsString());
            throw new CarerServiceException("Failed to block carer availability: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error calling carer service for carer {}: {}", carerId, e.getMessage());
            throw new CarerServiceException("Failed to block carer availability: " + e.getMessage(), e);
        }
    }
    
    @CircuitBreaker(name = "carer-service", fallbackMethod = "unblockAvailabilityFallback")
    @Retry(name = "carer-service")
    public void unblockAvailability(UUID carerId, UUID bookingId, String unblockedBy) {
        logger.info("Calling carer service to unblock availability for carer {} for booking {}", carerId, bookingId);
        
        UnblockAvailabilityRequest request = new UnblockAvailabilityRequest(bookingId, unblockedBy);
        
        try {
            webClient.put()
                .uri("/api/carers/{carerId}/availability/unblock", carerId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(serviceProperties.getCarerService().getTimeout()))
                .block();
                
            logger.info("Successfully unblocked availability for carer {} for booking {}", carerId, bookingId);
        } catch (WebClientResponseException e) {
            logger.error("Carer service returned error {} for carer {}: {}", 
                e.getStatusCode(), carerId, e.getResponseBodyAsString());
            throw new CarerServiceException("Failed to unblock carer availability: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error calling carer service for carer {}: {}", carerId, e.getMessage());
            throw new CarerServiceException("Failed to unblock carer availability: " + e.getMessage(), e);
        }
    }
    
    // Fallback methods
    public void blockAvailabilityFallback(UUID carerId, UUID bookingId, LocalDateTime startTime, 
                                         LocalDateTime endTime, String blockedBy, Exception ex) {
        logger.error("Carer service circuit breaker activated for carer {}: {}", carerId, ex.getMessage());
        throw new CarerServiceException("Carer service is currently unavailable", ex);
    }
    
    public void unblockAvailabilityFallback(UUID carerId, UUID bookingId, String unblockedBy, Exception ex) {
        logger.error("Carer service circuit breaker activated for carer {}: {}", carerId, ex.getMessage());
        throw new CarerServiceException("Carer service is currently unavailable", ex);
    }
    
    public static class CarerServiceException extends RuntimeException {
        public CarerServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
