package hexlet.code.dto.label;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LabelCreateDTO {
    @NotBlank
    private String name;
}
