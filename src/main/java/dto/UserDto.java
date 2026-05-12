package dto;

public class UserDto {
    public String email;
    public String password;
    public String firstName;
    public String lastName;
    public String role;
    public String phoneNumber;

    // Getters et Setters (nécessaires pour que Jackson parse le JSON)
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }


    public String getRole(){
        return this.role;
    }
    public void setRole(String role){
        this.role = role;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
