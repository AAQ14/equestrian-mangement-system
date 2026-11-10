package com.ga.equestrian.bookingservice.repository;

import com.ga.equestrian.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
