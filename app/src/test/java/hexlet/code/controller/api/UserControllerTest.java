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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private User anotherUser;
    private String testUserToken;
    private String anotherUserToken;


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

        anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password456"));
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser = userRepository.save(anotherUser);
        anotherUserToken = jwtUtils.generateToken(anotherUser.getEmail());
    }

    @AfterEach
    public void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    public void testCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "new@example.com",
                                    "password": "password789",
                                    "firstName": "New",
                                    "lastName": "User"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void testAccessProtectedResourceWithToken() throws Exception {
        User user = new User();
        user.setEmail("protected@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user = userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());

        mockMvc.perform(get("/api/users/" + user.getId())
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    public void testPartialUpdateUser() throws Exception {
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .header("Authorization", "Bearer " + testUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Partial"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Partial"))
                .andExpect(jsonPath("$.lastName").value("User")) // Осталось прежним
                .andExpect(jsonPath("$.email").value("test@example.com"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("Partial", updatedUser.getFirstName());
    }

    @Test
    public void testUpdateUserUnauthorized() throws Exception {
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .header("Authorization", "Bearer " + anotherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "firstName": "Hacked"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

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
