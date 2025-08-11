package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class LabelDTO {
    Long id;
    String name;
    LocalDate createdAt;
}
