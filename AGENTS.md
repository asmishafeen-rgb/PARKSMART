# ParkSmart Agent Guide

## Repository Status

This repository is currently a minimal Spring Boot application. The existing coordinates are:

- Java: 17 (`<java.version>` in [pom.xml](pom.xml))
- Group/package root: `com.parksmart.parksmart`
- Artifact: `parksmart`
- Application entry point: `src/main/java/com/parksmart/parksmart/ParksmartApplication.java`
- Configuration: `src/main/resources/application.properties`

Preserve these coordinates unless a task explicitly requires a package or artifact migration. Do not create duplicate application classes or parallel package roots.

## Build And Test

Use the Maven wrapper from the repository root on Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Run the application with:

```powershell
.\mvnw.cmd spring-boot:run
```

Run a focused test with `-Dtest=ClassName#methodName`. Keep the existing context test passing after structural changes. If MySQL is required by the application context, document or provide a test-safe configuration rather than hiding startup failures.

## Target Architecture

Keep HTTP handling in `controller`, business rules in `service`, persistence in `repository`, persistence models in `entity`, transport models in `dto`, and cross-cutting setup in `config` or `exception`. Controllers should map requests and responses; they should not contain booking, availability, check-in, checkout, or penalty calculations.

Use Jakarta validation and imports, `Long` identifiers, `LocalDateTime` for booking lifecycle times, and `BigDecimal` for monetary values. Do not expose JPA entities as the public API when a DTO is appropriate. Keep database credentials out of source control; use a clearly documented local placeholder or environment/property override.

## ParkSmart Invariants

- A booking is valid only when its slot and time window exist and `startTime` is before `endTime`.
- Two bookings overlap when `requestedStart < existingEnd` and `requestedEnd > existingStart`. Reject an overlap with HTTP 409; never save it.
- Availability queries must exclude slots with overlapping non-cancelled bookings for the requested window.
- Check-in is allowed only for a `BOOKED` booking and records the actual check-in time.
- Checkout is allowed only after check-in. It completes the booking and makes the slot available again.
- Overstay is the positive duration from booked `endTime` to actual checkout, in minutes. Penalty is `overstayMinutes * configured penalty rate`, calculated with `BigDecimal` and returned in the checkout response.
- Invalid input, missing resources, state violations, and booking conflicts must produce stable JSON error responses without stack traces.

Use transactional service methods for booking creation and check-in/out state transitions. Keep the penalty rate in configuration or one dedicated constant/property, never scattered through service code.

## Frontend Boundary

The frontend is plain HTML, CSS, and JavaScript under `src/main/resources/static` (or the explicitly chosen frontend directory). Use `fetch` against the REST API, centralize readable error handling, and refresh affected dashboard/booking data after state-changing operations. Do not add a frontend framework unless explicitly requested. Keep CORS configuration centralized for Live Server origins.

## Change Workflow

1. Inspect the nearest controller/service/repository/test before editing.
2. Make the smallest layered change that preserves the invariants above.
3. Add or update focused tests for overlap rejection, lifecycle state transitions, and penalty calculation.
4. Run the narrowest relevant test first, then ` .\mvnw.cmd test` and ` .\mvnw.cmd package` from the repository root.
5. Do not commit, reset, or discard unrelated working-tree changes.

[HELP.md](HELP.md) contains the generated Spring Boot reference links. Add durable setup or API details to a project README when implementation work creates that documentation; link to it from future agent guidance rather than duplicating it here.
