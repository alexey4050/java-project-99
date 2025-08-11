package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class TaskDTO {
    private Long id;

    private String title;

    private Integer index;

    @JsonProperty("content")
    private JsonNullable<String> description;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    private LocalDate createdAt;
    private String status;

    private List<Long> labels;
}
