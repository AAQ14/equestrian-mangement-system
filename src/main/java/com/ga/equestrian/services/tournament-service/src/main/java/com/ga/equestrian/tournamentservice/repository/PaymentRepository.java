package com.ga.equestrian.tournamentservice.repository;

import com.ga.equestrian.tournamentservice.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
