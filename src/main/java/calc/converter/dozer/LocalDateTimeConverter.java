package calc.converter.dozer;

import org.dozer.DozerConverter;

import java.time.LocalDateTime;

public class LocalDateTimeConverter extends DozerConverter<LocalDateTime, LocalDateTime> {

    public LocalDateTimeConverter() {
        super(LocalDateTime.class, LocalDateTime.class);
    }

    @Override
    public LocalDateTime convertTo(LocalDateTime source, LocalDateTime destination) {
        if (source==null) return null;
        return LocalDateTime.of(source.getYear(), source.getMonth(), source.getDayOfMonth(), source.getHour(), source.getMinute(), source.getSecond());
    }

    @Override
    public LocalDateTime convertFrom(LocalDateTime source, LocalDateTime destination) {
        if (source==null) return null;
        return LocalDateTime.of(source.getYear(), source.getMonth(), source.getDayOfMonth(), source.getHour(), source.getMinute(), source.getSecond());
    }
}
