package hexlet.code.dto.label;

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
