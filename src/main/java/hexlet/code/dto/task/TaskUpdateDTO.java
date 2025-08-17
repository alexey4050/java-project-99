package hexlet.code.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;


@Setter
@Getter
public class TaskUpdateDTO {
    private JsonNullable<String> title;

    private JsonNullable<Integer> index;

    @JsonProperty("content")
    private JsonNullable<String> description;

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId;

    private JsonNullable<String> status;

    @JsonProperty("taskLabelIds")
    private JsonNullable<List<Long>> labelsToIds;
}
