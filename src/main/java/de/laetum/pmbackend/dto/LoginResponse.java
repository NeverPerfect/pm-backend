package de.laetum.pmbackend.dto;

public class LoginResponse {

  private Long userId;
  private String token;
  private String username;
  private String role;

  public LoginResponse(Long userId, String token, String username, String role) {
    this.userId = userId;
    this.token = token;
    this.username = username;
    this.role = role;
  }

  public Long getUserId() {
    return userId;
  }

  public String getToken() {
    return token;
  }

  public String getUsername() {
    return username;
  }

  public String getRole() {
    return role;
  }
}
