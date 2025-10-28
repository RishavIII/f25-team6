# Duet API Documentation

## Common JSONs

### User

```json
{
  "id": 1,
  "name": "Alice Student",
  "email": "alice@student.test",
  "password": "alice123",
  "phone": "123-456-7890",
  "role": "STUDENT"  // or "TUTOR" 
}
```

### StudentProfile

```json
{
  "user": { "id": 1 },
  "contactPreferences": "email",
  "prefersOnline": true,
  "prefersInPerson": false
}
```

### TutorProfile

```json
{
  "user": { "id": 2 },
  "bio": "Saxophonist with 5y teaching.",
  "photoUrl": "https://...",
  "hourlyRateCents": 6000,
  "onlineEnabled": true,
  "inPersonEnabled": false,
  "latitude": 36.0726,
  "longitude": -79.7920,
  "city": "Greensboro",
  "state": "NC",
  "timezone": "America/New_York",
  "cancellationNote": "24h notice please"
}
```

### BookingRequest

```json
{
  "id": 10,
  "student": { "id": 1 },
  "tutor": { "id": 2 },
  "instrument": { "id": 5 },
  "level": "BEGINNER",   
  "durationMin": 60,
  "requestedStartUtc": "2025-10-30T19:00:00Z",
  "lessonMode": "ONLINE", 
  "notes": "Please focus on embouchure.",
  "status": "PENDING"    // or "ACCEPTED" / "DECLINED"
}
```

### Lesson

```json
{
  "id": 20,
  "bookingRequest": { "id": 10 },
  "student": { "id": 1 },
  "tutor": { "id": 2 },
  "instrument": { "id": 5 },
  "level": "BEGINNER",
  "startUtc": "2025-11-01T19:00:00Z",
  "durationMin": 60,
  "mode": "ONLINE",
  "status": "SCHEDULED"
}
```

### PaymentSummary 

```json
{
  "id": 33,
  "lessonId": 20,
  "studentId": 1,
  "amountCents": 6000,
  "status": "CAPTURED", 
  "processorRef": "pi_12345",
  "createdAt": "2025-11-01T18:00:02Z"
}
```

### Review

```json
{
  "id": 99,
  "lesson": { "id": 20 },
  "student": { "id": 1 },
  "tutor": { "id": 2 },
  "rating": 5,
  "text": "Great lesson!",
  "createdAt": "2025-11-01T21:00:00Z"
}
```

## Student API Endpoints

### Create User

```http
POST /api/users
Content-Type: application/json
{
  "name":"Alice Student",
  "email":"alice@student.test",
  "password":"alice123",
  "phone":"123-456-7890",
  "role":"STUDENT"
}
```

**Responses**

* 200 OK — created user
* 400 BAD_REQUEST — email missing
* 409 CONFLICT — email already in use

### Get User by ID

```http
GET /api/users/{id}
```

**Responses**

* 200 OK — user JSON
* 404 NOT_FOUND

### List Users

```http
GET /api/users
```

**Response**: 200 OK — `User[]`

### Update User

```http
PUT /api/users/{id}
Content-Type: application/json
{
  "email":"newalice@student.test", // optional
  "password":"newpass",            // optional
  "name":"Alice Updated",          // optional
  "phone":"336-555-7777",          // optional
  "role":"STUDENT"                 // optional
}
```

**Responses**

* 200 OK — updated user
* 404 NOT_FOUND — user not found
* 409 CONFLICT — email already in use

### Delete User

```http
DELETE /api/users/{id}
```

**Responses**

* 204 NO_CONTENT — deleted
* 404 NOT_FOUND
---

## Student Profiles

### Create Student Profile

```http
POST /api/student-profiles/{userId}
Content-Type: application/json
{
  "contactPreferences":"email",
  "prefersOnline": true,
  "prefersInPerson": false
}
```

**Responses**

* 200 OK — created profile
* 404 NOT_FOUND — user not found
* 409 CONFLICT — profile exists for user


### Get Student Profile

```http
GET /api/student-profiles/{userId}
```

**Responses**

* 200 OK — StudentProfile
* 404 NOT_FOUND

### Update Student Profile

```http
PUT /api/student-profiles/{userId}
Content-Type: application/json
{
  "contactPreferences":"sms",  // optional
  "prefersOnline": false,       // optional (bool fields default to false if omitted in this controller)
  "prefersInPerson": true       // optional
}
```

**Responses**

* 200 OK — updated profile
* 404 NOT_FOUND


### Delete Student Profile

```http
DELETE /api/student-profiles/{userId}
```

**Responses**

* 204 NO_CONTENT — deleted
* 404 NOT_FOUND


## Tutor Profiles

