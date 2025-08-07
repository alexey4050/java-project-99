package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class TaskDTO {
    private Long id;
    private String title;
    private Integer index;
    private String description;
    private Long assigneeId;
    private LocalDate createdAt;
    private String status;
}
