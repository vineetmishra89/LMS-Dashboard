package com.example.lms.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GraphApiService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;


  public GraphApiService() {
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
  }

  public boolean validateToken(String accessToken) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(
        "https://graph.microsoft.com/v1.0/me",
        HttpMethod.GET,
        entity,
        String.class
      );

      return response.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      return false;
    }
  }



  public JsonNode getUserInfo(String accessToken) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<String> response = restTemplate.exchange(
        "https://graph.microsoft.com/v1.0/me",
        HttpMethod.GET,
        entity,
        String.class
      );

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return objectMapper.readTree(response.getBody());
      }

      return null;
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch user info from Graph API: " + e.getMessage());
    }
  }
}