### Create Tutor Profile (by User ID)

```http
POST /api/tutor-profiles/{userId}
Content-Type: application/json
{
  "bio":"Saxophonist with 5y teaching.",
  "photoUrl":"https://...",
  "hourlyRateCents":6000,
  "onlineEnabled":true,
  "inPersonEnabled":false,
  "latitude":36.0726,
  "longitude":-79.7920,
  "city":"Greensboro",
  "state":"NC",
  "timezone":"America/New_York",
  "cancellationNote":"24h notice please"
}
```

**Responses**

* 200 OK — created profile
* 400 BAD_REQUEST — `hourlyRateCents` missing
* 404 NOT_FOUND — user not found
* 409 CONFLICT — profile exists


### Get Tutor Profile

```http
GET /api/tutor-profiles/{userId}
```

**Responses**

* 200 OK — TutorProfile
* 404 NOT_FOUND


### Update Tutor Profile

```http
PUT /api/tutor-profiles/{userId}
Content-Type: application/json
{
  "bio": "Updated bio",            // optional
  "photoUrl": "https://...",       // optional
  "hourlyRateCents": 6500,          // optional
  "onlineEnabled": true,            // optional
  "inPersonEnabled": true,          // optional
  "latitude": 36.1,                 // optional
  "longitude": -79.8,               // optional
  "city": "Greensboro",            // optional
  "state": "NC",                   // optional
  "timezone": "America/New_York",  // optional
  "cancellationNote": "48h notice"
}
```

**Responses**

* 200 OK — updated profile
* 404 NOT_FOUND

**curl**


### Delete Tutor Profile

```http
DELETE /api/tutor-profiles/{userId}
```

**Responses**

* 204 NO_CONTENT — deleted
* 404 NOT_FOUND
---

## Tutor Search


### Search Tutors

```http
GET /api/tutors/search?instrumentId={id}&online={bool}&inPerson={bool}&maxRate={cents}
```

**Query Params (all optional)**

* `instrumentId` (Long)
* `online` (Boolean) — filter tutors with `onlineEnabled = true`
* `inPerson` (Boolean) — filter tutors with `inPersonEnabled = true`
* `maxRate` (Integer) — max hourly rate in cents (e.g., 6000 = $60)

**Response**: 200 OK — `TutorProfile[]`

---

## Booking API

### Create Booking Request

```http
POST /api/booking-requests
Content-Type: application/json
{
  "studentId": 1,
  "tutorId": 2,
  "instrumentId": 5,
  "level": "BEGINNER",           // enum Level
  "durationMin": 60,
  "requestedStartUtc": "2025-10-30T19:00:00Z",
  "lessonMode": "ONLINE",        // enum LessonMode
  "notes": "Please focus on embouchure."
}
```

**Responses**

* 200 OK — created `BookingRequest`
* 400 BAD_REQUEST — validation failure (service-defined)
* 404 NOT_FOUND — referenced entities missing (service-defined)


### Get Booking Request

```http
GET /api/booking-requests/{id}
```

**Responses**

* 200 OK — `BookingRequest`
* 404 NOT_FOUND

### List Booking Requests

```http
GET /api/booking-requests
```

**Response**: 200 OK — `BookingRequest[]`

### Accept Booking Request

```http
POST /api/booking-requests/{id}/accept
```

**Response**: 200 OK — updated `BookingRequest` (status transitioned; implementation-defined)

### Decline Booking Request

```http
POST /api/booking-requests/{id}/decline
```

**Response**: 200 OK — updated `BookingRequest`

### Get Lesson

```http
GET /api/lessons/{id}
```

**Responses**

* 200 OK — `Lesson`
* 404 NOT_FOUND


### List Lessons

```http
GET /api/lessons
```

**Response**: 200 OK — `Lesson[]`


### Pay for Lesson

```http
POST /api/lessons/{lessonId}/pay?amountCents={int}
```

**Query Param**

* `amountCents` (int) — amount to charge in cents

**Response**: 200 OK — `PaymentSummary`


### Review a Lesson

```http
POST /api/lessons/{lessonId}/review
Content-Type: application/json
{
  "studentId": 1,
  "rating": 5,
  "text": "Great lesson!"
}
```

**Responses**

* 200 OK — `Review`
* 400 BAD_REQUEST — invalid rating, etc. (service-defined)
* 404 NOT_FOUND — lesson/student not found
---

## Error Responses (examples)

```json
{
  "timestamp": "2025-10-28T17:05:00.123+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email required",
  "path": "/api/users"
}
```

```json
{
  "timestamp": "2025-10-28T17:06:00.123+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Email in use",
  "path": "/api/users"
}
```