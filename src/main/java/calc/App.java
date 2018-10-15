package calc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableScheduling;

@EntityScan(basePackageClasses = {App.class, Jsr310JpaConverters.class})
@SpringBootApplication
@EnableScheduling
public class App  {
    public static void main(String[] args) {
        /*
        String template = "Hi ${name}! Your number is ${number}";

        Map<String, String> data = new HashMap<String, String>();
        data.put("name", "John");
        data.put("number", "1");

        String formattedString = StrSubstitutor.replace(template, data);
        System.out.println(formattedString);
        */

        SpringApplication.run(App.class, args);
    }
}
