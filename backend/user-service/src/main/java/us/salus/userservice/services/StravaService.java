package us.salus.userservice.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import us.salus.userservice.models.Athlete;

@Service
public class StravaService {
  private final static RestClient restClient = RestClient.builder().baseUrl("https://www.strava.com/api/v3").build();

  public static Athlete getAthlete(String token) {
    return restClient
        .get()
        .uri("/athlete")
        .header("Authorization", "Bearer " + token)
        .retrieve()
        .body(Athlete.class);
  }

}
