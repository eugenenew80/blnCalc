package calc.entity.calc.bs;

import calc.entity.calc.enums.MessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_bs_result_messages")
public class BalanceSubstResultMessage {
    @Id
    @SequenceGenerator(name="calc_bs_result_messages_s", sequenceName = "calc_bs_result_messages_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_bs_result_messages_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "header_id")
    private BalanceSubstResultHeader header;

    @Column(name = "section_type")
    private String section;

    @Column(name = "line_num")
    private Long lineNum;

    @Column(name="message_type_code")
    @Enumerated(EnumType.STRING)
    private MessageTypeEnum messageType;

    @Column(name = "error_code")
    private String errorCode;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BalanceSubstResultMessageTranslate> translates;
}
