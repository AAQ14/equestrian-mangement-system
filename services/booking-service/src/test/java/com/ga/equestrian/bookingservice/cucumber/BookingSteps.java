package com.ga.equestrian.bookingservice.cucumber;

import com.ga.equestrian.bookingservice.client.UserClient;
import com.ga.equestrian.bookingservice.dto.request.BookingRequest;
import com.ga.equestrian.bookingservice.dto.response.BookingResponse;
import com.ga.equestrian.bookingservice.dto.response.UserSummary;
import com.ga.equestrian.bookingservice.event.BookingEventPublisher;
import com.ga.equestrian.bookingservice.model.entity.Instructor;
import com.ga.equestrian.bookingservice.model.entity.RidingSession;
import com.ga.equestrian.bookingservice.model.enums.*;
import com.ga.equestrian.bookingservice.repository.BookingRepository;
import com.ga.equestrian.bookingservice.repository.InstructorRepository;
import com.ga.equestrian.bookingservice.repository.RidingSessionRepository;
import com.ga.equestrian.bookingservice.security.AuthUser;
import com.ga.equestrian.bookingservice.service.BookingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
public class BookingSteps {
    @Autowired BookingService bookingService;
    @Autowired BookingRepository bookingRepository;
    @Autowired RidingSessionRepository ridingSessionRepository;
    @Autowired InstructorRepository instructorRepository;
    @Autowired UserClient userClient;
    @Autowired BookingEventPublisher bookingEventPublisher;

    private AuthUser client;
    private RidingSession session;
    private BookingResponse result;
    private Exception error;

    @Before
    public void cleanUp() {
        bookingRepository.deleteAll();
        ridingSessionRepository.deleteAll();
        instructorRepository.deleteAll();
        reset(userClient, bookingEventPublisher);
        result = null;
        error = null;
    }

    @Given("an active client with id {long}")
    public void anActiveClient(Long id) {
        client = new AuthUser(id, "client@test.com", "CLIENT");
        stubUser(id, UserStatus.ACTIVE);
    }

    @Given("an available riding session tomorrow with capacity {int}")
    public void anAvailableSession(int capacity) {
        Instructor instructor = new Instructor();
        instructor.setUserId(200L);
        instructor.setFirstName("Sara");
        instructor.setLastName("Ali");
        instructor.setExperienceYears(5);
        instructor.setSpecialization(Specialization.SHOW_JUMPING);
        instructor.setSkillLevel(SkillLevel.ADVANCED);
        instructor = instructorRepository.save(instructor);

        session = new RidingSession();
        session.setInstructor(instructor);
        session.setDate(LocalDate.now().plusDays(1));
        session.setStartTime(LocalTime.of(10, 0));
        session.setEndTime(LocalTime.of(11, 0));
        session.setCapacity(capacity);
        session.setSessionStatus(SessionStatus.AVAILABLE);
        session.setPrice(new BigDecimal("20.00"));
        session.setSkillLevel(SkillLevel.BEGINNER);
        session = ridingSessionRepository.save(session);
    }

    @Given("the session capacity is {int}")
    public void sessionCapacityIs(int capacity) {
        session.setCapacity(capacity);
        session = ridingSessionRepository.save(session);
    }

    @Given("the session is cancelled")
    public void sessionIsCancelled() {
        session.setSessionStatus(SessionStatus.CANCELLED);
        session = ridingSessionRepository.save(session);
    }

    @Given("the client has already booked the session")
    public void alreadyBooked() {
        bookingService.createBooking(client, new BookingRequest(session.getId(), null));
    }

    @When("the client books the session")
    public void clientBooks() {
        try {
            result = bookingService.createBooking(client, new BookingRequest(session.getId(), null));
        } catch (Exception e) {
            error = e;
        }
    }

    @Then("the booking is created with status {string}")
    public void bookingCreated(String status) {
        assertThat(error).isNull();
        assertThat(result.getBookingStatus().name()).isEqualTo(status);
    }

    @Then("a booking event is published to Kafka")
    public void eventPublished() {
        assertThat(mockingDetails(bookingEventPublisher).getInvocations()).isNotEmpty();
    }

    @Then("the booking is rejected with message containing {string}")
    public void bookingRejected(String text) {
        assertThat(error).isNotNull();
        assertThat(error.getMessage()).contains(text);
    }

    @Then("the session status is {string}")
    public void sessionStatusIs(String status) {
        RidingSession fresh = ridingSessionRepository.findById(session.getId()).orElseThrow();
        assertThat(fresh.getSessionStatus().name()).isEqualTo(status);
    }

    private void stubUser(Long id, UserStatus status) {
        when(userClient.getUser(id)).thenReturn(
                new UserSummary(id, "Test", "Client", "client@test.com", status, Role.CLIENT));
    }
}
