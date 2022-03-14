package com.server.repositories;


import com.server.models.ContactModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * A repository that links with JPA repository used to form Queries for data fetching from the DB.
 */
@Repository
public interface ContactRepo extends JpaRepository<ContactModel, Integer> {

    Optional<ContactModel> findContactModelByStatus(String status);

    List<ContactModel> findContactModelByUserModelId(Integer userId);

    ContactModel findContactModelByUserModelIdAndContactNameAndStatus(Integer userId, String userName, String status);

    List<ContactModel> findContactModelByContactNameAndStatus(String userName, String status);

}
