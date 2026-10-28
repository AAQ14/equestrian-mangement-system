package com.ga.equestrian.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ga.equestrian.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * bookings entity represents a client's reservation of a riding session.
 * A client can't book the same session in the same time.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "bookings")
//uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "session_id"}))
@ToString(exclude = {"user", "ridingSession", "payment"})
public class Booking {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="session_id", nullable = false)
    private RidingSession ridingSession;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus = BookingStatus.CONFIRMED;

    @JsonIgnore
    @OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
    private HorseAssignment horseAssignment;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
