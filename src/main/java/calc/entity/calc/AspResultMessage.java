package calc.entity.calc;

import calc.entity.calc.enums.MessageTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

@Data
@EqualsAndHashCode(of= {"id"})
@Entity
@Table(name = "calc_asp1_result_messages")
public class AspResultMessage {
    @Id
    @SequenceGenerator(name="calc_asp1_result_messages_s", sequenceName = "calc_asp1_result_messages_s", allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_asp1_result_messages_s")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asp1_result_header_id")
    private AspResultHeader header;

    @Column(name = "line_num")
    private Long lineNum;

    @Column(name="message_type_code")
    @Enumerated(EnumType.STRING)
    private MessageTypeEnum messageType;

    @Column(name = "error_code")
    private String errorCode;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AspResultMessageTranslate> translates;
}
