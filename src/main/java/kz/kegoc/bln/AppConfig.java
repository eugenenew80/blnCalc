package kz.kegoc.bln;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import kz.kegoc.bln.calc.operand.DoubleValueOperand;
import kz.kegoc.bln.calc.operand.Operand;
import kz.kegoc.bln.entity.MeteringPoint;
import kz.kegoc.bln.entity.Parameter;
import org.ehcache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.*;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    @Bean
    public ScriptEngine scriptEngine() {
        return new ScriptEngineManager().getEngineByName("nashorn");
    }

    @Bean
    public CacheManager cacheManager() {
        return newCacheManagerBuilder()
            .withCache("meteringPoints", newCacheConfigurationBuilder(String.class, MeteringPoint.class, heap(100)).build())
            .withCache("parameters", newCacheConfigurationBuilder(String.class, Parameter.class, heap(100)).build())
            .build(true);
    }

    @Bean
    public Map<String, BinaryOperator<Operand>> binaryOperators() {
        BinaryOperator<Operand> add = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() + op2.getValue())
            .build();

        BinaryOperator<Operand> subtract = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() - op2.getValue())
            .build();

        BinaryOperator<Operand> divide = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() / op2.getValue())
            .build();

        BinaryOperator<Operand> multiply = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() * op2.getValue())
            .build();

        BinaryOperator<Operand> pow = (op1, op2) -> DoubleValueOperand.builder()
            .value(Math.pow(op1.getValue(), op2.getValue()))
            .build();

        BinaryOperator<Operand> max = (op1, op2) -> DoubleValueOperand.builder()
            .value(Math.max(op1.getValue(), op2.getValue()))
            .build();

        BinaryOperator<Operand> min = (op1, op2) -> DoubleValueOperand.builder()
            .value(Math.min(op1.getValue(), op2.getValue()))
            .build();

        Map<String, BinaryOperator<Operand>> operations = new HashMap<>();
        operations.put("add", add);
        operations.put("subtract", subtract);
        operations.put("multiply", multiply);
        operations.put("divide", divide);
        operations.put("max", max);
        operations.put("min", min);
        operations.put("pow", pow);

        return operations;
    }

    @Bean
    public Map<String, UnaryOperator<Operand>> unaryOperations() {
        UnaryOperator<Operand> nothing = (op) -> DoubleValueOperand.builder()
            .value(op.getValue())
            .build();

        UnaryOperator<Operand> minus = (op) -> DoubleValueOperand.builder()
            .value(-1*op.getValue())
            .build();

        UnaryOperator<Operand> abs = (op) -> DoubleValueOperand.builder()
            .value(Math.abs(op.getValue()))
            .build();

        UnaryOperator<Operand> ceil = (op) -> DoubleValueOperand.builder()
            .value(Math.ceil(op.getValue()))
            .build();

        UnaryOperator<Operand> floor = (op) -> DoubleValueOperand.builder()
            .value(Math.floor(op.getValue()))
            .build();

        UnaryOperator<Operand> sqrt = (op) -> DoubleValueOperand.builder()
            .value(Math.sqrt(op.getValue()))
            .build();

        UnaryOperator<Operand> pow2 = (op) -> DoubleValueOperand.builder()
            .value(Math.pow(op.getValue(),2))
            .build();

        UnaryOperator<Operand> sign = (op) -> DoubleValueOperand.builder()
            .value(Math.signum(op.getValue()))
            .build();

        UnaryOperator<Operand> round = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*1d) / 1d)
            .build();

        UnaryOperator<Operand> round1 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*10d) / 10d)
            .build();

        UnaryOperator<Operand> round2 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*100d) / 100d)
            .build();

        UnaryOperator<Operand> round3 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*1000d) / 1000d)
            .build();

        UnaryOperator<Operand> round4 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*10000d) / 10000d)
            .build();

        Map<String, UnaryOperator<Operand>> operations = new HashMap<>();
        operations.put("nothing", nothing);
        operations.put("minus", minus);
        operations.put("abs", abs);
        operations.put("ceil", ceil);
        operations.put("floor", floor);
        operations.put("sqrt", sqrt);
        operations.put("pow-2", pow2);
        operations.put("sign", sign);
        operations.put("round", round);
        operations.put("round-1", round1);
        operations.put("round-2", round2);
        operations.put("round-3", round3);
        operations.put("round-4", round4);
        return operations;
    }
}
