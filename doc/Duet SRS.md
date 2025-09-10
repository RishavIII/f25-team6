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
This section should describe the general factors that affect the product and its requirements. This section does not state specific requirements. Instead, it provides a background for those requirements, which are defined in detail in Section 3, and makes them easier to understand.

### 2.1 Product Functions
Summarize the major functions the product must perform or must let the user perform. Details will be provided in Section 3, so only a high level summary (such as a bullet list) is needed here. Organize the functions to make them understandable to any reader of the SRS. A picture of the major groups of related requirements and how they relate, such as a top level data flow diagram or object class diagram, is often effective.

### 2.2 Product Constraints
This subsection should provide a general description of any other items that will limit the developer’s options. These may include:  

* Interfaces to users, other applications or hardware.  
* Quality of service constraints.  
* Standards compliance.  
* Constraints around design or implementation.
  
### 2.3 User Characteristics
Identify the various user classes that you anticipate will use this product. User classes may be differentiated based on frequency of use, subset of product functions used, technical expertise, security or privilege levels, educational level, or experience. Describe the pertinent characteristics of each user class. Certain requirements may pertain only to certain user classes. Distinguish the most important user classes for this product from those who are less important to satisfy.

### 2.4 Assumptions and Dependencies
List any assumed factors (as opposed to known facts) that could affect the requirements stated in the SRS. These could include third-party or commercial components that you plan to use, issues around the development or operating environment, or constraints. The project could be affected if these assumptions are incorrect, are not shared, or change. Also identify any dependencies the project has on external factors, such as software components that you intend to reuse from another project, unless they are already documented elsewhere (for example, in the vision and scope document or the project plan).

## 3. Requirements

### 3.1 Functional Requirements 
This section specifies the software product's requirements. Specify all of the software requirements to a level of detail sufficient to enable designers to design a software system to satisfy those requirements, and to enable testers to test that the software system satisfies those requirements.

The specific requirements should:
* Be uniquely identifiable.
* State the subject of the requirement (e.g., system, software, etc.) and what shall be done.
* Optionally state the conditions and constraints, if any.
* Describe every input (stimulus) into the software system, every output (response) from the software system, and all functions performed by the software system in response to an input or in support of an output.
* Be verifiable (e.g., the requirement realization can be proven to the customer's satisfaction)
* Conform to agreed upon syntax, keywords, and terms.

#### 3.1.1 User interfaces
Define the software components for which a user interface is needed. Describe the logical characteristics of each interface between the software product and the users. This may include sample screen images, any GUI standards or product family style guides that are to be followed, screen layout constraints, standard buttons and functions (e.g., help) that will appear on every screen, keyboard shortcuts, error message display standards, and so on. Details of the user interface design should be documented in a separate user interface specification.

Could be further divided into Usability and Convenience requirements.

#### 3.1.2 Hardware interfaces
Describe the logical and physical characteristics of each interface between the software product and the hardware components of the system. This may include the supported device types, the nature of the data and control interactions between the software and the hardware, and communication protocols to be used.

#### 3.1.3 Software interfaces
Describe the connections between this product and other specific software components (name and version), including databases, operating systems, tools, libraries, and integrated commercial components. Identify the data items or messages coming into the system and going out and describe the purpose of each. Describe the services needed and the nature of communications. Refer to documents that describe detailed application programming interface protocols. Identify data that will be shared across software components. If the data sharing mechanism must be implemented in a specific way (for example, use of a global data area in a multitasking operating system), specify this as an implementation constraint.

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
