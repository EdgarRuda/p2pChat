package com.server.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "user_model")
public class UserModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false)
    private Integer id;
    @Column(name = "user_name", nullable = false)
    private String userName;
    @Column(name = "port_number")
    private Integer portNumber;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "friends")


    @OneToMany
    private List<ContactModel> contacts;

    public UserModel() {};

    public UserModel(Integer id, String userName, Integer portNumber, String ipAddress, String password) {
        this.id = id;
        this.userName = userName;
        this.portNumber = portNumber;
        this.ipAddress = ipAddress;
        this.password = password;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ContactModel> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactModel> contacts) {
        this.contacts = contacts;
    }


}
