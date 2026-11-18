package com.ga.equestrian.notificationservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A small copy of a user's contact details, kept by notification-service
 * so it can notify and email users without calling user-service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_contacts")
public class UserContact {
    /** The same id as the user in user-service. */
    @Id
    private Long userId;

    @Column(nullable = false)
    private String email;

    private String firstName;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean active;
}
