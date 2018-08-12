package calc.entity.calc;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_tasks")
public class Task {
    @Id
    private Long id;

    @Column(name = "doc_code")
    private String docCode;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column
    private Long priority;

    @Column
    private String status;

    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY)
    private List<TaskParam> params;
}
