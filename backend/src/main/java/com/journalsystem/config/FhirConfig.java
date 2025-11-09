package com.journalsystem.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {
    
    @Value("${fhir.server.base-url:https://hapi-fhir.app.cloud.cbh.kth.se/fhir}")
    private String fhirServerBaseUrl;
    
    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }
    
    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        return fhirContext.newRestfulGenericClient(fhirServerBaseUrl);
    }
}