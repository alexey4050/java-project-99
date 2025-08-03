package hexlet.code.controller.api;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String testUserToken;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser = userRepository.save(testUser);
        testUserToken = jwtUtils.generateToken(testUser.getEmail());

    }

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }

//    @Test
//    public void testCreateUser() throws Exception {
//        MvcResult result = mockMvc.perform(post("/api/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                    "email": "new@example.com",
//                                    "password": "password789",
//                                    "firstName": "New",
//                                    "lastName": "User"
//                                }
//                                """))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").exists())
//                .andExpect(jsonPath("$.email").value("new@example.com"))
//                .andExpect(jsonPath("$.firstName").value("New"))
//                .andExpect(jsonPath("$.lastName").value("User"))
//                .andExpect(jsonPath("$.password").doesNotExist())
//                .andReturn();
//
//        String email = "new@example.com";
//        Optional<User> createdUser = userRepository.findByEmail(email);
//        assertTrue(createdUser.isPresent());
//        assertTrue(passwordEncoder.matches("password789", createdUser.get().getPassword()));
//    }

//    @Test
//    public void testGetUserById() throws Exception {
//        mockMvc.perform(get("/api/users/{id}", testUser.getId())
//                        .header("Authorization", "Bearer " + testUserToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(testUser.getId()))
//                .andExpect(jsonPath("$.email").value("test@example.com"))
//                .andExpect(jsonPath("$.firstName").value("Test"))
//                .andExpect(jsonPath("$.lastName").value("User"))
//                .andExpect(jsonPath("$.password").doesNotExist());
//    }

    @Test
    public void testGetUserByIdUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

//    @Test
//    public void testGetAllUsers() throws Exception {
//        User anotherUser = new User();
//        anotherUser.setEmail("another@example.com");
//        anotherUser.setPassword(passwordEncoder.encode("password456"));
//        anotherUser.setFirstName("Another");
//        anotherUser.setLastName("User");
//        userRepository.save(anotherUser);
//
//        mockMvc.perform(get("/api/users")
//                        .header("Authorization", "Bearer " + testUserToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].email").value("test@example.com"))
//                .andExpect(jsonPath("$[1].email").value("another@example.com"))
//                .andExpect(jsonPath("$[0].password").doesNotExist());
//    }

//    @Test
//    public void testPartialUpdateUser() throws Exception {
//        mockMvc.perform(put("/api/users/{id}", testUser.getId())
//                        .header("Authorization", "Bearer " + testUserToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                    "firstName": "Partial"
//                                }
//                                """))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.firstName").value("Partial"))
//                .andExpect(jsonPath("$.lastName").value("User"))
//                .andExpect(jsonPath("$.email").value("test@example.com"));
//        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
//        assertEquals("Partial", updatedUser.getFirstName());
//        assertEquals("User", updatedUser.getLastName());
//    }
//
//    @Test
//    public void testUpdateUser() throws Exception {
//        mockMvc.perform(put("/api/users/{id}", testUser.getId())
//                        .header("Authorization", "Bearer " + testUserToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("""
//                                {
//                                    "email": "updated@example.com",
//                                    "firstName": "Updated",
//                                    "lastName": "Name",
//                                    "password": "newpassword123"
//                                }
//                                """))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.firstName").value("Updated"))
//                .andExpect(jsonPath("$.lastName").value("Name"))
//                .andExpect(jsonPath("$.email").value("updated@example.com"));
//
//
//        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
//        assertTrue(passwordEncoder.matches("newpassword123", updatedUser.getPassword()));
//    }

    @Test
    public void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", testUser.getId())
                        .header("Authorization", "Bearer " + testUserToken))
                .andExpect(status().isNotFound());
    }
}
