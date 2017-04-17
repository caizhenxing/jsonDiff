package com.waez.jsondiff.functional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.waez.jsondiff.JsondiffApplication;
import com.waez.jsondiff.controller.JsonDiffController;

import dto.DiffResponseDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JsondiffApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationIT {

    @Autowired
    private JsonDiffController jsonDiffController;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testContexLoads() throws Exception {
        assertThat(jsonDiffController).isNotNull();
    }

    @Test
    public void testBothParts() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{ \"binary\": \"YWFhYWFh\" }", headers);

        // Post to put data for the left side
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/v1/diff/1/left", entity, String.class);

        String response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        entity = new HttpEntity<>("{ \"binary\": \"YWFhYWJi\" }", headers);

        // Post to put data for the right side
        responseEntity = restTemplate.postForEntity("/v1/diff/1/right", entity, String.class);
        response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        // Then we get the difference
        ResponseEntity<DiffResponseDTO> diffResponse = restTemplate.getForEntity("/v1/diff/1", DiffResponseDTO.class);

        assertThat(diffResponse).isNotNull();
        assertThat(diffResponse.getStatusCode()).isNotNull().isEqualTo(HttpStatus.OK);
        assertThat(diffResponse.getBody()).isNotNull();
        assertThat(diffResponse.getBody().getMessage()).isNotEmpty().isEqualTo("Diff successfully calculated");
        assertThat(diffResponse.getBody().getLines()).isNotEmpty().size().isEqualTo(1);
        assertThat(diffResponse.getBody().getLines().stream().filter(x -> x.getOffset() == 4 && x.getLength() == 2)
                .collect(Collectors.toList())).isNotEmpty();
    }

    @Test
    public void testBothPartsSameContent() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{ \"binary\": \"YWFhYWFh\" }", headers);

        // Post to put data for the left side
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/v1/diff/2/left", entity, String.class);

        String response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        entity = new HttpEntity<>("{ \"binary\": \"YWFhYWFh\" }", headers);

        // Post to put data for the right side
        responseEntity = restTemplate.postForEntity("/v1/diff/2/right", entity, String.class);
        response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        // Then we get the difference
        ResponseEntity<DiffResponseDTO> diffResponse = restTemplate.getForEntity("/v1/diff/2", DiffResponseDTO.class);

        assertThat(diffResponse).isNotNull();
        assertThat(diffResponse.getStatusCode()).isNotNull().isEqualTo(HttpStatus.OK);
        assertThat(diffResponse.getBody()).isNotNull();
        assertThat(diffResponse.getBody().getMessage()).isNotEmpty().isEqualTo("Data is exactly the same");
        assertThat(diffResponse.getBody().getLines()).isEmpty();
    }

    @Test
    public void testBothPartsDiferentSizes() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{ \"binary\": \"YWFhYWFh\" }", headers);

        // Post to put data for the left side
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/v1/diff/3/left", entity, String.class);

        String response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        entity = new HttpEntity<>("{ \"binary\": \"YWFhYWJiZGFzYWRhZHNhc2Q=\" }", headers);

        // Post to put data for the right side
        responseEntity = restTemplate.postForEntity("/v1/diff/3/right", entity, String.class);
        response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        // Then we get the difference
        ResponseEntity<DiffResponseDTO> diffResponse = restTemplate.getForEntity("/v1/diff/3", DiffResponseDTO.class);

        assertThat(diffResponse).isNotNull();
        assertThat(diffResponse.getStatusCode()).isNotNull().isEqualTo(HttpStatus.OK);
        assertThat(diffResponse.getBody()).isNotNull();
        assertThat(diffResponse.getBody().getMessage()).isNotEmpty().isEqualTo("Data is NOT the same size");
        assertThat(diffResponse.getBody().getLines()).isEmpty();
    }

    @Test
    public void testOnePart() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{ \"binary\": \"YWFhYWFh\" }", headers);

        // Post to put data for the left side
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/v1/diff/4/left", entity, String.class);

        String response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        // Then we get the difference
        ResponseEntity<DiffResponseDTO> diffResponse = restTemplate.getForEntity("/v1/diff/4", DiffResponseDTO.class);

        assertThat(diffResponse).isNotNull();
        assertThat(diffResponse.getStatusCode()).isNotNull().isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(diffResponse.getBody()).isNotNull();
        assertThat(diffResponse.getBody().getMessage()).isNotEmpty()
                .contains("One of the parts to compare with are missing with id: ");
    }

    @Test
    public void testNoParts() {
        // Then we get the difference
        ResponseEntity<DiffResponseDTO> diffResponse = restTemplate.getForEntity("/v1/diff/5", DiffResponseDTO.class);

        assertThat(diffResponse).isNotNull();
        assertThat(diffResponse.getStatusCode()).isNotNull().isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(diffResponse.getBody()).isNotNull();
        assertThat(diffResponse.getBody().getMessage()).isNotEmpty().contains("Nothign to compare with id: ");
    }

    @Test
    public void testBothPartsMultipleLines() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(
                "{ \"binary\": \"YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFiDQphYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWENCmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ0KYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYmFhYWFhYWFhYWFhDQphYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWENCmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFiYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ0KYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhDQphYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWENCmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ0KYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFiYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFh\" }",
                headers);

        // Post to put data for the left side
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("/v1/diff/6/left", entity, String.class);

        String response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        entity = new HttpEntity<>(
                "{ \"binary\": \"YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWF6DQphYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWENCmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ0KYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhDQpiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmJiYmINCmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ0KYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhDQphYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhemFhYWFhYWFhYWFhYWFhYWENCmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYQ0KYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFjYWFhYWFhYWFhYWFh\" }",
                headers);

        // Post to put data for the right side
        responseEntity = restTemplate.postForEntity("/v1/diff/6/right", entity, String.class);
        response = responseEntity.getBody();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isNotNull().isEqualTo(HttpStatus.CREATED);
        assertThat(response).isNotEmpty().contains("Part of comparision added with id: ");

        // Then we get the difference
        ResponseEntity<DiffResponseDTO> diffResponse = restTemplate.getForEntity("/v1/diff/6", DiffResponseDTO.class);

        assertThat(diffResponse).isNotNull();
        assertThat(diffResponse.getStatusCode()).isNotNull().isEqualTo(HttpStatus.OK);
        assertThat(diffResponse.getBody()).isNotNull();
        assertThat(diffResponse.getBody().getMessage()).isNotEmpty().isEqualTo("Diff successfully calculated");
        assertThat(diffResponse.getBody().getLines()).isNotEmpty().size().isEqualTo(7);
    }
}
