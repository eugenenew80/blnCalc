package calc.controller.rest;

import calc.controller.rest.dto.FormulaDto;
import calc.entity.calc.Formula;
import calc.repo.calc.FormulaRepo;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.dozer.DozerBeanMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import static calc.util.Util.first;

@RestController
@RequiredArgsConstructor
public class FormulaController {
    private final FormulaRepo formulaRepo;
    private final DozerBeanMapper mapper;

    @PostConstruct
    private void init() {
        findById = formulaRepo::findOne;
        findByCode = formulaRepo::findByCode;
        save = formulaRepo::save;
        transformToEntity = t -> mapper.map(t, Formula.class);
        transformToDto = t -> mapper.map(t, FormulaDto.class);

        encodeToBase64 = (t -> {
            t.setTextBase64(new String(Base64.encodeBase64(t.getText().getBytes())));
            return t;
        });

        decodeFromBase64 = (t -> {
            t.setText(new String(Base64.decodeBase64(t.getTextBase64().getBytes())));
            return t;
        });
    }

    @GetMapping(value = "/rest/formula/", produces = "application/json;charset=utf-8")
    public List<FormulaDto> getAll() {
        return formulaRepo.findAll()
            .stream()
            .map(transformToDto::apply)
            .collect(Collectors.toList());
    }

    @GetMapping(value = "/rest/formula/{id}", produces = "application/json;charset=utf-8")
    public FormulaDto getById(@PathVariable Long id) {
        return first(findById)
            .andThen(transformToDto)
            .andThen(encodeToBase64)
            .apply(id);
    }

    @GetMapping(value = "/rest/formula/byCode/{code}", produces = "application/json;charset=utf-8")
    public FormulaDto getByCode(@PathVariable String code) {
        return first(findByCode)
            .andThen(transformToDto)
            .andThen(encodeToBase64)
            .apply(code);
    }

    @PostMapping(value = "/rest/formula/", produces = "application/json;charset=utf-8")
    public FormulaDto create(@RequestBody FormulaDto dto) {
        return first(decodeFromBase64)
            .andThen(transformToEntity)
            .andThen(save)
            .andThen(transformToDto)
            .apply(dto);
    }

    @PutMapping(value = "/rest/formula/{id}", produces = "application/json;charset=utf-8")
    public FormulaDto update(@PathVariable Long id, @RequestBody FormulaDto dto) {
        return first(decodeFromBase64)
            .andThen(transformToEntity)
            .andThen(save)
            .andThen(transformToDto)
            .apply(dto);
    }

    @DeleteMapping(value = "/rest/formula/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        formulaRepo.delete(id);
    }


    private UnaryOperator<Formula> save;
    private Function<Long, Formula> findById;
    private Function<String, Formula> findByCode;
    private Function<FormulaDto, Formula> transformToEntity;
    private Function<Formula, FormulaDto> transformToDto;
    private Function<FormulaDto, FormulaDto> encodeToBase64;
    private Function<FormulaDto, FormulaDto> decodeFromBase64;
}
