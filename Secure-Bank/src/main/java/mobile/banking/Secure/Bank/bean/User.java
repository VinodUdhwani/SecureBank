package mobile.banking.Secure.Bank.bean;

import java.sql.Date;

public class User {

    private int userId;
    private String name;
    private String mobileNo;
    private String email;
    private String DateOfBirth;
    private String fatherName;
    private String address;
    private String otp;
    private double amount;

    public User(int userId,String name, String mobileNo, String email,String dateOfBirth, String fatherName, String address, String otp,double amount) {
        this.userId=userId;
        this.name = name;
        this.mobileNo = mobileNo;
        this.email = email;
        this.DateOfBirth = dateOfBirth;
        this.fatherName = fatherName;
        this.address = address;
        this.otp=otp;
        this.amount=amount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
