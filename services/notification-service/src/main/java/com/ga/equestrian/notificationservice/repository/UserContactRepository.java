package com.ga.equestrian.notificationservice.repository;

import com.ga.equestrian.notificationservice.model.entity.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {
    List<UserContact> findAllByRoleAndActiveTrue(String role);
}
