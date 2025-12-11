package com.f25_team6.duet.admin;

import com.f25_team6.duet.common.enums.UserRole;
import com.f25_team6.duet.user.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepo;

    @MockBean
    private TutorProfileRepository tutorProfileRepo;

    @MockBean
    private com.f25_team6.duet.booking.BookingRequestRepository bookingRequestRepo;
    @MockBean
    private com.f25_team6.duet.booking.ReviewRepository reviewRepo;
    @MockBean
    private com.f25_team6.duet.messaging.ConversationRepository conversationRepo;
    @MockBean
    private com.f25_team6.duet.messaging.MessageRepository messageRepo;
    @MockBean
    private com.f25_team6.duet.booking.LessonRepository lessonRepo;
    @MockBean
    private com.f25_team6.duet.booking.PaymentRepository paymentRepo;

    @Test
    public void listUsers_ShouldReturnUsers() throws Exception {
        User u = User.builder().id(1L).name("Test User").role(UserRole.STUDENT).build();
        given(userRepo.findAll()).willReturn(List.of(u));

        mvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUser_ShouldDeleteUserAndProfile() throws Exception {
        Long userId = 1L;
        given(userRepo.existsById(userId)).willReturn(true);
        TutorProfile profile = new TutorProfile(); // mocked profile
        given(tutorProfileRepo.findById(userId)).willReturn(Optional.of(profile));

        mvc.perform(delete("/api/admin/users/" + userId))
                .andExpect(status().isNoContent());

        verify(tutorProfileRepo).delete(profile);
        verify(userRepo).deleteById(userId);
    }

    @Test
    public void deleteAllUsers_ShouldDeleteAll() throws Exception {
        mvc.perform(delete("/api/admin/users"))
                .andExpect(status().isNoContent());

        verify(tutorProfileRepo).deleteAll();
        verify(userRepo).deleteAll();
    }

    @Test
    public void generateStudents_ShouldCreateUsers() throws Exception {
        int count = 5;
        // Mock findAll to return something so generated users are returned
        given(userRepo.findAll()).willReturn(Collections.emptyList());

        mvc.perform(post("/api/admin/generate/students").param("count", String.valueOf(count)))
                .andExpect(status().isOk());

        verify(userRepo, times(count)).save(any(User.class));
    }

    @Test
    public void generateTutors_ShouldCreateUsersAndprofiles() throws Exception {
        int count = 3;
        given(userRepo.findAll()).willReturn(Collections.emptyList());

        mvc.perform(post("/api/admin/generate/tutors").param("count", String.valueOf(count)))
                .andExpect(status().isOk());

        verify(userRepo, times(count)).save(any(User.class));
        verify(tutorProfileRepo, times(count)).save(any(TutorProfile.class));
    }
}
