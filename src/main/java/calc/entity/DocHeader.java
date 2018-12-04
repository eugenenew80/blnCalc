package calc.entity;

import calc.entity.calc.Organization;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import java.time.LocalDate;

public interface DocHeader {
    Long getId();
    PeriodTypeEnum getPeriodType();
    LocalDate getStartDate();
    LocalDate getEndDate();
    DataTypeEnum getDataType();
    Organization getOrganization();
}
