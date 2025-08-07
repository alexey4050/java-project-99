package hexlet.code.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskCreateDTO {
    @NotNull
    private String title;

    private Integer index;
    private String description;
    private Long assigneeId;

    @NotNull
    private String status;
}
