package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskStatusDTO;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
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
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

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
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        statusRepository.deleteAll();
        userRepository.deleteAll();

        testStatus = genStatus("Test Status", "test-status");
        testStatus = statusRepository.save(testStatus);
        token = jwt().jwt(builder -> builder.subject("test@example.com"));
    }

    @Test
    public void testGetAllStatuses() throws Exception {
        TaskStatus anotherStatus = genStatus("Another Status", "another-status");
        statusRepository.save(anotherStatus);

        List<TaskStatus> expectedStatuses = statusRepository.findAll();
        List<TaskStatusDTO> expectedDTOs = expectedStatuses.stream()
                .map(taskStatusMapper::map)
                .toList();

        var request = get("/api/task_statuses");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(expectedDTOs.size()))
                .andExpect(content().json(om.writeValueAsString(expectedDTOs)));
    }


    @Test
    public void testCreateStatus() throws Exception {
        TaskStatus newStatus = genStatus("New Status", "new-status");

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newStatus));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        TaskStatusDTO createdDTO = om.readValue(response.getContentAsString(), TaskStatusDTO.class);
        assertThat(createdDTO.getName()).isEqualTo("New Status");
        assertThat(createdDTO.getSlug()).isEqualTo("new-status");

        Optional<TaskStatus> createdStatus = statusRepository.findBySlug("new-status");
        assertTrue(createdStatus.isPresent());
        assertThat(createdStatus.get().getName()).isEqualTo("New Status");
        assertThat(createdStatus.get().getSlug()).isEqualTo("new-status");

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
        var expectedDTO = taskStatusMapper.map(testStatus);

        var request = get("/api/task_statuses/" + testStatus.getId());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(expectedDTO)));
    }

    @Test
    public void testUpdateStatus() throws Exception {
        var data = Map.of("name", "Updated Status", "slug", "updated-status");

        var response = mockMvc.perform(put("/api/task_statuses/" + testStatus.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(data)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        TaskStatusDTO updatedDTO = om.readValue(response.getContentAsString(), TaskStatusDTO.class);
        assertThat(updatedDTO.getName()).isEqualTo("Updated Status");
        assertThat(updatedDTO.getSlug()).isEqualTo("updated-status");

        TaskStatus updatedStatus = statusRepository.findById(testStatus.getId()).orElseThrow();
        assertThat(updatedStatus.getName()).isEqualTo("Updated Status");
        assertThat(updatedStatus.getSlug()).isEqualTo("updated-status");
    }

    @Test
    public void testDeleteStatus() throws Exception {
        var request = delete("/api/task_statuses/" + testStatus.getId())
                .with(token);

        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(statusRepository.findById(testStatus.getId())).isEmpty();
    }
}
