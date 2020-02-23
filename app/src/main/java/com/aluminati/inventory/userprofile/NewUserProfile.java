package com.aluminati.inventory.userprofile;

public class NewUserProfile {
    private String email;
    private String name;
    private String password;
    private String address;
    private String phone;

    public NewUserProfile() {}



    public NewUserProfile(String email, String name, String address, String phone) {
        this.email = email;
        this.name = name;
       // this.password = password;
        this.address = address;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
