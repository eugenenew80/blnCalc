package calc.controller.rest;

import calc.controller.rest.dto.CalcDto;
import calc.controller.rest.dto.ResultDto;
import calc.entity.MeteringPointFormula;
import calc.formula.CalcContext;
import calc.formula.expression.Expression;
import calc.formula.service.ExpressionService;
import calc.formula.sort.Graph;
import calc.formula.sort.Vertex;
import calc.repo.FormulaRepo;
import calc.repo.MeteringPointFormulaRepo;
import calc.repo.MeteringPointRepo;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.toList;


@RestController
@RequiredArgsConstructor
public class CalcController {
    private final ExpressionService expressionService;
    private final FormulaRepo formulaRepo;
    private final MeteringPointRepo meteringPointRepo;
    private final MeteringPointFormulaRepo meteringPointFormulaRepo;

    @PostConstruct
    private void init() {
    }

    @PostMapping(value = "/rest/calc/text", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public ResultDto calc(@RequestBody CalcDto calcDto) throws Exception {
        byte[] contentAsBytes = Base64.decodeBase64(calcDto.getContentBase64());
        String formula = new String(contentAsBytes, Charset.forName("UTF-8"));

        CalcContext context = CalcContext.builder()
            .startDate(calcDto.getStartDate().atStartOfDay())
            .endDate(calcDto.getEndDate().atStartOfDay().plusDays(1))
            .build();

        Double result = expressionService
            .parse(formula, context)
            .value();

        return new ResultDto(result);
    }


    @PostMapping(value = "/rest/calc/all", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
    public void calcAll(@RequestBody CalcDto calcDto) throws Exception {
        CalcContext context = CalcContext.builder()
            .startDate(calcDto.getStartDate().atStartOfDay())
            .endDate(calcDto.getEndDate().atStartOfDay().plusDays(1))
            .build();

        Map<String, Expression> expressionMap = new TreeMap<>();
        for (MeteringPointFormula mpf : meteringPointFormulaRepo.findAll()) {
            Expression expr = expressionService
                .parse(mpf.getFormula().getText(), context);

            expressionMap.put(mpf.getMeteringPoint().getCode(), expr);
        }

        List<String> mps = sort(expressionMap);
        for (String code : mps) {
            Double value = expressionMap.get(code).value();
            System.out.println(value);
        }
    }

    private List<String> sort(Map<String, Expression> expressionMap) {
        Map<String, Integer> vertexMap = new TreeMap<>();
        int n =0;
        for (String key : expressionMap.keySet()) {
            vertexMap.put(key, n);
            n++;
        }

        Graph theGraph = new Graph(expressionMap.size());
        for (String key : expressionMap.keySet()) {
            theGraph.addVertex(new Vertex(key));
        }

        for (String key : expressionMap.keySet()) {
            Expression expression = expressionMap.get(key);
            for (String mp : expression.meteringPoints()) {
                if (vertexMap.containsKey(mp))
                    theGraph.addEdge(vertexMap.get(mp), vertexMap.get(key));
            }
        }

        return theGraph.topo().stream()
            .map(t->t.getCode())
            .collect(toList());
    }
}
