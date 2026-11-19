package com.ga.equestrian.bookingservice.cucumber;


import com.ga.equestrian.bookingservice.client.UserClient;
import com.ga.equestrian.bookingservice.event.AuditEventPublisher;
import com.ga.equestrian.bookingservice.event.BookingEventPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest
@ActiveProfiles("test")
public class CucumberSpringConfig {
    @MockitoBean
    UserClient userClient;

    @MockitoBean
    BookingEventPublisher bookingEventPublisher;

    @MockitoBean
    AuditEventPublisher auditEventPublisher;
}
