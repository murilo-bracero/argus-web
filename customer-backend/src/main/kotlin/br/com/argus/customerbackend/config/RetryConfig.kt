package br.com.argus.customerbackend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.support.RetryTemplate

@Configuration
class RetryConfig(
    @Value("\${retry.max-attempts}") private val maxAttempts: Int,
    @Value("\${retry.backoff}") private val backoff: Long,
    @Value("\${retry.backoffMultiplier}") private val backoffMultiplier: Double,
) {

    @Bean
    fun retryTemplate(): RetryTemplate{
        return RetryTemplate.builder()
            .maxAttempts(maxAttempts)
            .exponentialBackoff(backoff, backoffMultiplier, backoff * maxAttempts)
            .build();
    }

}