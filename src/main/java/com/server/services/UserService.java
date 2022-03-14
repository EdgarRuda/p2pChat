package com.server.services;

import com.server.models.UserModel;
import com.server.exceptions.UserNotFoundException;
import com.server.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;


@Service
public class UserService {

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final UserRepo userRepo;

    //Autowired UserRepo
    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    //Checks if the passwords are the same
    public boolean checkPassword(UserModel userModel, String providedPassword){
        return bCryptPasswordEncoder.matches(providedPassword, userModel.getPassword());
    }

    //Hashes the password when called
    public void hashPassword(UserModel userModel, String password) {
        String hashedPassword = bCryptPasswordEncoder.encode(userModel.getPassword());
        userModel.setPassword(hashedPassword);
    }

    //Adds a user with a Hashed password
    public void addUser(UserModel userModel) {
        hashPassword(userModel, userModel.getPassword());
        userRepo.save(userModel);
        System.err.println("User: " + userModel.getUserName() + " with port: " + userModel.getPortNumber() + " has been added.");
    }

    public List<UserModel> findAllUsers() {return userRepo.findAll();}

    public List<UserModel> findAllOnlineUsers() {return userRepo.findByIpAddressContains(".");}

    //Finds all users
    public List<UserModel> findUsersWithPartOfName(String username) {
        return userRepo.findByUserNameStartingWith(username);}

    public void updateUserPortAndIpAddress(UserModel userModel, Integer portNumber, String ipAddress, Integer prevId) {
        userModel.setId(prevId);
        userModel.setIpAddress(ipAddress);
        userModel.setPortNumber(portNumber);
        System.err.println("Users: " + userModel.getUserName() + " port Updated to: " + userModel.getPortNumber() + " ipAddress Updated to: " +userModel.getIpAddress());
        userRepo.save(userModel);
    }


    public UserModel findUserById(Integer id) {
        return userRepo.findUserModelById(id)
                .orElseThrow(() -> new UserNotFoundException("User by id: " + id + " not found."));
    }

    public UserModel findUserModelByName(String userName) {
        return userRepo.findUserModelByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User by the name "+userName+" not found"));
    }

    public void deleteUser(Integer id){userRepo.deleteUserById(id);}


}
