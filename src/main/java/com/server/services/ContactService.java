package com.server.services;

import com.server.models.ContactModel;
import com.server.models.UserModel;
import com.server.repositories.ContactRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class determines data fetching and placing logic with the DB and ClientHandler.
 */
@Service
public class ContactService {

    private final ContactRepo contactRepo;

    /**
     * @param contactRepo instantiates the constructor with the user repository.
     */
    @Autowired
    public ContactService(ContactRepo contactRepo) {this.contactRepo = contactRepo;}

    /**
     * @param contactModel adds the provided contact to the contact table.
     * @param userModel receives the user by which the contact must be added.
     */
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


    /**
     * @param contactModel receives the contact model to edit its status
     * @param status provided the status condition.
     */
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

}
