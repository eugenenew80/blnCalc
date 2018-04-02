package kz.kegoc.bln.calc.service;

import kz.kegoc.bln.calc.CalcContext;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

public interface FormulaCalculator {
    Double calc(String formula, CalcContext context);
    Double calc(File file, CalcContext context) throws IOException, SAXException, Exception;
}
