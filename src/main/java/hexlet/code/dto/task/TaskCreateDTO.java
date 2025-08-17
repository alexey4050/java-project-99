package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Setter
@Getter
public class TaskCreateDTO {
    @NotNull
    private String title;

    private Integer index;

    @JsonProperty("content")
    private JsonNullable<String> description;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    @NotNull
    private String status;

    @JsonProperty("taskLabelIds")
    private List<Long> labels;
}
