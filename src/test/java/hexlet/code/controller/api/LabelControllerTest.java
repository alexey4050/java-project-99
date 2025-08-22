package hexlet.code.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private ObjectMapper om;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private Label testLabel;

    @BeforeEach
    public void setup() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();

        testLabel = new Label();
        testLabel.setName("bug");
        testLabel = labelRepository.save(testLabel);

        var testUser = new Object() {
            public final String email = "test@example.com";
        };

        token = jwt().jwt(builder -> builder.subject(testUser.email));
    }

    @Test
    public void testCreateLabel() throws Exception {
        var labelData = new HashMap<>();
        labelData.put("name", "feature");

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(labelData));

        var response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        LabelDTO createdDTO = om.readValue(response.getContentAsString(), LabelDTO.class);
        assertThat(createdDTO.getName()).isEqualTo("feature");

        Optional<Label> createdLabel = labelRepository.findByName("feature");
        assertThat(createdLabel).isPresent();
        assertThat(createdLabel.get().getName()).isEqualTo("feature");
    }

    @Test
    public void testCreateLabelUnauthorized() throws Exception {
        var labelData = new HashMap<>();
        labelData.put("name", "feature");

        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(labelData));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetLabelById() throws Exception {
        var expectedDTO = labelMapper.map(testLabel);

        var request = get("/api/labels/" + testLabel.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(om.writeValueAsString(expectedDTO)));
    }

    @Test
    public void testGetAllLabels() throws Exception {
        List<Label> dbLabels = labelRepository.findAll();
        List<LabelDTO> expectedDTOs = dbLabels.stream()
                .map(labelMapper::map)
                .toList();
        var request = get("/api/labels")
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(expectedDTOs.size()))
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    List<LabelDTO> actualDTOs = om.readValue(content, new TypeReference<>() {
                    });

                    assertThat(actualDTOs)
                            .usingRecursiveComparison()
                            .isEqualTo(expectedDTOs);
                });
    }

    @Test
    public void testUpdateLabel() throws Exception {
        var updateData = Map.of("name", "critical-bug");

        var request = put("/api/labels/" + testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        var response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        LabelDTO updatedDTO = om.readValue(response.getContentAsString(), LabelDTO.class);
        assertThat(updatedDTO.getName()).isEqualTo("critical-bug");
        assertThat(updatedDTO.getId()).isEqualTo(testLabel.getId());

        Label updatedLabel = labelRepository.findById(testLabel.getId()).orElseThrow();
        assertThat(updatedLabel.getName()).isEqualTo("critical-bug");
    }

    @Test
    public void testDeleteLabel() throws Exception {
        var request = delete("/api/labels/" + testLabel.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(testLabel.getId())).isEmpty();
    }

    @Test
    public void testDeleteLabelAssociatedWithTask() throws Exception {
        var status = new TaskStatus();
        status.setName("Test Status");
        status.setSlug("test-super");
        taskStatusRepository.save(status);

        var task = new Task();
        task.setName("Test Task");
        task.setTaskStatus(status);
        task.getLabels().add(testLabel);
        taskRepository.save(task);

        var request = delete("/api/labels/" + testLabel.getId())
                .with(token);

        mockMvc.perform(request)
                .andExpect(status().isConflict());

        assertThat(labelRepository.existsById(testLabel.getId())).isTrue();
    }
}
