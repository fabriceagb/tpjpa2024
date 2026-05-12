package dto;

public class LoginResponseDto {
    private String token;
    private Object user; // Peut être remplacé par un UserDto sécurisé

    public LoginResponseDto(String token, Object user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Object getUser() { return user; }
    public void setUser(Object user) { this.user = user; }
}
