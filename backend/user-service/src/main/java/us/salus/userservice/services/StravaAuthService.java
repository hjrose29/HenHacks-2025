package us.salus.userservice.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import us.salus.userservice.models.AuthCodeRequest;
import us.salus.userservice.models.TokenResponse;

@Service
public class StravaAuthService {
  private final static RestClient restClient = RestClient.builder().build();

  public static TokenResponse getToken(String code) {
    return restClient
        .post()
        .uri("https://www.strava.com/oauth/token")
        .body(new AuthCodeRequest(code))
        .retrieve()
        .body(TokenResponse.class);
  }
}
