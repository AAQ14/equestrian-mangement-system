package com.ga.equestrian;

import com.ga.equestrian.model.entity.*;
import com.ga.equestrian.model.enums.*;
import com.ga.equestrian.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Fills an empty database with sample data for testing and demos.
 * All seeded accounts use the password "Password123!".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final InstructorRepository instructorRepository;
    private final HorseRepository horseRepository;
    private final RidingSessionRepository ridingSessionRepository;
    private final BookingRepository bookingRepository;
    private final HorseAssignmentRepository horseAssignmentRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final TournamentRepository tournamentRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Seed skipped: the database already has data");
            return;
        }

        // ---------- users ----------
        User admin = user("Admin", "Amna", "admin@equestrian.com", "36000001", Role.ADMIN, UserStatus.ACTIVE);
        User instructorUser1 = user("Layla", "Hassan", "layla@equestrian.com", "36000002", Role.INSTRUCTOR, UserStatus.ACTIVE);
        User instructorUser2 = user("Omar", "Salem", "omar@equestrian.com", "36000003", Role.INSTRUCTOR, UserStatus.ACTIVE);
        User client1 = user("Sara", "Ali", "sara@equestrian.com", "36000004", Role.CLIENT, UserStatus.ACTIVE);
        User client2 = user("Khalid", "Nasser", "khalid@equestrian.com", "36000005", Role.CLIENT, UserStatus.ACTIVE);
        User client3 = user("Noor", "Yusuf", "noor@equestrian.com", "36000006", Role.CLIENT, UserStatus.ACTIVE);
        user("Mariam", "Jaber", "mariam@equestrian.com", "36000007", Role.CLIENT, UserStatus.PENDING);

        // ---------- instructors ----------
        Instructor instructor1 = instructor(instructorUser1, Specialization.DRESSAGE, 8, SkillLevel.ADVANCED);
        Instructor instructor2 = instructor(instructorUser2, Specialization.SHOW_JUMPING, 5, SkillLevel.INTERMEDIATE);

        // ---------- horses ----------
        Horse horse1 = horse("Thunder", "Arabian", 8, Gender.MARE, SkillLevel.ADVANCED);
        Horse horse2 = horse("Luna", "Thoroughbred", 6, Gender.MARE, SkillLevel.INTERMEDIATE);
        Horse horse3 = horse("Biscuit", "Quarter Horse", 12, Gender.STALLION, SkillLevel.BEGINNER);
        horse("Shadow", "Andalusian", 9, Gender.STALLION, SkillLevel.ADVANCED);

        // ---------- riding sessions ----------
        LocalDate today = LocalDate.now();
        RidingSession session1 = session(instructor1, today.plusDays(1), 10, 11, "15.00", SkillLevel.BEGINNER, SessionStatus.AVAILABLE, 4);
        RidingSession session2 = session(instructor1, today.plusDays(2), 16, 17, "30.00", SkillLevel.INTERMEDIATE, SessionStatus.FULL, 1);
        session(instructor2, today.plusDays(3), 9, 10, "20.00", SkillLevel.ADVANCED, SessionStatus.AVAILABLE, 6);
        RidingSession pastSession = session(instructor2, today.minusDays(5), 15, 16, "18.00", SkillLevel.BEGINNER, SessionStatus.COMPLETED, 5);
        session(instructor1, today.plusDays(5), 11, 12, "15.00", SkillLevel.BEGINNER, SessionStatus.CANCELLED, 4);

        // ---------- bookings, payments, horse assignments ----------
        Booking booking1 = booking(client1, session1, BookingStatus.CONFIRMED,
                payment("15.00", PaymentStatus.COMPLETED, PaymentMethod.CARD));
        booking(client2, session1, BookingStatus.PENDING, null);
        Booking booking3 = booking(client3, session2, BookingStatus.CONFIRMED,
                payment("30.00", PaymentStatus.COMPLETED, PaymentMethod.CASH));
        Booking pastBooking1 = booking(client1, pastSession, BookingStatus.COMPLETED,
                payment("18.00", PaymentStatus.COMPLETED, PaymentMethod.CARD));
        Booking pastBooking2 = booking(client2, pastSession, BookingStatus.COMPLETED,
                payment("18.00", PaymentStatus.COMPLETED, PaymentMethod.CASH));

        assign(booking1, horse1);
        assign(booking3, horse2);

        // ---------- tournaments and seats ----------
        Tournament showJumping = tournament("Winter Show Jumping Cup", TournamentType.SHOW_JUMPING,
                today.plusDays(20), 18, "Royal Equestrian Arena", "Annual show jumping tournament.", "15.50", TournamentStatus.UPCOMING);
        Tournament dressage = tournament("Spring Dressage Classic", TournamentType.DRESSAGE,
                today.plusDays(35), 17, "Sakhir Stables", "Elegant dressage competition.", "10.00", TournamentStatus.UPCOMING);
        Tournament polo = tournament("Desert Polo Day", TournamentType.POLO,
                today.minusDays(10), 16, "Al Areen Field", "A finished polo event.", "12.00", TournamentStatus.COMPLETED);

        List<Seat> showJumpingSeats = seats(showJumping, 20);
        seats(dressage, 12);
        seats(polo, 10);

        // ---------- tickets (the seats they hold become SOLD) ----------
        Seat seat1 = showJumpingSeats.get(0);
        Seat seat2 = showJumpingSeats.get(1);
        Seat seat3 = showJumpingSeats.get(2);
        sell(seat1);
        sell(seat2);
        ticket(client1, showJumping, seat1, TicketStatus.CONFIRMED, payment("15.50", PaymentStatus.COMPLETED, PaymentMethod.CARD));
        ticket(client2, showJumping, seat2, TicketStatus.PENDING, null);
        ticket(client3, showJumping, seat3, TicketStatus.CANCELLED, null);   // cancelled, so seat3 stays AVAILABLE

        // ---------- notifications ----------
        notification(client1, "Your booking for " + session1.getDate() + " was confirmed.", NotificationType.BOOKING_CONFIRMED);
        notification(client1, "Your ticket for seat 1 of Winter Show Jumping Cup was bought successfully.", NotificationType.TICKET_PURCHASED);
        notification(client2, "Welcome to the Equestrian Centre!", NotificationType.SYSTEM);

        // ---------- audit log ----------
        audit(client1, AuditAction.LOGIN, "User", client1.getId(), "User " + client1.getId() + " logged in");
        audit(client1, AuditAction.CREATE_BOOKING, "Booking", booking1.getId(), "Booking " + booking1.getId() + " created");
        audit(client1, AuditAction.BUY_TICKET, "Seat", seat1.getId(), "Bought seat " + seat1.getId());
        audit(admin, AuditAction.CREATE_TOURNAMENT, "Tournament", showJumping.getId(), "Tournament " + showJumping.getId() + " created");

        log.info("Seed data created");
    }

    private User user(String first, String last, String email, String phone, Role role, UserStatus status) {
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPassword(passwordEncoder.encode("Password123!"));
        u.setRole(role);
        u.setUserStatus(status);
        return userRepository.save(u);
    }

    private Instructor instructor(User u, Specialization specialization, int years, SkillLevel level) {
        Instructor i = new Instructor();
        i.setUser(u);
        i.setSpecialization(specialization);
        i.setExperienceYears(years);
        i.setSkillLevel(level);
        return instructorRepository.save(i);
    }

    private Horse horse(String name, String breed, int age, Gender gender, SkillLevel level) {
        Horse h = new Horse();
        h.setName(name);
        h.setBreed(breed);
        h.setAge(age);
        h.setGender(gender);
        h.setSkillLevel(level);
        h.setHorseStatus(HorseStatus.AVAILABLE);
        return horseRepository.save(h);
    }

    private RidingSession session(Instructor instructor, LocalDate date, int startHour, int endHour,
                                  String price, SkillLevel level, SessionStatus status, int capacity) {
        RidingSession s = new RidingSession();
        s.setInstructor(instructor);
        s.setDate(date);
        s.setStartTime(LocalTime.of(startHour, 0));
        s.setEndTime(LocalTime.of(endHour, 0));
        s.setPrice(new BigDecimal(price));
        s.setSkillLevel(level);
        s.setSessionStatus(status);
        s.setCapacity(capacity);
        return ridingSessionRepository.save(s);
    }

    private Payment payment(String amount, PaymentStatus status, PaymentMethod method) {
        Payment p = new Payment();
        p.setAmount(new BigDecimal(amount));
        p.setPaymentStatus(status);
        p.setPaymentMethod(method);
        if (status == PaymentStatus.COMPLETED) {
            p.setPaymentDate(LocalDateTime.now());
        }
        return paymentRepository.save(p);
    }

    private Booking booking(User u, RidingSession session, BookingStatus status, Payment payment) {
        Booking b = new Booking();
        b.setUser(u);
        b.setRidingSession(session);
        b.setBookingStatus(status);
        b.setPayment(payment);
        return bookingRepository.save(b);
    }

    private void assign(Booking booking, Horse horse) {
        HorseAssignment a = new HorseAssignment();
        a.setBooking(booking);
        a.setHorse(horse);
        a.setRidingSession(booking.getRidingSession());
        horseAssignmentRepository.save(a);
    }

    private Tournament tournament(String name, TournamentType type, LocalDate date, int hour, String venue,
                                  String description, String price, TournamentStatus status) {
        Tournament t = new Tournament();
        t.setName(name);
        t.setTournamentType(type);
        t.setDate(date);
        t.setTime(LocalTime.of(hour, 0));
        t.setVenue(venue);
        t.setDescription(description);
        t.setTicketPrice(new BigDecimal(price));
        t.setTournamentStatus(status);
        return tournamentRepository.save(t);
    }

    private List<Seat> seats(Tournament tournament, int count) {
        List<Seat> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Seat s = new Seat();
            s.setTournament(tournament);
            s.setSeatNumber(i);
            s.setPrice(tournament.getTicketPrice());
            list.add(s);
        }
        return seatRepository.saveAll(list);
    }

    private void sell(Seat seat) {
        seat.setSeatStatus(SeatStatus.SOLD);
        seatRepository.save(seat);
    }

    private void ticket(User u, Tournament tournament, Seat seat, TicketStatus status, Payment payment) {
        Ticket t = new Ticket();
        t.setUser(u);
        t.setTournament(tournament);
        t.setSeat(seat);
        t.setPrice(seat.getPrice());
        t.setTicketStatus(status);
        t.setPayment(payment);
        ticketRepository.save(t);
    }

    private void notification(User u, String message, NotificationType type) {
        Notification n = new Notification();
        n.setUser(u);
        n.setMessage(message);
        n.setNotificationType(type);
        notificationRepository.save(n);
    }

    private void audit(User u, AuditAction action, String entityType, Long entityId, String description) {
        AuditLog a = new AuditLog();
        a.setUser(u);
        a.setAuditAction(action);
        a.setEntityType(entityType);
        a.setEntityId(entityId);
        a.setDescription(description);
        auditLogRepository.save(a);
    }
}
