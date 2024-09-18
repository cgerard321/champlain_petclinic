package com.petclinic.bffapigateway.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
// @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")  // Apply CORS settings to all endpoints
//                .allowedOrigins("http://localhost:3000")  // Allow requests from front-end
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")  // Allow PATCH method
//                .allowedHeaders("*")  // Allow all headers
//                .allowCredentials(true);  // Allow credentials (if needed)
//    }
//}