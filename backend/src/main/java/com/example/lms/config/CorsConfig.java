package com.example.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for the application.
 * This is the single source of truth for CORS settings used by both Spring Security and MVC.
 */
@Configuration
public class CorsConfig {
  
  /**
   * Builds the shared CORS configuration used across the application.
   * 
   * @return CorsConfiguration with allowed origins, methods, headers, and credentials
   */
  private CorsConfiguration buildCorsConfiguration() {
    CorsConfiguration config = new CorsConfiguration();
    
    config.setAllowCredentials(true);
    
    config.setAllowedOrigins(Arrays.asList(
        "http://localhost:4200",
        "http://127.0.0.1:4200",
        "http://localhost:5000",
        "http://localhost:8080",
        "http://192.168.8.116"
    ));
    
    config.setAllowedHeaders(List.of("*"));
    
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    
    config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
    
    return config;
  }
  
  /**
   * CORS configuration source bean used by Spring Security.
   * Registers CORS configuration for all paths.
   * 
   * @return CorsConfigurationSource for Spring Security
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", buildCorsConfiguration());
    return source;
  }
  
  @Bean
  public RestTemplate restTemplate() {
      return new RestTemplate();
  }
}
