package calc;

import calc.formula.expression.Expression;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import calc.formula.expression.impl.DoubleValueOperand;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
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
    public Map<String, BinaryOperator<Expression>> binaryOperators() {
        BinaryOperator<Expression> add = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() + op2.getValue())
            .build();

        BinaryOperator<Expression> subtract = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() - op2.getValue())
            .build();

        BinaryOperator<Expression> divide = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() / op2.getValue())
            .build();

        BinaryOperator<Expression> multiply = (op1, op2) -> DoubleValueOperand.builder()
            .value(op1.getValue() * op2.getValue())
            .build();

        BinaryOperator<Expression> pow = (op1, op2) -> DoubleValueOperand.builder()
            .value(Math.pow(op1.getValue(), op2.getValue()))
            .build();

        BinaryOperator<Expression> max = (op1, op2) -> DoubleValueOperand.builder()
            .value(Math.max(op1.getValue(), op2.getValue()))
            .build();

        BinaryOperator<Expression> min = (op1, op2) -> DoubleValueOperand.builder()
            .value(Math.min(op1.getValue(), op2.getValue()))
            .build();

        Map<String, BinaryOperator<Expression>> operations = new HashMap<>();
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
    public Map<String, UnaryOperator<Expression>> unaryOperations() {
        UnaryOperator<Expression> nothing = (op) -> DoubleValueOperand.builder()
            .value(op.getValue())
            .build();

        UnaryOperator<Expression> minus = (op) -> DoubleValueOperand.builder()
            .value(-1*op.getValue())
            .build();

        UnaryOperator<Expression> abs = (op) -> DoubleValueOperand.builder()
            .value(Math.abs(op.getValue()))
            .build();

        UnaryOperator<Expression> ceil = (op) -> DoubleValueOperand.builder()
            .value(Math.ceil(op.getValue()))
            .build();

        UnaryOperator<Expression> floor = (op) -> DoubleValueOperand.builder()
            .value(Math.floor(op.getValue()))
            .build();

        UnaryOperator<Expression> sqrt = (op) -> DoubleValueOperand.builder()
            .value(Math.sqrt(op.getValue()))
            .build();

        UnaryOperator<Expression> pow2 = (op) -> DoubleValueOperand.builder()
            .value(Math.pow(op.getValue(),2))
            .build();

        UnaryOperator<Expression> sign = (op) -> DoubleValueOperand.builder()
            .value(Math.signum(op.getValue()))
            .build();

        UnaryOperator<Expression> round = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*1d) / 1d)
            .build();

        UnaryOperator<Expression> round1 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*10d) / 10d)
            .build();

        UnaryOperator<Expression> round2 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*100d) / 100d)
            .build();

        UnaryOperator<Expression> round3 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*1000d) / 1000d)
            .build();

        UnaryOperator<Expression> round4 = (op) -> DoubleValueOperand.builder()
            .value(Math.round(op.getValue()*10000d) / 10000d)
            .build();

        Map<String, UnaryOperator<Expression>> operations = new HashMap<>();
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
