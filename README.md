# equestrian-mangement-system
A secure Spring Boot REST API for an equestrian centre, where clients book riding sessions with instructors.

## Project Information

### Description
The Equestrian Centre API is a backend application for managing riding sessions, instructors, horses and client bookings. Clients register, verify their email with a one-time code, browse sessions and book a place. Instructors and admins manage sessions and bookings, and everyone receives real-time notifications about changes.

### Purpose
Equestrian centres often manage bookings by phone or messages, which causes double bookings and confusion about who is attending which session. This API centralises sessions, capacity and bookings, and enforces the booking rules automatically.

### Users
- **CLIENT:** registers, books sessions, views and cancels their own bookings, manages their own profile.
- **INSTRUCTOR:** creates and updates sessions, confirms and manages bookings.
- **ADMIN:** manages users (including deactivating and reactivating accounts), and has instructor permissions.

### Main Features
- Registration with email verification by a 6-digit code
- JWT login with role-based authorization (CLIENT, INSTRUCTOR, ADMIN)
- Forgot password (code, then reset token) and change password
- User profile update with profile picture upload
- Admin user management with soft delete (`INACTIVE`) and reactivation
- Riding sessions with capacity, status and filtering (status, skill level, date)
- Booking workflow with status transitions and double-booking prevention
- Horse management with image upload
- Real-time notifications using Server-Sent Events (SSE)
- Global exception handling with a consistent JSON error structure
- Input validation, DTOs and logging

### Technologies
- Java 17
- Spring Boot (Web, Data JPA, Security, Validation, Mail)
- Spring Security with JWT (JJWT)
- PostgreSQL
- Hibernate / JPA
- Lombok
- Maven
- Server-Sent Events (`SseEmitter`)
- Git / GitHub, Trello, Postman

## Architecture

The application uses a layered architecture:

```
Controller  ->  Service  ->  Repository  ->  PostgreSQL
```

| Package | Responsibility |
|---|---|
| `controller` | Receives HTTP requests, validates input, returns responses |
| `service` | Business rules and workflows (booking rules, verification codes, notifications) |
| `repository` | Spring Data JPA repositories |
| `model.entity` / `model.enums` | Database entities and enums |
| `dto.request` / `dto.response` | Objects exchanged with API clients (entities are never exposed) |
| `mapper` | Converts entities into response DTOs |
| `security` | JWT utility, request filter, user details, security configuration |
| `exception` | Custom exceptions and the global exception handler |
| `sse` | Registry of open SSE connections per user |

### Main components
- **JwtRequestFilter:** reads the `Authorization: Bearer` header, validates the token and signs the user in.
- **VerificationService:** creates hashed one-time codes with `SecureRandom`, limits wrong attempts and expiry.
- **BookingService:** enforces the booking rules and status transitions.
- **NotificationService and SseEmitterRegistry:** save notifications and push them to connected clients.
- **GlobalExceptionHandler:** turns every exception into the same JSON error structure.
- **FileStorageService:** validates and stores uploaded images.

## General Approach

I started by designing the entities and their relationships (users, instructors, horses, riding sessions, bookings, verification codes and notifications), then built the layers from the repository up to the controllers. Each feature was developed on its own branch and merged into `develop`, so the Git history follows the order the application was built.

Security was added around the working API: passwords are hashed with BCrypt, login returns a JWT that carries the email and role, and a filter authorizes each request. Roles are enforced with `@PreAuthorize`, and ownership rules are checked in the service layer (for example, a client can only read their own bookings). Email verification and password recovery use hashed one-time codes with an expiry and a limit on wrong attempts.

The booking workflow is the core of the application. A booking starts as `PENDING` and can move to `CONFIRMED` and `COMPLETED`, or to `CANCELLED`. Cancelling is a soft delete, so the history is kept and the spot is released. Double booking is prevented by checking duplicate bookings, session capacity and overlapping sessions for the same user. Real-time notifications use SSE: a client opens a stream, and the server pushes an event when a booking is confirmed or cancelled, or a session is updated.

### Business rules
- A user cannot book the same session twice.
- A session must be `AVAILABLE` and in the future to be booked.
- A session becomes `FULL` when capacity is reached, and `AVAILABLE` again when a booking is cancelled.
- A user cannot hold two bookings that overlap in time on the same date.
- Booking status changes follow fixed transitions: `PENDING` to `CONFIRMED` or `CANCELLED`, `CONFIRMED` to `COMPLETED` or `CANCELLED`. `CANCELLED` and `COMPLETED` are final.
- Clients can only view and cancel their own bookings. Only instructors and admins can confirm or complete them.
- Inactive and unverified users cannot log in or use protected routes.
- An admin cannot deactivate their own account.
- To change a booking's session, cancel it and book again.

