package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class TaskUpdateDTO {
    private JsonNullable<String> title;
    private JsonNullable<Integer> index;
    private JsonNullable<String> description;
    private JsonNullable<Long> assigneeId;
    private JsonNullable<String> status;
}
