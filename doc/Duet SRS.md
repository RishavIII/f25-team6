# Software Requirements Specification
## For <project name>

Version 0.1  
Prepared by <author>  
<organization>  
<date created> 

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
|      |         |                     |           |
|      |         |                     |           |

## 1. Introduction

### 1.1 Document Purpose
Describe the purpose of the SRS and its intended audience.

### 1.2 Product Scope
Identify the product whose software requirements are specified in this document, including the revision or release number. Explain what the product that is covered by this SRS will do, particularly if this SRS describes only part of the system or a single subsystem. 
Provide a short description of the software being specified and its purpose, including relevant benefits, objectives, and goals. Relate the software to corporate goals or business strategies. If a separate vision and scope document is available, refer to it rather than duplicating its contents here.

### 1.3 Definitions, Acronyms and Abbreviations                                                                                                                                                                          |

### 1.4 References
List any other documents or Web addresses to which this SRS refers. These may include user interface style guides, contracts, standards, system requirements specifications, use case documents, or a vision and scope document. Provide enough information so that the reader could access a copy of each reference, including title, author, version number, date, and source or location.

### 1.5 Document Overview
Describe what the rest of the document contains and how it is organized.

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
- FR0 Accounts & Profiles

	 - FR0.1 Students (or Parents) can register/login via email/password; password reset is available.

	 - FR0.2 If the learner is under 13, a Parent account is required (age gate at signup).

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

	 - FR5.1 Pay for accepted lessons via integrated processor (e.g., Stripe PaymentIntents).

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


#### 3.1.1 User interfaces
Web pages using HTML, CSS, and JavaScript.

#### 3.1.2 Hardware interfaces
Devices that have web browser capabilities.

#### 3.1.3 Software interfaces
- Java jdk 21
- PostgreSQL 17
- SpringBoot 3.4.5

### 3.2 Non Functional Requirements 

#### 3.2.1 Performance
If there are performance requirements for the product under various circumstances, state them here and explain their rationale, to help the developers understand the intent and make suitable design choices. Specify the timing relationships for real time systems. Make such requirements as specific as possible. You may need to state performance requirements for individual functional requirements or features.

#### 3.2.2 Security
Specify any requirements regarding security or privacy issues surrounding use of the product or protection of the data used or created by the product. Define any user identity authentication requirements. Refer to any external policies or regulations containing security issues that affect the product. Define any security or privacy certifications that must be satisfied.

#### 3.2.3 Reliability
Specify the factors required to establish the required reliability of the software system at time of delivery.

#### 3.2.4 Availability
Specify the factors required to guarantee a defined availability level for the entire system such as checkpoint, recovery, and restart.

#### 3.2.5 Compliance
Specify the requirements derived from existing standards or regulations

#### 3.2.6 Cost
Specify monetary cost of the software product.

#### 3.2.7 Deadline
Specify schedule for delivery of the software product.
