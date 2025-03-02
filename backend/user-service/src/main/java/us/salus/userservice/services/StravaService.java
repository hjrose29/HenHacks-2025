package us.salus.userservice.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import us.salus.userservice.models.Athlete;

@Service
public class StravaService {

  public static Athlete getAthlete(String token) {
    RestClient restClient = RestClient.builder().baseUrl("https://www.strava.com/api/v3").build();

    return restClient
        .get()
        .uri("/athlete")
        .header("Authorization", "Bearer " + token)
        .retrieve()
        .body(Athlete.class);
  }

}
