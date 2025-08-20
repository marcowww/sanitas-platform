package com.healthcare.staffing.orchestration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
public class ServiceProperties {
    
    private BookingService bookingService;
    private CarerService carerService;
    
    public BookingService getBookingService() {
        return bookingService;
    }
    
    public void setBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    
    public CarerService getCarerService() {
        return carerService;
    }
    
    public void setCarerService(CarerService carerService) {
        this.carerService = carerService;
    }
    
    public static class BookingService {
        private String baseUrl;
        private int timeout;
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
    
    public static class CarerService {
        private String baseUrl;
        private int timeout;
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public int getTimeout() {
            return timeout;
        }
        
        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}
