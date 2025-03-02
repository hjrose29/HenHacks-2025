package us.salus.userservice.models;

import lombok.Data;

@Data
public class AuthCodeRequest {
  final String client_id;
  final String client_secret;
  final String grant_type = "authorization_code";
  final String code;

  public AuthCodeRequest(String code) {
    this.client_id = System.getenv("CLIENT_ID");
    this.client_secret = System.getenv("CLIENT_SECRET");
    this.code = code;
  }
}
