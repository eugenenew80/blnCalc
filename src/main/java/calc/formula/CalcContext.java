package calc.formula;

import calc.entity.DocHeader;
import calc.entity.calc.enums.LangEnum;
import calc.formula.exception.CalcServiceException;
import lombok.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
@Builder
public class CalcContext {
    @NonNull
    private DocHeader header;
    private CalcServiceException exception;

    @Builder.Default
    private LangEnum lang = LangEnum.RU;

    @Builder.Default
    private boolean useDataTypePriority = false;

    @Builder.Default
    private boolean nullPermissible = true;

    @Builder.Default
    private boolean traceEnabled = false;

    @Builder.Default
    private ContextTypeEnum defContextType = ContextTypeEnum.DEFAULT;

    @Builder.Default
    private FormulaBehaviourEnum formulaBehaviour = FormulaBehaviourEnum.ANY;

    @Builder.Default
    private Map<String, Double> transformerValues = new HashMap<>();

    @Builder.Default
    private Map<String, List<CalcTrace>> traces = new HashMap<>();
}
