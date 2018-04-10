package calc.formula.sort;

import lombok.Data;

@Data
public class Vertex {
    private final String code;

    public Vertex(String code) {
        this.code = code;
    }
}

