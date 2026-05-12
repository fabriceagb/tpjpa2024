package dto;

public class LoginDto {
    public String email;
    public String password;


    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public  String getPassword(){
        return  this.password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
