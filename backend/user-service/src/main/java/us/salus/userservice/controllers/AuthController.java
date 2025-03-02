package us.salus.userservice.controllers;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import us.salus.userservice.models.Athlete;
import us.salus.userservice.models.QueryParams;
import us.salus.userservice.models.TokenResponse;
import us.salus.userservice.models.User;
import us.salus.userservice.repositories.UserRepository;
import us.salus.userservice.services.StravaAuthService;
import us.salus.userservice.services.StravaService;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserRepository userRepository;

  @Autowired
  public AuthController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

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
  public ResponseEntity<TokenResponse> callback(@RequestParam(value = "code") String code) {
    TokenResponse token = StravaAuthService.getToken(code);
    Athlete athlete = StravaService.getAthlete(token.getAccess_token());

    // Update user in database
    User user;
    Optional<User> oUser = userRepository.findById(athlete.getId());
    if (oUser.isEmpty()) {
      user = new User();
      user.setId(athlete.getId());
      user.setName(athlete.getFirstname());
      if (athlete.getWeight() != null) {
        user.setWeight(athlete.getWeight());
      }
      user.setToken(token);
      userRepository.insert(user);
    } else {
      user = oUser.get();
      user.setToken(token);
      userRepository.insert(user);
    }

    // Create cookie to store JWT
    Cookie cookie = new Cookie("salus_session", "");

    cookie.setMaxAge(60 * 60 * 24 * 7);
    cookie.setSecure(true);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setDomain(System.getenv("FRONTEND_URL"));

    // Redirect back to frontend with cookie
    // URI redirect = URI.create(System.getenv("FRONTEND_URL"));
    // return
    // ResponseEntity.status(HttpStatus.FOUND).location(redirect).header("Set-Cookie",
    // cookie.toString()).build();

    return ResponseEntity.ok(token);
  }
}
