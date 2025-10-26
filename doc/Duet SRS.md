# Software Requirements Specification
## For Duet – Local Music Tutor Finder

Version 0.1  
Prepared by Brian Tekmen and Rishav Patel
CSC340
September 10, 2025

Table of Contents
=================
* [Revision History](#revision-history)
* 1 [Introduction](#1-introduction)
  * 1.1 [Document Purpose](#11-document-purpose)
  * 1.2 [Product Scope](#12-product-scope)
  * 1.3 [Definitions, Acronyms and Abbreviations](#13-definitions-acronyms-and-abbreviations)
  * 1.4 [References](#14-references)
  * 1.5 [Document Overview](#15-document-overview)
* 2 [Product Overview](#2-product-overview)
  * 2.1 [Product Functions](#21-product-functions)
  * 2.2 [Product Constraints](#22-product-constraints)
  * 2.3 [User Characteristics](#23-user-characteristics)
  * 2.4 [Assumptions and Dependencies](#24-assumptions-and-dependencies)
* 3 [Requirements](#3-requirements)
  * 3.1 [Functional Requirements](#31-functional-requirements)
    * 3.1.1 [User Interfaces](#311-user-interfaces)
    * 3.1.2 [Hardware Interfaces](#312-hardware-interfaces)
    * 3.1.3 [Software Interfaces](#313-software-interfaces)
  * 3.2 [Non-Functional Requirements](#32-non-functional-requirements)
    * 3.2.1 [Performance](#321-performance)
    * 3.2.2 [Security](#322-security)
    * 3.2.3 [Reliability](#323-reliability)
    * 3.2.4 [Availability](#324-availability)
    * 3.2.5 [Compliance](#325-compliance)
    * 3.2.6 [Cost](#326-cost)
    * 3.2.7 [Deadline](#327-deadline)

## Revision History
| Name | Date    | Reason For Changes  | Version   |
| ---- | ------- | ------------------- | --------- |
|Rishav|9/10/2025|Initial SRS Section 2|1.0        |
|  Emre | 9/10/2025      |    Init Section 1                 |     0.1      |

## 1. Introduction

### 1.1 Document Purpose
The purpose of this Software Requirements Specification (SRS) is to describe client-view and developer-view requirements for Duet, a web application that connects local music tutors with students (or parents). The client view captures user goals and use cases; the developer view specifies functional behavior, data, performance, and quality attributes required to implement the system.

### 1.2 Product Scope
Duet helps students/parents discover, evaluate, and book local private music lessons (in-person or online). Tutors publish detailed profiles, availability, instruments taught, travel radius, and rates. Students search by location, instrument, level, and schedule; book lessons; message tutors; and leave reviews. An admin console enables moderation and dispute handling. Primary objectives:

- Reduce search friction for trustworthy, nearby tutors.

- Provide simple, reliable scheduling and payments.

- Support safe communication and reputation via reviews and responses.


### 1.3 Definitions, Acronyms and Abbreviations    
| Reference             | Definition                                                                           |
| --------------------- | ------------------------------------------------------------------------------------ |
| **Tutor**             | A music teacher offering paid lessons.                                               |
| **Student**           | A learner seeking lessons (may be represented by a **Parent** account).              |
| **Admin**             | Platform operator with moderation/oversight privileges.                              |
| **Lesson**            | A scheduled session between a tutor and a student (trial or standard).               |
| **Availability**      | Tutor’s time slots open for booking, including recurring patterns and buffers.       |
| **RBAC**              | Role-Based Access Control.                                                           |
| **JWT**               | JSON Web Token for stateless session auth.                                           |
| **REST API**          | Backend HTTP interface for the web client.                                           |
| **WebSocket**         | Bi-directional channel for real-time messaging/notifications.                        |
| **PCI-DSS**           | Payment Card Industry Data Security Standard (handled via payment processor).        |
| **COPPA**             | U.S. Children’s Online Privacy Protection Act (requirements when serving users <13). |
| **Java**              | Backend implementation language.                                                     |
| **Spring Boot**       | Java framework used to create the backend service.                                   |
| **PostgreSQL**        | Relational database used to store application data.                                  |
| **Map Geocoding API** | Service to geocode addresses and compute distances (e.g., Mapbox/Google).            |
| **Stripe**            | Third-party payment processor (or equivalent).                                       |

### 1.4 References
Spring Guides & Docs

PostgreSQL Documentation

Stripe API Docs (Payments, PaymentIntents)

Mapbox/Google Maps Geocoding & Distance APIs

OWASP ASVS & Top 10 (security baseline)
### 1.5 Document Overview
Section 1 is a general introduction to the document, intended for any readers. Section 2 describes Duet’s product perspective and major features for stakeholders. Section 3 details functional and non-functional requirements for the engineering team.

## 2. Product Overview
 Duet is a web-based platform designed to help students discover and make appointments with music tutors. Students can leave reviews based on the quality of their experience with tutors. Tutors showcase their profile, mange their appointments, and track student engagement. The system supports the user roles for students and tutors, each with tailored services to ensure a seamless market between tutor and student.

### 2.1 Product Functions
Duet allows tutors to post and customize their profiles. They can manage and track their calendar of appointments from the website. Students can look for and schedule appointments to any tutor of their choosing based on convenience by price, location, and skill from the dashboard.

### 2.2 Product Constraints
At this point, the program will only run on a computer with Java jdk 21 installed. The full scope of the project is hopefully realized, however the team has a deadline of about 10 weeks, which could lead to feature cuts. The program would have a challenge scaling, as the current plan is to use a free version of a Postgresql database to store the information.
  
### 2.3 User Characteristics
Our website application does not expect our users to have any prior knowledge of a computer, apart from using a web browser. As long as users know what skills or music intruments they are interested in, they should be experts within several uses of the application.

### 2.4 Assumptions and Dependencies
We will be using Java, with our program being dependent on Spring & SpringBoot, and RestAPI to connect to external APIs and developed with VS Code. The application will also use an external location API that will help the student schedule the appointment with the most convenience.

## 3. Requirements

### 3.1 Functional Requirements 

3.1.A Student / Customer

- FR0 Accounts & Profiles

	 - FR0.1 Students (or Parents) can register/login via email/password; password reset is available.


	 - FR0.3 Students can edit profile info (name, preferred instruments/levels, contact preferences).

- FR1 Search & Discovery

	 - FR1.1 Search tutors by instrument, level, distance/radius (geocoded), price range, rating, availability, online/in-person.

	 - FR1.2 Sort results by relevance, distance, price, or rating.

	 - FR1.3 View results as list (MVP); map view is optional post-MVP.

- FR2 Tutor Profiles

	 - FR2.1 Open a tutor profile showing bio, photo, instruments/levels, genres, rates, travel radius, in-person/online options, credentials, cancellation policy, reviews/ratings.

	 - FR2.2 See next available time slots if the tutor exposes availability.

- FR3 Messaging

	 - FR3.1 Start and continue in-app message threads with tutors.

	 - FR3.2 Report/flag abusive messages.

- FR4 Booking Requests

	 - FR4.1 Create a booking request selecting instrument, duration, date/time, lesson mode (online/in-person) and location if needed.

	 - FR4.2 See status changes: Pending → Accepted/Declined or Alt-Time Proposed.

	 - FR4.3 Receive confirmations and calendar details when accepted.

- FR5 Payments & Checkout

	 - FR5.1 Pay for accepted lessons (mock payment)

	 - FR5.2 View totals, fees, and refund policy before paying.

	 - FR5.3 Receive receipt/confirmation after successful payment.

- FR6 Reschedule & Cancel

	 - FR6.1 Request reschedule/cancel according to tutor policy and time buffers.

	 - FR6.2 System calculates applicable refunds/fees automatically and displays them prior to confirmation.
- FR7 Reviews & Ratings

	 - FR7.1 After a completed lesson, submit a 1–5 star rating and optional text review.

	 - FR7.2 Flag inappropriate reviews for admin moderation.

- FR8 Notifications

	 - FR8.1 Receive email notifications (and optional SMS later) for new messages, booking decisions, payment results, and upcoming lesson reminders (e.g., 24h prior).

- FR9 Data Portability

	 - FR9.1 Export basic profile and booking history (CSV/JSON) in v1.1.

3.1.B Tutor / Provider

- FR10 Accounts, Onboarding & RBAC

	 - FR10.1 Register/login as Tutor; complete onboarding wizard.

	 - FR10.2 Role-based permissions restrict access to tutor-only features.

- FR11 Profile Management

	 - FR11.1 Create/edit profile: bio, photo, instruments taught, levels, genres, hourly rate(s), travel radius, studio address, online/in-person options, credentials.

	 - FR11.2 Define cancellation policy and lesson buffers (prep/commute).

- FR12 Availability & Calendar

	 - FR12.1 Define recurring availability (weekly patterns) and blackout dates.

	 - FR12.2 System generates bookable slots; prevent double-booking.

	 - FR12.3 Time-zone-aware display; optional iCal read-only export in v1.1.

- FR13 Booking Management

	 - FR13.1 View incoming booking requests; Accept, Decline, or Propose alternate times.

	 - FR13.2 On Accept, the system creates a Lesson record and notifies the student.

	 - FR13.3 Modify or cancel per policy; system computes fees/refunds.

- FR14 Messaging

	 - FR14.1 Message students within the platform; receive new-message notifications.

	 - FR14.2 Mark/report abusive content.

- FR15 Pricing, Payments & Payouts

	 - FR15.1 Set lesson rates (optionally by duration or level).

	 - FR15.2 Connect a payout account with the payment processor.

	 - FR15.3 View payment status for lessons (authorized/captured/refunded).

- FR16 Reviews & Reputation

	 - FR16.1 View received reviews/ratings.

	 - FR16.2 Post one public response per review.

	 - FR16.3 Flag abusive reviews for admin moderation.

- FR17 Insights & Reporting

	 - FR17.1 Dashboard with upcoming lessons, monthly earnings, cancellations.

	 - FR17.2 Export lesson and payout history (CSV/JSON) in v1.1.

- FR18 Notifications

	 - FR18.1 Email notifications (optional SMS later) for new requests, messages, schedule changes, and payout events.

#### 3.1.1 User interfaces
Web pages using HTML, CSS, and JavaScript.

#### 3.1.2 Hardware interfaces
Devices that have web browser capabilities.

#### 3.1.3 Software interfaces
- Java jdk 21
- PostgreSQL 17
- SpringBoot 3.4.5

### 3.2 Non Functional Requirements 
3.2.1 Performance

- NFR0: Search response time less than 0.5 seconds on average

- NFR1: Booking confirmation less than 2 seconds

- NFR2: First meaningful connections < 2.5 seconds

- NFR3: Message delivery latency < 1 second

3.2.2 Security

- NFR4: All traffic served over HTTPS

- NFR5: Passwords will be salted and hashed.

- NFR6: tutors cannot access other tutors’ earnings/availability data.

- NFR7: Payment data never stored on Duet servers; handled by Stripe.

- NFR8: Sending requests will be rate-limited

- NFR9: Basic censorship of vulgar words

- NFR10: Privacy for minors: no public contact details; parent account required for those under 13.

3.2.3 Reliability

- NFR11: Consistently back up data

- NFR12: Payments will retry until success.

- NFR13: If message transmissions fail, they will be stored and queued for delivery

3.2.4 Availability

- NFR14: Should be upt 99.99% of the time.

- NFR15: Maintenance will occur during off hours.

3.2.5 Compliance

- NFR16: Use stripe for payment processing safety

- NFR17: Create a privacy policy and terms of service

- NFR18: Follow generic accessibility guidlines

3.2.6 Cost

- NFR19: This application should cost 0 dollars.

3.2.7 Deadline

- NFR20: The final product will be delivered by December 2025.