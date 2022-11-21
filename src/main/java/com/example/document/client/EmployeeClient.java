package com.example.document.client;

import com.example.document.exceptionHandler.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
@RequiredArgsConstructor
public class EmployeeClient {

    @Value("${employee.service.url}")
    private String employeeUrl;

    private final RestTemplate restTemplate;

    public EmployeeDTO getEmployeeById(long employeeId) {
        String url = fromHttpUrl(employeeUrl + "/employee/{employeeId}")
                .buildAndExpand(employeeId).toUriString();
        try {
            return restTemplate.getForObject(url, EmployeeDTO.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new EmployeeNotFoundException(exception.getStatusCode(), exception.getMessage());
        }

    }
}
