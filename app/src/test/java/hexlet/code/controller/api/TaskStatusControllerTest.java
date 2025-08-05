package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private ObjectMapper om;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private TaskStatus testStatus;

    private TaskStatus genStatus(String name, String slug) {
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getName), () -> name)
                .supply(Select.field(TaskStatus::getSlug), () -> slug)
                .create();
    }

    @BeforeEach
    public void setup() {
        statusRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();

        testStatus = genStatus("Test Status", "test-status");
        testStatus = statusRepository.save(testStatus);
        token = jwt().jwt(builder -> builder.subject("test@example.com"));
    }

    @Test
    public void testCreateStatus() throws Exception {
        TaskStatus newStatus = genStatus("New Status", "new-status");
        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newStatus));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        Optional<TaskStatus> createdStatus = statusRepository.findBySlug("new-status");
        assertTrue(createdStatus.isPresent());
        assertThat(createdStatus.get().getName()).isEqualTo("New Status");
    }

    @Test
    public void testCreateStatusUnauthorized() throws Exception {
        TaskStatus newStatus = genStatus("New Status", "new-status");
        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newStatus));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testGetStatusById() throws Exception {
        var request = get("/api/task_statuses/" + testStatus.getId());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var body = result.getContentAsString();
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(testStatus.getId()),
                v -> v.node("name").isEqualTo("Test Status"),
                v -> v.node("slug").isEqualTo("test-status"),
                v -> v.node("createdAt").isPresent()
        );
    }

    @Test
    public void testUpdateStatus() throws Exception {
        var data = new HashMap<>();
        data.put("name", "Updated Status");

        var request = put("/api/task_statuses/" + testStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request).andExpect(status().isOk());

        TaskStatus updatedStatus = statusRepository.findById(testStatus.getId()).orElseThrow();
        assertThat(updatedStatus.getName()).isEqualTo("Updated Status");
    }

    @Test
    public void testDeleteStatus() throws Exception {
        var request = delete("/api/task_statuses/" + testStatus.getId())
                .with(token);

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(statusRepository.findById(testStatus.getId())).isEmpty();
    }
}
