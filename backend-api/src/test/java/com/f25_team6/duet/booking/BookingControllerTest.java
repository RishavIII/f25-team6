package com.f25_team6.duet.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f25_team6.duet.common.enums.BookingStatus;
import com.f25_team6.duet.user.User;
import com.f25_team6.duet.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingRequestRepository bookingRepo;

    @MockBean
    private LessonRepository lessonRepo;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserRepository userRepo; // Likely needed if Controller uses it, though BookingController doesn't seem to
                                     // based on previous view.

    @Test
    public void testGetTutorRequests() throws Exception {
        Long tutorId = 1L;
        BookingRequest mockReq = new BookingRequest();
        mockReq.setId(100L);
        when(bookingRepo.findByTutor_IdAndStatus(eq(tutorId), eq(BookingStatus.PENDING)))
                .thenReturn(List.of(mockReq));

        mockMvc.perform(get("/api/tutors/{tutorId}/booking-requests", tutorId)
                .param("status", "PENDING")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
