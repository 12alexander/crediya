package co.com.bancolombia.api.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Validation configuration for the reactive web layer.
 * Provides Jakarta Validation beans for request validation.
 * 
 * @author Crediya Development Team
 */
@Configuration
public class ValidationConfig {

    /**
     * Creates a Jakarta Validation Validator bean.
     * 
     * @return Validator instance for bean validation
     */
    @Bean
    public Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}