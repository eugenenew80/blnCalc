package calc;

import calc.entity.calc.Parameter;
import calc.formula.service.MessageError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.dozer.DozerBeanMapper;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.Duration;
import java.util.Arrays;

import static java.time.Duration.*;
import static org.ehcache.config.builders.CacheConfigurationBuilder.*;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.*;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;
import static org.ehcache.config.builders.ResourcePoolsBuilder.heap;

@Configuration
public class AppConfig {

    @Bean
    public DozerBeanMapper dozerBeanMapper() {
        DozerBeanMapper mapper = new DozerBeanMapper();
        mapper.setMappingFiles(Arrays.asList(
            "dozer/MappingConfig.xml"
        ));

        return mapper;
    }

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
    public CacheManager ehcacheManager() {
        return CacheManagerBuilder.newCacheManagerBuilder()
            .withCache("paramCache",
                    newCacheConfigurationBuilder(String.class, Parameter.class, heap(20))
                    .withExpiry(timeToLiveExpiration(ofMinutes(60))).build())
            .withCache("messageCache",
                    newCacheConfigurationBuilder(String.class, MessageError.class, heap(300))
                    .withExpiry(timeToLiveExpiration(ofMinutes(10))).build())
            .build(true);
    }
}