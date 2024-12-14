package mobile.banking.Secure.Bank.Controller;

import mobile.banking.Secure.Bank.bean.EmailDetails;
import mobile.banking.Secure.Bank.bean.User;
import mobile.banking.Secure.Bank.service.EmailService;
import mobile.banking.Secure.Bank.utility.Otp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

@RestController
public class BankController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("OpenNewAccount")
    public String accountOpening(@RequestBody User user){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String sql="select * from user where name=? and mobileNo=? and email=?";
            PreparedStatement ps= connection.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getMobileNo());
            ps.setString(3, user.getEmail());
            ResultSet resultSet= ps.executeQuery();
            if(resultSet.next())
                return "either name or mobile or email already exist";
            else {
                String otp = Otp.generateOtp(4);
                emailService.sendMail(new EmailDetails(user.getEmail(), "Otp Generated", "Your verification otp is " + otp));
                String query = "insert into user(name,mobileNo,email,DOB,fatherName,address,otp,is_verified)values(?,?,?,?,?,?,?,0)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, user.getName());
                preparedStatement.setString(2, user.getMobileNo());
                preparedStatement.setString(3, user.getEmail());
                preparedStatement.setString(4, user.getDateOfBirth());
                preparedStatement.setString(5, user.getFatherName());
                preparedStatement.setString(6, user.getAddress());
                preparedStatement.setString(7, otp);
                preparedStatement.executeUpdate();
                return "your details has been submitted";
            }
        }
        catch (Exception e){
            return "Something went wrong.";
        }
    }


    @PostMapping("verify")
    public String verification(@RequestBody User user){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select otp,account_no from user where userId=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setInt(1, user.getUserId());
            ResultSet resultSet= preparedStatement.executeQuery();
            if(resultSet.next()){
                if(resultSet.getString(1).equals(user.getOtp()) && resultSet.getString(2)==null) {
                    String sql="update user set is_verified=1,account_no=?,ifsc=?,acc_balance=? where userId=?";
                    PreparedStatement preparedStatement1= connection.prepareStatement(sql);
                    preparedStatement1.setString(1,"573"+Otp.generateOtp(9));
                    preparedStatement1.setString(2,"SEC000159");
                    preparedStatement1.setDouble(3,0.0);
                    preparedStatement1.setInt(4, user.getUserId());
                    preparedStatement1.executeUpdate();
                    return "your details has verified successfully";
                }
                else {
                    if(resultSet.getString(2)==null)
                        return "Invalid otp";
                    else
                    return "your details has verified already";
                }
            }
            else{
                return "userId does not exist";
            }
        }
        catch (Exception e){
            return "Something went wrong.try again";
        }
    }

    @GetMapping("accountinfo")
    public ArrayList accountDetails(@RequestParam int userId){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select account_no,ifsc,mobileNo,email from user where userId=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            ResultSet resultSet= preparedStatement.executeQuery();
            ArrayList arrayList=new ArrayList<>();
            if(resultSet.next()){
                arrayList.add(resultSet.getString(1));
                arrayList.add(resultSet.getString(2));
                arrayList.add(resultSet.getString(3));
                arrayList.add(resultSet.getString(4));
                return arrayList;
            }
            else
                return null;
        }
        catch (Exception e){
            return null;
        }
    }

    @GetMapping("checkBalance")
    public double checkBalance(@RequestParam int userId){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select acc_balance from user where userId=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next())
            return resultSet.getDouble(1);
            else
                return 0.0;
        }
        catch (Exception e){
            return 0;
        }
    }


    public void transations(int userId,double amount,String mobileNo){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select account_no from user where userId=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            ResultSet resultSet=preparedStatement.executeQuery();
            if(resultSet.next()){
                String query1="update passbook set amount=?,account_no=?,sendTo=? where userId=?";
                PreparedStatement preparedStatement1= connection.prepareStatement(query1);
                preparedStatement1.setDouble(1,amount);
                preparedStatement1.setString(2,resultSet.getString(1));
                preparedStatement1.setString(3,mobileNo);
                preparedStatement1.setInt(4,userId);
                preparedStatement1.executeUpdate();
            }
            String query2="select userId, account_no from user where mobileNo=?";
            PreparedStatement preparedStatement2= connection.prepareStatement(query2);
            preparedStatement2.setString(1,mobileNo);
            ResultSet resultSet1=preparedStatement2.executeQuery();
            if(resultSet1.next()){
                String query3="update passbook set amount=?,account_no=?,sendTo=? where userId=?";
                PreparedStatement preparedStatement3= connection.prepareStatement(query3);
                preparedStatement3.setDouble(1,amount);
                preparedStatement3.setInt(2,resultSet1.getInt(1));
                preparedStatement3.setString(3,resultSet.getString(2));
                preparedStatement3.setInt(4,userId);
                preparedStatement3.executeUpdate();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @PostMapping("transfermoney")
    public String transfer(@RequestBody User user){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select acc_balance from user where userId=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setInt(1,user.getUserId());
            ResultSet resultSet= preparedStatement.executeQuery();
            if(resultSet.next()){
                if(resultSet.getDouble(1)>= user.getAmount()){
                    String query1="update user set acc_balance=? where userId=?";
                    PreparedStatement preparedStatement1= connection.prepareStatement(query1);
                    preparedStatement1.setDouble(1,resultSet.getDouble(1)-user.getAmount());
                    preparedStatement1.setInt(2,user.getUserId());
                    preparedStatement1.executeUpdate();

                    BankController bankController=new BankController();
                    bankController.transations(user.getUserId(), user.getAmount(), user.getMobileNo());

                    String query2="select acc_balance,userId from user where mobileNo=?";
                    PreparedStatement preparedStatement2= connection.prepareStatement(query2);
                    preparedStatement2.setString(1, user.getMobileNo());
                    ResultSet resultSet1=preparedStatement2.executeQuery();
                    if(resultSet1.next()){
                        transations(resultSet1.getInt(2), user.getAmount(),resultSet.getString(1));
                        String query3 = "update user set acc_balance=? where userId=?";
                        PreparedStatement preparedStatement3 = connection.prepareStatement(query3);
                        preparedStatement3.setDouble(1,resultSet1.getDouble(1)+user.getAmount());
                        preparedStatement3.setInt(2, resultSet1.getInt(2));
                        preparedStatement3.executeUpdate();
                    }
                    else
                        return "account not found with this mobile number";
                    return "transaction successful";
                }
                else
                    return "Insufficient balance";
            }
            else
                return "account does not exist";
        }
        catch (Exception e){
            e.printStackTrace();
            return "Something went wrong";
        }
    }



    @GetMapping("viewtransaction")
    public void passbook(@RequestParam int userId){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select ";
        }
        catch (Exception e){

        }
    }

    @GetMapping("AddWallet")
    public String wallet(@RequestParam int userId,double ammount){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select acc_balance from user where userId=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            ResultSet resultSet= preparedStatement.executeQuery();
            if(resultSet.next()) {
                if (resultSet.getDouble(1) >= ammount) {
                    String query1 = "update user set acc_balance=? where userId=?";
                    PreparedStatement preparedStatement1 = connection.prepareStatement(query1);
                    preparedStatement1.setDouble(1, resultSet.getDouble(1) - ammount);
                    preparedStatement1.setInt(2, userId);
                    preparedStatement1.executeUpdate();

                    String query2 = "select wallet from user where userId=?";
                    PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
                    preparedStatement2.setInt(1, userId);
                    ResultSet resultSet1 = preparedStatement2.executeQuery();
                    if(resultSet1.next()) {
                        String query3 = "update user set wallet=? where userId=?";
                        PreparedStatement preparedStatement3 = connection.prepareStatement(query3);
                        preparedStatement3.setDouble(1, resultSet1.getDouble(1) + ammount);
                        preparedStatement3.setInt(2, userId);
                        preparedStatement3.executeUpdate();
                        return "money added successfully";
                    }
                    else
                        return "invalid details";
                } else
                    return "insufficient balance";
            }
            else
                return "account does not exist.";
        }
        catch (Exception e){
            e.printStackTrace();
            return "Something went wrong.try again.";
        }
    }



    @GetMapping("SelfTransfer")
    public String walletToBank(@RequestParam int userId,double ammount){
        try(Connection connection=jdbcTemplate.getDataSource().getConnection()){
            String query="select wallet from user where userId=?";
            PreparedStatement preparedStatement= connection.prepareStatement(query);
            preparedStatement.setInt(1,userId);
            ResultSet resultSet= preparedStatement.executeQuery();
            if(resultSet.next()) {
                if (resultSet.getDouble(1) >= ammount) {
                    String query1 = "update user set wallet=? where userId=?";
                    PreparedStatement preparedStatement1 = connection.prepareStatement(query1);
                    preparedStatement1.setDouble(1, resultSet.getDouble(1) - ammount);
                    preparedStatement1.setInt(2, userId);
                    preparedStatement1.executeUpdate();

                    String query2 = "select acc_balance from user where userId=?";
                    PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
                    preparedStatement2.setInt(1, userId);
                    ResultSet resultSet1 = preparedStatement2.executeQuery();
                    if(resultSet1.next()) {
                        String query3 = "update user set acc_balance=? where userId=?";
                        PreparedStatement preparedStatement3 = connection.prepareStatement(query3);
                        preparedStatement3.setDouble(1, resultSet1.getDouble(1) + ammount);
                        preparedStatement3.setInt(2, userId);
                        preparedStatement3.executeUpdate();
                        return "money sent successfully";
                    }
                    else
                        return "invalid details";
                } else
                    return "insufficient balance";
            }
            else
                return "account does not exist.";
        }
        catch (Exception e){
            e.printStackTrace();
            return "Something went wrong.try again.";
        }
    }
}
