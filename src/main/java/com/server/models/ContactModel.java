package com.server.models;


import javax.persistence.*;
import java.io.Serializable;

/**
 * Base entity formation for user contacts.
 * The table is auto-generated within PostgreSQL (check resources/application.properties to set up your postgres id
 * and password.
 */
@Entity
@Table(name = "contact_model")
public class ContactModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, insertable = false)
    private Integer id;
    @Column(name = "user_name", nullable = false)
    private String contactName;
    @Column(name = "status", nullable =false)
    private String status;

    @ManyToOne
    private UserModel userModel;


    public ContactModel() {}

    public ContactModel(Integer id, String contactName, String status, UserModel userModel) {
        this.id = id;
        this.contactName = contactName;
        this.status = status;
        this.userModel = userModel;
    }

    @Override
    public String toString() {
        return "contactModel{" +
                ", contactName='" + contactName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
