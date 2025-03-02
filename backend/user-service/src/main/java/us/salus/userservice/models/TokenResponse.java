package us.salus.userservice.models;

import lombok.Data;

@Data
public class TokenResponse {
  public final String token_type;
  public final Long expires_at;
  public final Long expires_in;
  public final String refresh_token;
  public final String access_token;

  public TokenResponse(String y, Long a, Long i, String r, String t) {
    token_type = y;
    expires_at = a;
    expires_in = i;
    refresh_token = r;
    access_token = t;
  }

}
