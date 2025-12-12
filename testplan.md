# Duet Requirements Testing
## Actors
- Tutor (Provider)
- Student (Customer) - Simulated via Admin Page

### Use Cases
#### 1. Tutor: Create tutor profile use case:
1. Tutor T1 navigates to the login page and registers a new account with email and password.
2. T1 is redirected to the onboarding page and completes their profile with bio, instruments taught, hourly rate, and zipcode.
3. The Zippopotamus API is called to validate the zipcode and auto-fill city and state.
4. T1 clicks Save and is redirected to the Tutor Dashboard. T1 exits.

#### 2. Student: Create student profile (simulated):
1. Admin navigates to /admin.html.
2. Admin clicks Generate next to Generate Students (count of 1) to create Student S1.
3. S1 appears in the User Management table with role STUDENT.

#### 3. Student: Book a lesson with tutor (simulated):
1. Admin locates the Booking Simulator section on /admin.html.
2. Admin selects S1 from the Student dropdown and T1 from the Tutor dropdown.
3. Admin selects an instrument (e.g., Piano), sets a future date and time.
4. Admin clicks Request Lesson. Booking request is created with status PENDING.

#### 4. Tutor: Accept booking request use case:
1. T1 logs in and views the Dashboard.
2. T1 sees the booking request from S1 in the Incoming Requests section.
3. T1 clicks Accept to confirm the lesson.
4. The lesson moves to Upcoming Lessons on the Dashboard.

#### 5. Student: Send message to tutor (simulated):
1. Admin navigates to /admin.html Messaging Utility section.
2. Admin selects S1 in From Users and T1 in To Users.
3. Admin types a message and clicks Send Message.

#### 6. Tutor: Reply to message use case:
1. T1 logs in and navigates to Messages.
2. T1 sees the conversation with S1 and reads the message.
3. T1 types a reply and clicks Send. T1 exits.

#### 7. Tutor: View calendar use case:
1. T1 logs in and navigates to Calendar.
2. T1 views the scheduled lesson on the appropriate date.
3. T1 clicks on the lesson to view details.

#### 8. Tutor: View notifications use case:
1. T1 logs in and clicks the Notifications icon.
2. T1 views notifications for booking confirmations and new messages.
3. T1 clicks a notification to view details.

#### 9. Tutor: View customer statistics use case:
1. T1 logs in and views the Dashboard.
2. T1 observes the Metrics section showing Monthly Earnings, Click-through Rate, and Overall Rating.
3. T1 observes the Student Stats section showing number of Students, Lessons this month, and Average lesson duration.

#### 10. Tutor: Modify profile use case:
1. T1 logs in and navigates to Profile.
2. T1 clicks Edit Profile and modifies the bio text.
3. T1 enters a new zipcode and the Zippopotamus API auto-fills city and state.
4. T1 clicks Save and verifies the updated profile is displayed. T1 exits.

#### 11. 3rd Party API: Filter tutors by location (via Admin):
1. Admin navigates to /admin.html and locates the Tutor Location Search section.
2. Admin enters a zipcode (e.g., 27412) in the Search Zipcode field.
3. The Zippopotamus API is called to geocode the zipcode, returning latitude and longitude.
4. Admin sets Max Distance to 25 miles and clicks Search Tutors.
5. The system displays tutors within 25 miles, sorted by distance, showing the API response data.
