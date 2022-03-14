package com.client.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class ContactList {

    private final ObservableList<User> contacts = FXCollections.observableArrayList();
    private final ObjectProperty<User> currentUser = new SimpleObjectProperty<>();
    public ObservableList<User> getContacts(){return contacts;}

    public final User getCurrentUser(){return currentUser.get();}
    public final void setCurrentUser(User user){
        currentUser.set(user);
    }


    public User getUser(String user){
        for (User contact : contacts) {
            if (contact.getName()!= null && contact.getName().equals(user))
               return contact;
        }
        return null;
    }

    public void addUser(User user) {
        contacts.add(user);
    }

    public void trimSearchResult(ArrayList<String> searchResult){
        ArrayList<String> temp = new ArrayList<>(searchResult);
        if (!searchResult.isEmpty())
            searchResult.remove(0);
        if (!searchResult.isEmpty())
            for (String contact : temp)
                for (User contactsModelContact : contacts)
                    if (contactsModelContact.getName().equals(contact))
                        searchResult.remove(contact);
    }


    public void removeUser(String name){
        contacts.removeIf(contact -> contact.getName().equals(name));
    }

    public void removeUser(User user){
        contacts.remove(user);
    }

}
