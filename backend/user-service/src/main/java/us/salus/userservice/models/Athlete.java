package us.salus.userservice.models;

import lombok.Data;

@Data
public class Athlete {
  Long id;
  String firstname;
  String lastname;
  Integer weight;
}
