package calc.converter.dozer;

import org.dozer.DozerConverter;
import java.time.LocalDate;

public class LocalDateConverter extends DozerConverter<LocalDate, LocalDate> {

    public LocalDateConverter() {
        super(LocalDate.class, LocalDate.class);
    }

    @Override
    public LocalDate convertTo(LocalDate source, LocalDate destination) {
        if (source==null) return null;
        return LocalDate.of(source.getYear(), source.getMonth(), source.getDayOfMonth());
    }

    @Override
    public LocalDate convertFrom(LocalDate source, LocalDate destination) {
        if (source==null) return null;
        return LocalDate.of(source.getYear(), source.getMonth(), source.getDayOfMonth());
    }
}
