package com.furkan.profil.Models;

/**
 * Created by Laptop on 8.03.2017.
 */
public class User {

    public String name;
    public String email;
    public String cell;
    public String workphone;
    public String address;
    public String job;
    public String uID;
    public String connection;
    public int number;

    public User(){

    }
    public User(String name, String email,String cell,String workphone, String address,String job,String uID ,String connection, int number) {
        this.name = name;
        this.email = email;
        this.cell = cell;
        this.workphone = workphone;
        this.address=address;
        this.job=job;
        this.uID=uID;
        this.connection=connection;
        this.number=number;
    }



    private String cleanEmailAddress(String email){
        //replace dot with comma since firebase does not allow dot
        return email.replace(".","-");
    }

    public void setNumber(int number){
        this.number=number;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public void setName(String name ) {
        this.name =name ;
    }

    public void setEmail (String email) {
        this.email = email ;
    }

    public void setCell (String cell) {
        this.cell = cell;
    }

    public void setWorkphone (String workphone) {
        this.workphone =workphone ;
    }

    public void setAddress (String address) {
        this.address =address ;
    }

    public void setJob (String job) {
        this.job = job;
    }

    public void setuID (String uID) {
        this.uID = uID;
    }

    public String getName(){
        return name ;
    }

    public String getEmail (){
        return email;
    }

    public String getCell (){
        return cell ;
    }

    public String getWorkphone (){
        return workphone ;
    }

    public String getJob (){
        return job ;
    }

    public String getAddress (){
        return address ;
    }

    public String getuID (){
        return uID ;
    }

    public String getConnection() {
        return connection;
    }

    public int getNumber() {
        return number;
    }
}