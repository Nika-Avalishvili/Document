package com.example.document.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

        return restTemplate.getForObject(url, EmployeeDTO.class);
    }
}
