package com.server.services;

import com.server.models.UserModel;
import com.server.exceptions.UserNotFoundException;
import com.server.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * This class determines data fetching and placing logic with the DB and ClientHandler.
 * Implements password hashing.
 */
@Service
public class UserService {

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final UserRepo userRepo;


    /**
     * @param userRepo returns information from the userRepository
     */
    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    /**
     * @param userModel refers to the user on which this method is applied.
     * @param providedPassword uses the provided password.
     * @return returns a boolean true of the password is correct.
     */
    public boolean checkPassword(UserModel userModel, String providedPassword){
        return bCryptPasswordEncoder.matches(providedPassword, userModel.getPassword());
    }


    /**
     * @param userModel refers to the user on which this method is applied.
     * @param password accepts the password input.
     */
    public void hashPassword(UserModel userModel, String password) {
        String hashedPassword = bCryptPasswordEncoder.encode(userModel.getPassword());
        userModel.setPassword(hashedPassword);
    }


    /**
     * @param userModel refers to the user on which this method is applied.
     */
    public void addUser(UserModel userModel) {
        hashPassword(userModel, userModel.getPassword());
        userRepo.save(userModel);
        System.err.println("User: " + userModel.getUserName() + " with port: " + userModel.getPortNumber() + " has been added.");
    }


    public List<UserModel> findAllOnlineUsers() {return userRepo.findByIpAddressContains(".");}


    public List<UserModel> findUsersWithPartOfName(String username) {
        return userRepo.findByUserNameStartingWith(username);}

    /**
     * @param userModel refers to the user on which this method is applied.
     * @param portNumber applies the given port number to the chosen client.
     * @param ipAddress applies the given ipAddress number to the chosen client.
     * @param prevId takes the previous ID of the user in order to update it.
     */
    public void updateUserPortAndIpAddress(UserModel userModel, Integer portNumber, String ipAddress, Integer prevId) {
        userModel.setId(prevId);
        userModel.setIpAddress(ipAddress);
        userModel.setPortNumber(portNumber);
        System.err.println("Users: " + userModel.getUserName() + " port Updated to: " + userModel.getPortNumber() + " ipAddress Updated to: " +userModel.getIpAddress());
        userRepo.save(userModel);
    }


    /**
     * @param userName takes the userName that has to be returned.
     * @return returns the user from the database.
     */
    public UserModel findUserModelByName(String userName) {
        return userRepo.findUserModelByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User by the name "+userName+" not found"));
    }



}
