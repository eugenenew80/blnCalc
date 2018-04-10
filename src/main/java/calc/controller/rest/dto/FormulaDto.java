package calc.controller.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FormulaDto {
    private Long id;
    private String code;
    private String description;
    private String textBase64;

    @JsonIgnore
    private String text;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDateTime endDate;
}
