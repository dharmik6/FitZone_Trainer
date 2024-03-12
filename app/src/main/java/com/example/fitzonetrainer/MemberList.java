package com.example.fitzonetrainer;

public class MemberList {
    private String name,username,image,id;
    private String email,number,gender,age,address,acticity,joidate;

    public MemberList(String name, String email, String image, String id) {
        this.name = name;
        this.email = email;
        this.image=image;
        this.id=id;
    }

    public String getName() {return name;}
    public String getId() {return id;}
    public String getUsername() {return username;}

    public String getImage() {return image;}
    public String getEmail() {return email;}
    public String getNumber() {return number;}
    public String getAge() {return age;}
    public String getGender() {return gender;}
    public String getAddress() {return address;}
    public String getActicity() {return acticity;}
    public String getJoidate() {return joidate;}

    public void setName(String name){this.name=name;}
    public void setUsername(String name){this.username=username;}
    public void setEmail(String email) {this.email=email;}
    public void setNumber(String number) {this.number= number;}
    public void setImage(String image) {this.image= image;}

    public void setAge(String age) {this.age= age;}
    public void setGender(String gender) {this.gender= gender;}
    public void setAddress(String address) {this.address= address;}
    public void setActicity(String acticity) {this.acticity= acticity;}
    public void setJoidate(String joidate) {this.joidate= joidate;}
}