## ERD
https://lucid.app/lucidchart/c937fbbc-8158-4a06-a4d1-cc7a851c59d9/edit?view_items=8jqyXhTwWiA5%2C5cqyAMFbiZ-8%2CXbqy-eWVcvKE%2C7iqyX~YwnAUw%2CN.pyBmwiDbHs%2CyiqyC.mVCMmG%2CxkqyTG4bMP7a%2CigqycHs-ie2b%2CthqyYA21USwp%2CO.pysFVEUh75%2C4qqyOQIR5N_r%2Cspqyc05hPhFl%2CWrqyK97-n5Fa%2CNpqyOn-wzETb%2CErqyOqUGF3Lp%2CpnqyNVTLLp.L%2Cdoqy-bso8xwP%2CnrqypBUN.VRO%2Ccqqy8UFLOZqK%2CGqqyUAzcB7f7&page=0_0&invitationId=inv_7aa50328-8a8a-4d16-b095-657b91f129f4

## API Documentation

Interactive documentation: http://localhost:8000/swagger-ui/index.html#/

### Authentication

| Request Type | URL | Functionality | Access |
|---|---|---|---|
| POST | `/auth/users/register` | Register a new client | Public |
| POST | `/auth/users/verify` | Verify email with the code | Public |
| POST | `/auth/users/login` | Log in and receive a JWT | Public |
| POST | `/auth/users/forgot-password` | Send a password reset code | Public |
| POST | `/auth/users/verify-reset-code` | Check the reset code and receive a reset token | Public |
| POST | `/auth/users/reset-password` | Set a new password using the reset token | Public |
| PUT | `/auth/users/change-password` | Change my password | Private |

### Users

| Request Type | URL | Functionality | Access |
|---|---|---|---|
| PUT | `/api/users/me` | Update my profile and picture (form-data) | Private |
| GET | `/api/users` | List users, optional `userStatus` filter | Admin |
| GET | `/api/users/{id}` | Get a user | Admin |
| PUT | `/api/users/{id}` | Update any user's profile | Admin |
| DELETE | `/api/users/{id}` | Deactivate a user (soft delete) | Admin |
| PUT | `/api/users/{id}/activate` | Reactivate a user | Admin |

### Instructors

| Request Type | URL | Functionality | Access |
|---|---|---|---|
| POST | `/api/instructors/create` | Create an instructor profile | Private |
| GET | `/api/instructors` | List instructors | Private |
| GET | `/api/instructors/{id}` | Get an instructor | Private |
| PUT | `/api/instructors/{id}` | Update an instructor | Private |
| DELETE | `/api/instructors/{id}` | Delete an instructor | Private |

### Horses

| Request Type | URL | Functionality | Access |
|---|---|---|---|
| POST | `/api/horses/create` | Create a horse (form-data with image) | Private |
| GET | `/api/horses/all` | List horses | Private |
| GET | `/api/horses/{id}` | Get a horse | Private |
| PUT | `/api/horses/update/{id}` | Update a horse | Private |
| DELETE | `/api/horses/delete/{id}` | Delete a horse | Private |

### Riding Sessions

| Request Type | URL | Functionality | Access |
|---|---|---|---|
| POST | `/api/sessions/create` | Create a session | Instructor, Admin |
| GET | `/api/sessions` | List sessions, optional `sessionStatus`, `skillLevel`, `date` (yyyy-MM-dd) filters | Public |
| GET | `/api/sessions/{id}` | Get a session | Public |
| PUT | `/api/sessions/{id}` | Update a session (partial) | Instructor, Admin |
| DELETE | `/api/sessions/{id}` | Delete a session | Instructor, Admin |

### Bookings

| Request Type | URL | Functionality | Access |
|---|---|---|---|
| POST | `/api/bookings` | Create a booking (staff may pass `userId`) | Private |
| GET | `/api/bookings` | My bookings (staff see all), optional `status` filter | Private |
| GET | `/api/bookings/{id}` | Get a booking (clients: own only) | Private |
| PUT | `/api/bookings/{id}` | Change booking status | Private (clients can only cancel) |
| DELETE | `/api/bookings/{id}` | Cancel a booking (soft delete) | Private |

### Notifications

| Request Type | URL | Functionality | Access |
|---|---|---|---|
| GET | `/api/notifications/stream` | Open the real-time SSE stream | Private |
| GET | `/api/notifications` | List my notifications, newest first | Private |

> Keep this table consistent with the code. Update the Access column if you change the security configuration.

### Demonstrating real-time notifications
1. In Postman (or `curl -N -H "Authorization: Bearer <token>" http://localhost:8000/api/notifications/stream`), open the stream with a client's token. The request stays open.
2. With an instructor or admin token, send `PUT /api/bookings/{id}` with `{ "bookingStatus": "CONFIRMED" }`.
3. The client's stream receives a `BOOKING_CONFIRMED` event immediately, without another request.

## Installation

### Prerequisites
- Java 17
- Maven
- PostgreSQL
- A mail account for sending verification codes (for example a Gmail app password, or a development mail service)

### Steps
1. **Clone the repository**
   ```
   git clone ADD_REPOSITORY_URL_HERE
   cd equestrian
   ```
2. **Create the database**
   ```
   createdb equestrian
   ```
