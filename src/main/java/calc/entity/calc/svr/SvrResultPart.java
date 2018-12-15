package calc.entity.calc.svr;

import calc.entity.DataTypeSupport;
import calc.entity.calc.enums.DataTypeEnum;
import lombok.*;
import javax.persistence.*;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_svr_parts")
public class SvrResultPart implements DataTypeSupport {
    @Id
    private Long id;

    @Column(name = "line_num")
    private Long lineNum;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private SvrResultHeader header;

    @Override
    @Transient
    public DataTypeEnum getDataType() {
        return header != null ? header.getDataType() : null;
    }
}
