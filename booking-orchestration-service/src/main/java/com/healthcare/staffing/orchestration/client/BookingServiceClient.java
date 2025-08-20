package com.healthcare.staffing.orchestration.client;

import com.healthcare.staffing.orchestration.config.ServiceProperties;
import com.healthcare.staffing.orchestration.dto.BookCarerRequest;
import com.healthcare.staffing.orchestration.dto.PulloutCarerRequest;
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
import java.util.UUID;

@Component
public class BookingServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(BookingServiceClient.class);
    
    private final WebClient webClient;
    private final ServiceProperties serviceProperties;
    
    @Autowired
    public BookingServiceClient(WebClient.Builder webClientBuilder, ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
        this.webClient = webClientBuilder
            .baseUrl(serviceProperties.getBookingService().getBaseUrl())
            .build();
    }
    
    @CircuitBreaker(name = "booking-service", fallbackMethod = "bookCarerFallback")
    @Retry(name = "booking-service")
    public void bookCarer(UUID bookingId, UUID carerId, String bookedBy) {
        logger.info("Calling booking service to book carer {} for booking {}", carerId, bookingId);
        
        BookCarerRequest request = new BookCarerRequest(carerId, bookedBy);
        
        try {
            webClient.post()
                .uri("/api/bookings/{bookingId}/book", bookingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(serviceProperties.getBookingService().getTimeout()))
                .block();
                
            logger.info("Successfully booked carer {} for booking {}", carerId, bookingId);
        } catch (WebClientResponseException e) {
            logger.error("Booking service returned error {} for booking {}: {}", 
                e.getStatusCode(), bookingId, e.getResponseBodyAsString());
            throw new BookingServiceException("Failed to book carer: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error calling booking service for booking {}: {}", bookingId, e.getMessage());
            throw new BookingServiceException("Failed to book carer: " + e.getMessage(), e);
        }
    }
    
    @CircuitBreaker(name = "booking-service", fallbackMethod = "pulloutCarerFallback")
    @Retry(name = "booking-service")
    public void pulloutCarer(UUID bookingId, UUID carerId, String reason, String pulloutBy) {
        logger.info("Calling booking service to pullout carer {} from booking {}", carerId, bookingId);
        
        PulloutCarerRequest request = new PulloutCarerRequest(carerId, reason, pulloutBy);
        
        try {
            webClient.post()
                .uri("/api/bookings/{bookingId}/pullout", bookingId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(serviceProperties.getBookingService().getTimeout()))
                .block();
                
            logger.info("Successfully pulled out carer {} from booking {}", carerId, bookingId);
        } catch (WebClientResponseException e) {
            logger.error("Booking service returned error {} for booking {}: {}", 
                e.getStatusCode(), bookingId, e.getResponseBodyAsString());
            throw new BookingServiceException("Failed to pullout carer: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error calling booking service for booking {}: {}", bookingId, e.getMessage());
            throw new BookingServiceException("Failed to pullout carer: " + e.getMessage(), e);
        }
    }
    
    // Fallback methods
    public void bookCarerFallback(UUID bookingId, UUID carerId, String bookedBy, Exception ex) {
        logger.error("Booking service circuit breaker activated for booking {}: {}", bookingId, ex.getMessage());
        throw new BookingServiceException("Booking service is currently unavailable", ex);
    }
    
    public void pulloutCarerFallback(UUID bookingId, UUID carerId, String reason, String pulloutBy, Exception ex) {
        logger.error("Booking service circuit breaker activated for pullout booking {}: {}", bookingId, ex.getMessage());
        throw new BookingServiceException("Booking service is currently unavailable", ex);
    }
    
    public static class BookingServiceException extends RuntimeException {
        public BookingServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
