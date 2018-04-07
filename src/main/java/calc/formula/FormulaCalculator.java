package calc.formula;

public interface FormulaCalculator {
    Double calc(String formula, CalcContext context) throws Exception;
}
