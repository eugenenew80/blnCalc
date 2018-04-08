package calc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import calc.entity.MeteringPoint;
import calc.entity.Parameter;
import org.ehcache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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
}
