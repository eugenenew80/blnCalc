package kz.kegoc.bln.calc.formula;

import kz.kegoc.bln.calc.CalcContext;

import java.io.File;
import java.nio.file.Path;

public interface FormulaCalculator {
    Double calc(String formula, CalcContext context) throws Exception;
    Double calc(File file, CalcContext context) throws Exception;
}
