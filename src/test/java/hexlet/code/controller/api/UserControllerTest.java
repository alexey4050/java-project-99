package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import hexlet.code.utils.JwtUtils;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User testUser;

    @BeforeEach
    public void setup() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        statusRepository.deleteAll();
        userRepository.deleteAll();

        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        testUser = userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        System.out.println("Test user ID: " + testUser.getId());
    }

    @Test
    public void testCreateUser() throws Exception {
        User newUser = Instancio.of(modelGenerator.getUserModel()).create();

        Map<String, Object> data = new HashMap<>();
        data.put("email", newUser.getEmail());
        data.put("id", newUser.getId());
        data.put("firstName", newUser.getFirstName());
        data.put("lastName", newUser.getLastName());
        data.put("password", "rawPassword123");
        data.put("createdAt", newUser.getCreatedAt());

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        UserDTO createdDTO = om.readValue(response.getContentAsString(), UserDTO.class);
        assertThat(createdDTO.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(createdDTO.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(createdDTO.getLastName()).isEqualTo(newUser.getLastName());

        Optional<User> createdUser = userRepository.findByEmail(newUser.getEmail());
        assertTrue(createdUser.isPresent());
        assertTrue(passwordEncoder.matches("rawPassword123", createdUser.get().getPassword()));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        User anotherUser = Instancio.of(modelGenerator.getUserModel()).create();
        anotherUser.setPassword(passwordEncoder.encode("password456"));
        userRepository.save(anotherUser);

        List<User> expectedUsers = userRepository.findAll();
        List<UserDTO> expectedDTOs = expectedUsers.stream()
                .map(userMapper::map)
                .toList();

        var request = get("/api/users").with(token);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(expectedDTOs)));
    }


    @Test
    public void testGetUserById() throws Exception {
        var expectedDTO = userMapper.map(testUser);

        var request = get("/api/users/" + testUser.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(expectedDTO)));
    }

    @Test
    public void testGetUserByIdUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testPartialUpdateUser() throws Exception {
        var data = new HashMap<>();
        data.put("firstName", "UpdatedName");

        var response = mockMvc.perform(put("/api/users/" + testUser.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        UserDTO updatedDTO = om.readValue(response.getContentAsString(), UserDTO.class);
        assertThat(updatedDTO.getFirstName()).isEqualTo("UpdatedName");
        assertThat(updatedDTO.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(updatedDTO.getEmail()).isEqualTo(testUser.getEmail());

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedName");
        assertThat(updatedUser.getLastName()).isEqualTo(testUser.getLastName());
    }

    @Test
    public void testUpdateUser() throws Exception {
        var data = new HashMap<>();
        data.put("email", "updated@example.com");
        data.put("firstName", "Updated");
        data.put("lastName", "User");
        data.put("password", "newpassword123");

        var response = mockMvc.perform(put("/api/users/" + testUser.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        UserDTO updatedDTO = om.readValue(response.getContentAsString(), UserDTO.class);
        assertThat(updatedDTO.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedDTO.getFirstName()).isEqualTo("Updated");
        assertThat(updatedDTO.getLastName()).isEqualTo("User");

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertTrue(passwordEncoder.matches("newpassword123", updatedUser.getPassword()));
    }

    @Test
    public void testDeleteUser() throws Exception {
        var request = delete("/api/users/" + testUser.getId())
                .with(token);

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }
}
