package com.example.document.client;

import com.example.document.exceptionHandler.BenefitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class BenefitClient {

    @Value("${benefit.service.url}")
    private String benefitUrl;


    public BenefitDTO findBenefitDtoById(Long id) {

        WebClient client = WebClient.create();

        WebClient.ResponseSpec responseSpec = client.get()
                .uri(benefitUrl + "/benefit/" + id)
                .retrieve();
        try {
            return responseSpec.bodyToMono(BenefitDTO.class).block();
        } catch (WebClientResponseException.NotFound exception) {
            throw new BenefitNotFoundException(exception.getMessage(), 404, exception.getStatusText(), exception.getHeaders(), exception.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
        }
    }
}
