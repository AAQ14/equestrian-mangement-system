package com.ga.equestrian.notificationservice.service;

import com.ga.equestrian.notificationservice.model.enums.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Year;

/**
 * Sends the HTML notification emails of the Equestrian Center.
 * Every email uses the same layout (templates/email/base.html); only the title, text, photo and button change.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * The address of the React frontend, used for the buttons in the emails.
     */
    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Banner photos (free photos from Unsplash). Emails need full public image addresses.
     */
    private static final String PHOTO_RIDER = photo("1600757490188-1eb2bab4cf4a");
    private static final String PHOTO_JUMPING = photo("1574695272842-52dfdb7989d0");
    private static final String PHOTO_HORSE = photo("1593179449458-e0d43d512551");
    private static final String PHOTO_RUNNING = photo("1553284965-83fd3e82fa5a");

    /**
     * The HTML layout, read once from the resources folder and kept in memory.
     */
    private String baseTemplate;

    /**
     * The parts of an email that change for each notification type.
     *
     * @param subject the subject line in the inbox.
     * @param eyebrow the small grey label above the title.
     * @param title the big title of the email.
     * @param buttonText the text of the button, or null for no button.
     * @param path the frontend page the button opens.
     * @param imageUrl the photo shown at the top.
     */
    private record EmailStyle(String subject, String eyebrow, String title,
                              String buttonText, String path, String imageUrl) {
    }

    /**
     * Builds the address of an Unsplash photo, cropped to the banner size.
     *
     * @param id the id of the photo on Unsplash.
     * @return the full image address.
     */
    private static String photo(String id) {
        return "https://images.unsplash.com/photo-" + id + "?auto=format&fit=crop&w=1200&h=520&q=80";
    }

    /**
     * Sends the email of one notification. The look depends on the notification type.
     *
     * @param to the email address of the user.
     * @param firstName the first name of the user, used in the greeting.
     * @param type the type of the notification.
     * @param message the notification text, shown as the main text.
     */
    public void sendNotificationEmail(String to, String firstName, NotificationType type, String message) {
        EmailStyle style = styleFor(type);
        String button = style.buttonText() == null ? "" : button(style.buttonText(), frontendUrl + style.path());
        String html = render(style.eyebrow(), style.title(), firstName, style.imageUrl(), paragraph(message), button, message);
        sendHtml(to, style.subject(), html);
    }

    /**
     * Chooses the subject, title, photo and button for each notification type.
     *
     * @param type the type of the notification.
     * @return the parts of the email for this type.
     */
    private EmailStyle styleFor(NotificationType type) {
        return switch (type) {
            case BOOKING_CREATED -> new EmailStyle("We received your booking", "Riding lesson",
                    "Booking received", "View my bookings", "/my-bookings", PHOTO_RIDER);
            case BOOKING_CONFIRMED -> new EmailStyle("Your booking is confirmed", "Riding lesson",
                    "You're all set", "View my bookings", "/my-bookings", PHOTO_RIDER);
            case BOOKING_CANCELLED -> new EmailStyle("Your booking was cancelled", "Riding lesson",
                    "Booking cancelled", "Book another lesson", "/sessions", PHOTO_RIDER);
            case SESSION_UPDATED -> new EmailStyle("Your lesson was updated", "Riding lesson",
                    "Lesson updated", "View my bookings", "/my-bookings", PHOTO_RIDER);
            case SESSION_CANCELLED -> new EmailStyle("Your lesson was cancelled", "Riding lesson",
                    "Lesson cancelled", "Find another lesson", "/sessions", PHOTO_RIDER);
            case HORSE_ASSIGNED -> new EmailStyle("A horse was assigned to your lesson", "Your horse",
                    "Meet your horse", "View my bookings", "/my-bookings", PHOTO_HORSE);
            case HORSE_ASSIGNMENT_UPDATED -> new EmailStyle("Your horse was changed", "Your horse",
                    "New horse for your lesson", "View my bookings", "/my-bookings", PHOTO_HORSE);
            case HORSE_ASSIGNMENT_CANCELLED -> new EmailStyle("Your horse was removed", "Your horse",
                    "Horse removed", "View my bookings", "/my-bookings", PHOTO_HORSE);
            case TICKET_PURCHASED -> new EmailStyle("Your ticket is confirmed", "Tournament ticket",
                    "Enjoy the show", "View my tickets", "/my-tickets", PHOTO_JUMPING);
            case TICKET_CANCELLED -> new EmailStyle("Your ticket was cancelled", "Tournament ticket",
                    "Ticket cancelled", "See tournaments", "/tournaments", PHOTO_JUMPING);
            case TICKET_SEAT_CHANGED -> new EmailStyle("Your seat was changed", "Tournament ticket",
                    "New seat confirmed", "View my tickets", "/my-tickets", PHOTO_JUMPING);
            case TOURNAMENT_CREATED -> new EmailStyle("A new tournament is open", "Tournaments",
                    "New tournament", "Choose your seat", "/tournaments", PHOTO_JUMPING);
            case TOURNAMENT_UPDATED -> new EmailStyle("A tournament was updated", "Tournaments",
                    "Tournament updated", "View my tickets", "/my-tickets", PHOTO_JUMPING);
            case TOURNAMENT_CANCELLED -> new EmailStyle("A tournament was cancelled", "Tournaments",
                    "Tournament cancelled", "View my tickets", "/my-tickets", PHOTO_JUMPING);
            // Any other type (for example SEAT_CONFLICT or SYSTEM) gets a simple general email.
            default -> new EmailStyle("A message from the Equestrian Center", "Equestrian Center",
                    "Hello from the club", null, null, PHOTO_RUNNING);
        };
    }

    /**
     * Fills the layout with the content of one email.
     * Text from the database is escaped first, so names cannot break the HTML.
     *
     * @return the full HTML of the email.
     */
    private String render(String eyebrow, String title, String firstName, String imageUrl,
                          String contentHtml, String buttonHtml, String preheader) {
        String name = (firstName == null || firstName.isBlank()) ? "rider" : firstName;
        return loadTemplate()
                .replace("{{title}}", HtmlUtils.htmlEscape(title))
                .replace("{{imageUrl}}", HtmlUtils.htmlEscape(imageUrl))
                .replace("{{imageAlt}}", "Horse riding at the Equestrian Center")
                .replace("{{eyebrow}}", HtmlUtils.htmlEscape(eyebrow))
                .replace("{{firstName}}", HtmlUtils.htmlEscape(name))
                .replace("{{preheader}}", HtmlUtils.htmlEscape(preheader))
                .replace("{{content}}", contentHtml)
                .replace("{{button}}", buttonHtml)
                .replace("{{year}}", String.valueOf(Year.now().getValue()));
    }

    /**
     * Reads templates/email/base.html the first time it is needed, then keeps it in memory.
     *
     * @return the HTML layout with its placeholders.
     */
    private String loadTemplate() {
        if (baseTemplate == null) {
            try (InputStream input = new ClassPathResource("templates/email/base.html").getInputStream()) {
                baseTemplate = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new IllegalStateException("Email template not found: templates/email/base.html", ex);
            }
        }
        return baseTemplate;
    }

    /**
     * Builds one paragraph of text in the email style.
     *
     * @param text the plain text, escaped before it is added.
     * @return the HTML of the paragraph.
     */
    private String paragraph(String text) {
        return "<p style=\"margin:0 0 16px 0; font-size:15px; line-height:1.7; color:#5c5c5c;\">"
                + HtmlUtils.htmlEscape(text) + "</p>";
    }

    /**
     * Builds the black button that opens a page of the website.
     *
     * @param text the text on the button.
     * @param url the full address the button opens.
     * @return the HTML of the button.
     */
    private String button(String text, String url) {
        return "<a href=\"" + HtmlUtils.htmlEscape(url) + "\" style=\"display:inline-block; padding:14px 34px; background-color:#1b1b1b;"
                + " color:#ffffff; text-decoration:none; font-family:Arial, Helvetica, sans-serif; font-size:12px;"
                + " font-weight:bold; letter-spacing:3px; text-transform:uppercase;\">" + HtmlUtils.htmlEscape(text) + "</a>";
    }

    /**
     * Sends one HTML email. A failure is only logged, so a mail problem never breaks a notification.
     *
     * @param to the email address of the receiver.
     * @param subject the subject line.
     * @param html the full HTML body.
     */
    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from, "Equestrian Center");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = the body is HTML.
            mailSender.send(message);
            log.info("Email '{}' sent to {}", subject, to);
        } catch (MessagingException | MailException | UnsupportedEncodingException ex) {
            log.error("Could not send email to {}: {}", to, ex.getMessage());
        }
    }
}