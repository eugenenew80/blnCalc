package calc.entity.calc.inter;

import calc.entity.calc.enums.MessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_act_inter_pl_result_messages")
public class InterResultMessage {
    @Id
    @SequenceGenerator(name="calc_act_inter_pl_result_messages_s", sequenceName = "calc_act_inter_pl_result_messages_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_act_inter_pl_result_messages_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_header_id")
    private InterResultHeader header;

    @Column(name = "line_num")
    private Long lineNum;

    @Column(name="message_type_code")
    @Enumerated(EnumType.STRING)
    private MessageTypeEnum messageType;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "create_by")
    private Long createBy;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InterResultMessageTranslate> translates;
}
