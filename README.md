# Title
> Duet

## Team Members
> Rishav Patel  
> Brian (Emre) Tekmen

## Description
> The motivation for our product is to connect students with music tutors so that they can learn instruments and skills of their choice. 
> Our product will allow students to browse tutor profiles, find the ones with the best reviews, convenience by location or skills, and then directly contact them for appointments.

## App functions
1. Customer (Rishav Patel):
    1. Create/modify customer profile - Register as a student
    2. Login authentication - Login to profile as a student with authentication
    3. View available services - Browse tutor profiles by keyowrds/location
    4. Subscribe to available services - Student should be able to directly contact a tutor, schedule appointments, and provide payment
    5. Write reviews for subscribed services - Student should be able review tutoring quality after one appointment, and dispute charges for scams/flag them.
    6. View subscriptions - Export appointments to google calendar.
2. Tutor/Provider (Brian Tekmen)
    1. Login Authentication - OAuth sign-in (Google/Apple) + email fallback.
    2. Create/modify tutor profile: fill out fields for name, email, phone, avatar, bio, instruments taught, genres, experience, languages, hourly rate, lesson durations offered, location/travel radius, availability, portfolio/resume
    3. Create/modify tutor session listings - Toggle listings active/inactive, set recurring availabilities and exceptions, define policies like cancellation window, lateness/no-show fees, travel surcharge
    4. Receive/Confirm/Deny Bookings - Bookings come with key details like student name, duration, date/time, location. Accept/decline with optional message. Allow for Google Calendar event exportation
    5. Direct Messaging - Chat with prospective/current students and share images/attachments
    6. Respond to Reviews - Write public responses to reviews. Reviews are only allowed for verified bookings.
    7. Payouts - Connect a payout account, view pending/available balances
    8. Disputes - Respond to payment disputes and upload counter-evidence. 
    9. Tutor Metrics - Lightweight dashboard: upcoming sessions, last 30-day earnings, response time, acceptance rate.
    10. Proof-of-Session: Generate dynamic QR codes at the start and end of a session. 

## 3rd Party API Integration
> **Zippopotamus** (https://api.zippopotam.us/)  
> Used to geocode US zipcodes for location-based features:
> - Automatic city/state population during tutor profile creation
> - Distance calculation for tutor search and filtering
> - Location validation during onboarding

## Tech Stack
- **Backend**: Java 21, Spring Boot, PostgreSQL
- **Frontend**: HTML, CSS, JavaScript
- **API**: RESTful API with JSON responses
