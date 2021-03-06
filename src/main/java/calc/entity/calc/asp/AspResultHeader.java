package calc.entity.calc.asp;

import calc.converter.jpa.BooleanToIntConverter;
import calc.entity.DocHeader;
import calc.entity.calc.Organization;
import calc.entity.calc.enums.BatchStatusEnum;
import calc.entity.calc.enums.DataTypeEnum;
import calc.entity.calc.enums.PeriodTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_result_headers")
public class AspResultHeader implements DocHeader {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_header_id")
    private AspHeader header;

    @ManyToOne
    @JoinColumn(name = "org_id")
    private Organization organization;

    @Column(name="period_type")
    @Enumerated(EnumType.STRING)
    private PeriodTypeEnum periodType;

    @Column(name="data_type_code")
    @Enumerated(EnumType.STRING)
    private DataTypeEnum dataType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private BatchStatusEnum status;

    @Column(name = "state_code")
    private String state;

    @Column(name = "is_active")
    @Convert(converter = BooleanToIntConverter.class)
    private Boolean isActive;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "last_update_by")
    private Long lastUpdateBy;
}
