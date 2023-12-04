package com.example.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(WiremockApplicationImportTest.WireMockTestConfiguration.class)
@ContextConfiguration(classes = WiremockApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock
class WiremockApplicationImportTest {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void shouldPass() {
        // GIVEN
        WireMock.stubFor(get(
                urlPathEqualTo("/test"))
                .willReturn(aResponse()
                        .withBody("{ \"success\": \"true\" }")
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                ));

        // WHEN
        var response = restTemplate.getForEntity("http://localhost:5678/test", String.class);

        // THEN
        assertEquals(200, response.getStatusCode().value());
        assertEquals("{ \"success\": \"true\" }", response.getBody());
    }

    @Test
    void shouldFail() {
        // GIVEN
        WireMock.stubFor(get(
                urlPathEqualTo("/test"))
                .willReturn(aResponse()
                        .withBody("{ \"success\": \"true\" }")
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                ));

        // WHEN
        var exception = assertThrows(ResourceAccessException.class, () -> restTemplate.getForEntity("http://localhost:8080/test", String.class));

        // THEN
        assertNotNull(exception);
    }

    static class WireMockTestConfiguration {
        @Bean
        WireMockConfigurationCustomizer optionsCustomizer() {
            return config -> config.port(5678);
        }
    }

}
