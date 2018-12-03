package calc.formula;

import calc.entity.DocHeader;
import calc.entity.calc.enums.LangEnum;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
@Builder
public class CalcContext {
    @NonNull
    private DocHeader header;
    private Exception exception;

    @Builder.Default
    private LangEnum lang = LangEnum.RU;

    @Builder.Default
    private boolean useDataTypePriority = false;

    @Builder.Default
    private boolean traceEnabled = false;

    @Builder.Default
    private ContextType defContextType = ContextType.DEFAULT;

    @Builder.Default
    private Map<String, Double> transformerValues = new HashMap<>();

    @Builder.Default
    private Map<String, List<CalcTrace>> traces = new HashMap<>();
}
