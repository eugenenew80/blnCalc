package calc.controller.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ContextDto {
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate endDate;

    private Long orgId;
    private String contentBase64;
}
