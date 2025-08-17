package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import hexlet.code.util.ModelGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    private LabelRepository labelRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User testUser;
    private TaskStatus testStatus;
    private Label testLabel;
    private Task testTask;

    @BeforeEach
    public void setup() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        statusRepository.deleteAll();
        userRepository.deleteAll();

        testUser = Instancio.of(modelGenerator.getUserModel()).create();
        testUser = userRepository.save(testUser);

        testStatus = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        testStatus = statusRepository.save(testStatus);

        testLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        testLabel = labelRepository.save(testLabel);

        testTask = Instancio.of(modelGenerator.getTaskModel()).create();
        testTask.setAssignee(testUser);
        testTask.setTaskStatus(testStatus);
        testTask.setLabels(Set.of(testLabel));
        testTask = taskRepository.save(testTask);

        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @Test
    public void testCreateTask() throws Exception {
        Label newLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        newLabel = labelRepository.save(newLabel);

        var newTaskData = new HashMap<>();
        newTaskData.put("title", "New Task");
        newTaskData.put("content", "New description");
        newTaskData.put("assignee_id", testUser.getId());
        newTaskData.put("status", testStatus.getSlug());
        newTaskData.put("taskLabelIds", List.of(newLabel.getId()));

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTaskData));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        Task createdTask = taskRepository.findByName("New Task").orElse(null);
        assertNotNull(createdTask);
        assertThat(createdTask.getDescription()).isEqualTo("New description");
        assertThat(createdTask.getLabels()).isNotNull();
        assertThat(createdTask.getLabels().iterator().next().getId()).isEqualTo(newLabel.getId());
    }

    @Test
    public void testCreateTaskUnauthorized() throws Exception {
        Map<String, Object> newTaskData = new HashMap<>();
        newTaskData.put("title", "New Task");
        newTaskData.put("status", testStatus.getSlug());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(newTaskData)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetTaskById() throws Exception {
        mockMvc.perform(get("/api/tasks/" + testTask.getId())
                        .with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTask.getId()))
                .andExpect(jsonPath("$.title").value(testTask.getName()))
                .andExpect(jsonPath("$.status").value(testStatus.getSlug()))
                .andExpect(jsonPath("$.labels[0]").value(testLabel.getId()));
    }

    @Test
    @Transactional
    public void testUpdateTask() throws Exception {
        Label newLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        newLabel = labelRepository.save(newLabel);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("title", "Updated Task");
        updateData.put("content", "Updated description");
        updateData.put("taskLabelIds", List.of(newLabel.getId()));

        mockMvc.perform(put("/api/tasks/" + testTask.getId())
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.content").value("Updated description"))
                .andExpect(jsonPath("$.labels[0]").value(newLabel.getId()));

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertThat(updatedTask.getName()).isEqualTo("Updated Task");
        assertThat(updatedTask.getDescription()).isEqualTo("Updated description");
        assertThat(updatedTask.getLabels().iterator().next().getId()).isEqualTo(newLabel.getId());
    }

    @Test
    public void testDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/" + testTask.getId())
                        .with(token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(testTask.getId())).isFalse();
    }

    @Test
    public void testCreateTaskValidation() throws Exception {
        Map<String, Object> invalidTaskData = new HashMap<>();
        invalidTaskData.put("description", "Invalid task without title and status");

        mockMvc.perform(post("/api/tasks")
                        .with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalidTaskData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIndexTaskTitleCont() throws Exception {
        taskRepository.deleteAll();

        Task task1 = Instancio.of(modelGenerator.getTaskModel()).create();
        task1.setName("Important task");
        task1.setAssignee(testUser);
        task1.setTaskStatus(testStatus);
        taskRepository.save(task1);

        Task task2 = Instancio.of(modelGenerator.getTaskModel()).create();
        task2.setName("Regular task");
        task2.setAssignee(testUser);
        task2.setTaskStatus(testStatus);
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks?titleCont=Important").with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Important task"))
                .andExpect(jsonPath("$").value(hasSize(1)));
    }

    @Test
    public void testIndexTaskAssigneeId() throws Exception {
        taskRepository.deleteAll();

        User user2 = userRepository.save(Instancio.of(modelGenerator.getUserModel()).create());

        Task task1 = Instancio.of(modelGenerator.getTaskModel()).create();
        task1.setAssignee(testUser);
        task1.setTaskStatus(testStatus);
        taskRepository.save(task1);

        Task task2 = Instancio.of(modelGenerator.getTaskModel()).create();
        task2.setAssignee(user2);
        task2.setTaskStatus(testStatus);
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks?assigneeId=" + testUser.getId()).with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()))
                .andExpect(jsonPath("$").value(hasSize(1)));
    }

    @Test
    public void testIndexTaskByStatus() throws Exception {
        taskRepository.deleteAll();

        TaskStatus status2 = statusRepository.save(Instancio.of(modelGenerator.getTaskStatusModel()).create());

        Task task1 = Instancio.of(modelGenerator.getTaskModel()).create();
        task1.setAssignee(testUser);
        task1.setTaskStatus(testStatus);
        taskRepository.save(task1);

        Task task2 = Instancio.of(modelGenerator.getTaskModel()).create();
        task2.setAssignee(testUser);
        task2.setTaskStatus(status2);
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks?status=" + testStatus.getSlug()).with(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value(testStatus.getSlug()))
                .andExpect(jsonPath("$").value(hasSize(1)));
    }
}
