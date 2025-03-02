package us.salus.userservice.models;

import lombok.Data;

@Data
public class TokenResponse {
  public final String token_type;
  public final Long expires_at;
  public final Long expires_in;
  public final String refresh_token;
  public final String access_token;
}
