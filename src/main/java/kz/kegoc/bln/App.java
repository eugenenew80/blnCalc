package kz.kegoc.bln;

import kz.kegoc.bln.calc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import java.time.LocalDateTime;

@EntityScan(
    basePackageClasses = { App.class, Jsr310JpaConverters.class }
)
@SpringBootApplication
public class App implements ApplicationListener<ApplicationReadyEvent> {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        CalcContext context = new CalcContext();
        context.setDate(LocalDateTime.of(2018, 3, 28, 0, 0, 0));
        context.setStart(LocalDateTime.of(2018, 3, 28, 0, 0, 0));
        context.setEnd(LocalDateTime.of(2018, 3, 29, 0, 0, 0));
        System.out.println(formula.calc(context).getValue());
    }

    @Autowired
    private Formula formula;
}
