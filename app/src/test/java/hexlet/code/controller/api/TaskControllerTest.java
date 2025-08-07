package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private ObjectMapper om;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private Task testTask;
    private User testUser;
    private TaskStatus testStatus;

    private Task genTask() {
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .supply(Select.field(Task::getName), () -> "Test Task")
                .supply(Select.field(Task::getDescription), () -> "Test description")
                .create();
    }

    @BeforeEach
    public void setup() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        statusRepository.deleteAll();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        testStatus = new TaskStatus();
        testStatus.setName("Test Status");
        testStatus.setSlug("test-status");
        testStatus = statusRepository.save(testStatus);

        testTask = genTask();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testStatus);
        testTask = taskRepository.save(testTask);

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    public void testCreateTask() throws Exception {
        var newTaskData = new HashMap<>();
        newTaskData.put("title", "New Task");
        newTaskData.put("description", "New description");
        newTaskData.put("assignee_id", testUser.getId());
        newTaskData.put("status", testStatus.getSlug());

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskData));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        Optional<Task> createdTask = taskRepository.findByName("New Task");
        assertThat(createdTask).isPresent();
        assertThat(createdTask.get().getDescription()).isEqualTo("New description");
    }

    @Test
    public void testCreateTaskUnauthorized() throws Exception {
        var newTaskData = new HashMap<>();
        newTaskData.put("title", "New Task");
        newTaskData.put("status", testStatus.getSlug());

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskData));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTaskById() throws Exception {
        var request = get("/api/tasks/" + testTask.getId())
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        var body = result.getContentAsString();
        assertThat(body).contains("\"id\":" + testTask.getId());
        assertThat(body).contains("\"title\":\"Test Task\"");
        assertThat(body).contains("\"status\":\"test-status\"");
    }

    @Test
    public void testGetAllTasks() throws Exception {
        Task anotherTask = genTask();
        anotherTask.setName("Another Task");
        anotherTask.setAssignee(testUser);
        anotherTask.setTaskStatus(testStatus);
        taskRepository.save(anotherTask);

        var request = get("/api/tasks")
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).contains("Test Task");
                    assertThat(content).contains("Another Task");
                });
    }

    @Test
    public void testUpdateTask() throws Exception {
        var updateData = new HashMap<>();
        updateData.put("title", "Updated Task");
        updateData.put("description", "Updated description");

        var request = put("/api/tasks/" + testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo("Updated Task");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated description");
    }

    @Test
    public void testDeleteTask() throws Exception {
        var request = delete("/api/tasks/" + testTask.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(testTask.getId())).isEmpty();
    }

    @Test
    public void testCreateTaskValidation() throws Exception {
        var invalidTaskData = new HashMap<>();
        invalidTaskData.put("description", "Invalid task without title and status");

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(invalidTaskData));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }
}
