# ParkSmart

ParkSmart is a Spring Boot smart-parking application for time-window slot reservations, check-in/out tracking, and configurable overstay penalties.

## Features

- View parking lots and slots
- Search availability for an exact time window
- Prevent overlapping bookings for the same slot
- Check vehicles in and out
- Calculate and store overstay minutes and penalty amounts
- Responsive static dashboard served by Spring Boot
- JSON validation and error responses

## Stack And Structure

- Java 17 source compatibility, Spring Boot 4.1.1, Spring Web MVC
- Spring Data JPA, MySQL Connector/J, Jakarta Validation
- HTML, CSS, and JavaScript using the Fetch API
- Layered packages: `controller`, `service`, `repository`, `entity`, `dto`, `exception`, and `config`

The package root is `com.parksmart.parksmart`. The application entry point is [ParksmartApplication.java](src/main/java/com/parksmart/parksmart/ParksmartApplication.java).

## Database Setup

1. Install and start MySQL.
2. Create a user with access to the `parksmart` database, or use the local root account.
3. Set environment variables before starting the backend:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/parksmart?createDatabaseIfNotExist=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-real-password"
$env:PARKSMART_PENALTY_RATE = "5.00"
```

`DB_PASSWORD` has no checked-in default; set it in the environment before running the application. Never commit a real password. Hibernate uses `ddl-auto=update` for local development. Sample data creates one lot named `Main Parking Area` with slots `A01` through `A05` when the database is empty.

## Run

From the repository root on Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The backend and dashboard are available at `http://localhost:8080`. Open `http://localhost:8080/` for the frontend. No separate Live Server is needed, but Live Server origins `localhost:5500` and `127.0.0.1:5500` are allowed by CORS for the API.

For tests, an in-memory H2 database is configured under `src/test/resources/application.properties`, so MySQL is not required to run `mvn test`.

## API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST/GET | `/api/parking-lots` | Create or list lots |
| GET/PUT/DELETE | `/api/parking-lots/{id}` | Read, update, or delete a lot |
| POST/GET | `/api/slots` | Create or list slots |
| GET/PUT/DELETE | `/api/slots/{id}` | Read, update, or delete a slot |
| GET | `/api/slots/available?startTime=2026-09-28T10:00:00&endTime=2026-09-28T12:00:00` | Find available slots |
| POST/GET | `/api/bookings` | Create or list bookings |
| GET/PUT/DELETE | `/api/bookings/{id}` | Read, update, or cancel a booking |
| POST | `/api/check-in/{bookingId}` | Check in a booked vehicle |
| POST | `/api/check-out/{bookingId}` | Check out and calculate penalty |

Date-times use ISO local format: `yyyy-MM-dd'T'HH:mm:ss`.

## Booking And Penalty Rules

A booking overlaps an existing non-cancelled booking when:

```text
requestedStart < existingEnd AND requestedEnd > existingStart
```

An overlap returns HTTP `409 CONFLICT` with the message `Parking slot is already booked for the selected time window.` Endpoints reject missing resources with `404`, invalid input/state with `400`, and unexpected failures with a stable `500` response without stack traces.

At checkout, if actual checkout is later than booked end:

```text
overstayMinutes = minutes between booked end and actual checkout
penaltyAmount = overstayMinutes * PARKSMART_PENALTY_RATE
```

The default rate is `5.00` per minute and the response includes booking ID, vehicle, check-in time, booked end, actual checkout, overstay minutes, rate, penalty amount, and final status.

## Postman Test Order

1. `POST /api/parking-lots` with `{"name":"Main","location":"Campus","totalSlots":5}`.
2. `POST /api/slots` with `{"slotNumber":"B01","slotType":"CAR","parkingLotId":1}`.
3. `GET /api/slots`.
4. `GET /api/slots/available?startTime=2026-09-28T10:00:00&endTime=2026-09-28T12:00:00`.
5. `POST /api/bookings` with `{"userName":"John","vehicleNumber":"TN01AB1234","slotId":1,"startTime":"2026-09-28T10:00:00","endTime":"2026-09-28T12:00:00"}`.
6. Repeat step 5 for the same slot with an overlapping window; expect HTTP `409`.
7. `POST /api/check-in/{bookingId}`; expect status `CHECKED_IN`.
8. `POST /api/check-out/{bookingId}` after the booked end; expect `COMPLETED` and penalty details.
9. `GET /api/slots/{slotId}`; the slot status should be `AVAILABLE`.
10. `GET /api/bookings/{bookingId}`; the booking status should be `COMPLETED`.

To test no penalty, check out before or at the booked end. To reproduce the example, book until `18:00`, check out at `18:15`, and expect `15 * configured rate`.