3. **Configure environment variables** (never commit real values)

   | Variable | Purpose |
   |---|---|
   | `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/equestrian` |
   | `DB_USERNAME` | Database user |
   | `DB_PASSWORD` | Database password |
   | `JWT_SECRET` | Secret used to sign tokens (at least 32 characters) |
   | `MAIL_USERNAME` | Sender email address |
   | `MAIL_PASSWORD` | Mail password or app password |
   | `FILE_UPLOADS_DIR` | Folder where uploaded images are stored |

   Match these names to the placeholders used in `application-dev.yml`.
4. **Choose the profile**
   - Development: `spring.profiles.active=dev`
   - Test: `spring.profiles.active=test`
5. **Seed the database**
   - ADD_SEED_DESCRIPTION_HERE (the seed data creates an admin, sample clients, instructors, horses, sessions and bookings when the application first starts).
6. **Start the application**
   ```
   mvn spring-boot:run
   ```
7. **Access the API:** `http://localhost:8000`
8. **Access Swagger/OpenAPI:**   `http://localhost:8000/swagger-ui/index.html#/`

## User stories
Client

Account
As a visitor, I want to register an account, so that I can book riding sessions.
As a new user, I want to verify my email with a code, so that my account is activated and I can log in.
As a client, I want to log in, so that I can access my bookings securely.
As a client who forgot my password, I want to receive a reset code by email, so that I can recover my account.
As a client, I want to change my password, so that I can keep my account secure.
As a client, I want to update my name, phone number and profile picture, so that my details stay correct.
As a client, I want to browse available riding sessions, so that I can choose one that suits me.
As a client, I want to filter sessions by status, skill level or date, so that I can find a suitable session quickly.
As a client, I want to view the details of a session, so that I know the time, price and level before booking.
As a client, I want to book an available session, so that I can reserve my place.
As a client, I want to be prevented from booking a full session, so that sessions are never overbooked.
As a client, I want to be prevented from booking two overlapping sessions, so that I do not double-book myself.
As a client, I want to view my bookings and filter them by status, so that I can track my reservations.
As a client, I want to cancel my booking, so that I can free my spot when I cannot attend.
As a client, I want to see only my own bookings, so that my information stays private.
As a client, I want to receive a real-time notification when my booking is confirmed or cancelled, so that I know its status without refreshing.
As a client, I want to view my past notifications, so that I do not miss anything that happened while I was offline.
As a client, I want to receive an email when I register or reset my password, so that I can complete those actions.
As an instructor, I want to create riding sessions with a date, time, price, skill level and capacity, so that clients can book them.
As an instructor, I want to update my sessions, so that I can correct details or reschedule.
As an instructor, I want to be notified when my session is updated by someone else, so that I always know what changed.
As an instructor, I want to confirm or complete bookings, so that clients know their reservation is accepted.
As an instructor, I want to book a session on behalf of a client, so that I can help clients who ask in person.
As an instructor, I want to view all bookings, so that I can prepare for my sessions.
As an admin, I want to view all users and filter them by status, so that I can manage the centre’s accounts.
As an admin, I want to deactivate a user without deleting their data, so that I can block access while keeping the history.
As an admin, I want to reactivate an inactive user, so that I can restore access when needed.
As an admin, I want to update any user’s profile, so that I can correct mistakes for clients.
As an admin, I want to create and manage instructor profiles, so that the right people can run sessions.
As an admin, I want to manage horses and their images, so that the centre’s stable is documented.
As an admin, I want to create, update and cancel any session, so that I can run the centre’s schedule.
As an admin, I want to change any booking’s status, so that I can resolve problems for clients.
As a system owner, I want passwords stored as hashes, so that a data leak does not expose real passwords.
As a system owner, I want protected routes to require a valid JWT, so that only signed-in users can use them.
As a system owner, I want roles to control access to endpoints, so that clients cannot use admin features.
As a system owner, I want unverified and inactive users blocked from logging in, so that only valid accounts use the system.
As a system owner, I want one-time codes to expire and lock after wrong attempts, so that they cannot be guessed.
As a system owner, I want uploaded files validated by type and size, so that harmful files are rejected.
As a system owner, I want consistent JSON error responses, so that API users understand what went wrong.
As a developer, I want the API documented with Swagger, so that I can understand and test the endpoints.

## Credits & External Resources

| Resource | URL | Used for |
|---|---|---|
| Spring Boot Documentation | https://docs.spring.io/spring-boot/ | Project setup and configuration |
| Spring Security Documentation | https://docs.spring.io/spring-security/reference/ | JWT filter, method security |
| Spring Data JPA Documentation | https://docs.spring.io/spring-data/jpa/reference/ | Derived query methods |
| JJWT | https://github.com/jwtk/jjwt | Creating and validating JWTs |
| Claude (Anthropic) | https://claude.ai | Code reviews, explanations and suggestions; I tested and can explain all submitted code |
| ADD_MORE | ADD_URL | ADD_PURPOSE |

