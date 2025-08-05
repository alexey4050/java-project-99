package hexlet.code.controller.api;

import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String testUserToken;
    private TaskStatus existingStatus;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        statusRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser = userRepository.save(testUser);
        testUserToken = jwtUtils.generateToken(testUser.getEmail());

        existingStatus = new TaskStatus();
        existingStatus.setName("Existing Status");
        existingStatus.setSlug("existing");
        existingStatus = statusRepository.save(existingStatus);
    }

    @Test
    public void testCreateStatus() throws Exception {
        mockMvc.perform(post("/api/task_statuses")
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "New Status",
                                    "slug": "new"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Status"))
                .andExpect(jsonPath("$.slug").value("new"))
                .andExpect(jsonPath("$.createdAt").exists());

        Optional<TaskStatus> createdStatus = statusRepository.findBySlug("new");
        assertTrue(createdStatus.isPresent());
        assertEquals("New Status", createdStatus.get().getName());
    }

    @Test
    public void testCreateStatusUnauthorized() throws Exception {
        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "New Status",
                                    "slug": "new"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetStatusById() throws Exception {
        mockMvc.perform(get("/api/task_statuses/{id}", existingStatus.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingStatus.getId()))
                .andExpect(jsonPath("$.name").value("Existing Status"))
                .andExpect(jsonPath("$.slug").value("existing"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    public void testUpdateStatus() throws Exception {
        mockMvc.perform(put("/api/task_statuses/{id}", existingStatus.getId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Status"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Status"))
                .andExpect(jsonPath("$.slug").value("existing"));

        TaskStatus updatedStatus = statusRepository.findById(existingStatus.getId()).orElseThrow();
        assertEquals("Updated Status", updatedStatus.getName());
    }

    @Test
    public void testDeleteStatus() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/{id}", existingStatus.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        assertFalse(statusRepository.existsById(existingStatus.getId()));
    }
}
