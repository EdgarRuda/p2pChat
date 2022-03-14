package com.server.services;

import com.server.models.ContactModel;
import com.server.models.UserModel;
import com.server.repositories.ContactRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepo contactRepo;

    @Autowired
    public ContactService(ContactRepo contactRepo) {this.contactRepo = contactRepo;}

    public void addContact(ContactModel contactModel, UserModel userModel) {
        //We need a list of all contacts, so we could iterate through them checking if there are no duplicates
        try {
            if (contactRepo.findContactModelByUserModelIdAndContactNameAndStatus(userModel.getId(), contactModel.getContactName(), contactModel.getStatus()) == null) {
                if (userModel == contactModel.getUserModel()) {
                    contactRepo.save(contactModel);
                }
            } else {
                System.err.println("Something wrong with the addContact() in ContactService class");
            }
        } catch (Exception e) {
            System.err.println("Something wrong with the addContact() in ContactService class - NullPointer probably");
        }
    }

    public void updateContactStatus(ContactModel contactModel, String status) {
        contactModel.setStatus(status);
        contactRepo.save(contactModel);

    }

    public List<ContactModel> getAllContactsByUserModelId(Integer userId) {
        return contactRepo.findContactModelByUserModelId(userId);
    }

    public ContactModel getContactModelByUserModelIdAndUserNameAndStatus(Integer userId, String userName, String status) {
        return contactRepo.findContactModelByUserModelIdAndContactNameAndStatus(userId, userName, status);
    }

    public List<ContactModel> getContactModelByUserModelIdAndStatus(String userName, String status) {
        return contactRepo.findContactModelByContactNameAndStatus(userName, status);
    }


    // getAllContactContacts()
    // getAllBlockedContacts()

    //We are going to use the above methods to send info about users to the clientApp





}
