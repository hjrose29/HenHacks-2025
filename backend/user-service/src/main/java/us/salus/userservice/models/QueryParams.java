package us.salus.userservice.models;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class QueryParams {
  private HashMap<String, String> params;

  public QueryParams() {
    params = new HashMap<>();
  }

  public void set(String k, String v) {
    params.put(k, v);
  }

  public String get(String k) {
    return params.get(k);
  }

  public void delete(String k) {
    params.remove(k);
  }

  public String toString() {
    String s = "";
    try {
      for (String key : params.keySet()) {
        String uriKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString());
        String uriValue = URLEncoder.encode(params.get(key), StandardCharsets.UTF_8.toString());
        if (s.length() == 0) {
          s += uriKey + "=" + uriValue;
        } else {
          s += "&" + uriKey + "=" + uriValue;
        }
      }
      return s;
    } catch (UnsupportedEncodingException e) {
      System.out.println(e);
      return null;
    }
  }
}
