package us.salus.userservice.controllers;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.salus.userservice.models.Athlete;
import us.salus.userservice.models.QueryParams;
import us.salus.userservice.models.TokenResponse;
import us.salus.userservice.services.StravaAuthService;
import us.salus.userservice.services.StravaService;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @GetMapping("/login")
  public ResponseEntity<String> login() {
    QueryParams params = new QueryParams();
    params.set("client_id", System.getenv("CLIENT_ID"));
    params.set("redirect_uri", System.getenv("REDIRECT_URI"));
    params.set("response_type", "code");
    params.set("approval_prompt", "force");
    params.set("scope", "read");
    URI redirect = URI.create("https://www.strava.com/oauth/authorize?" + params.toString());
    return ResponseEntity.status(HttpStatus.FOUND).location(redirect).build();
  }

  @GetMapping("/callback")
  public ResponseEntity<Athlete> callback(@RequestParam(value = "code") String code) {
    TokenResponse token = StravaAuthService.getToken(code);
    Athlete athlete = StravaService.getAthlete(token.getAccess_token());
    return ResponseEntity.ok(athlete);
  }
}
