package com.f25_team6.duet.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TutorProfileController.class)
public class TutorProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TutorProfileRepository repo;

    @MockBean
    private UserRepository userRepo;

    @Test
    public void getProfile_ShouldReturnProfileJson_WithoutBlob() throws Exception {
        User user = User.builder().id(1L).name("Tutor Name").email("tutor@test.com").build();
        TutorProfile profile = TutorProfile.builder()
                .userId(1L)
                .user(user)
                .bio("Test Bio")
                .hourlyRateCents(2000)
                .photoUrl("/api/tutor-profiles/1/photo")
                .build();

        // Simulate a large blob
        byte[] largeBlob = new byte[1024 * 1024]; // 1MB
        profile.setPhotoBlob(largeBlob);
        profile.setPhotoContentType("image/jpeg");

        given(repo.findById(1L)).willReturn(Optional.of(profile));

        mvc.perform(get("/api/tutor-profiles/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bio").value("Test Bio"))
                .andExpect(jsonPath("$.photoBlob").doesNotExist()) // Verify blob is ignored
                .andExpect(jsonPath("$.photoUrl").value("/api/tutor-profiles/1/photo"));
    }
}
