package com.server.repositories;

import com.server.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserModel, Integer> {

    void deleteUserById(Integer id);

    Optional<UserModel> findUserModelById(Integer id);

    Optional<UserModel> findUserModelByUserName(String userName);

    //@Query("SELECT u.userName FROM USERS u WHERE u.userName LIKE CONCAT('%',:username,'%')")
    List<UserModel> findByUserNameStartingWith(String username);
    //@Param("username") String username

    @Query("select u from UserModel u where u.ipAddress like concat('%', ?1, '%')")
    List<UserModel> findByIpAddressContains(String ipAddress);


}
