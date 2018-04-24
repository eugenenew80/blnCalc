package calc;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class App2 {
    private static final String XSL_FILE = "files/report.xsl";
    private static final String INPUT_FILE = "files/report.xml";
    private static final String OUTPUT_FILE = "files/output.xml";

    public static void main(String[] args) throws Exception {
        StreamSource xslCode = new StreamSource(new File(XSL_FILE));
        StreamSource input = new StreamSource(new File(INPUT_FILE));
        StreamResult output = new StreamResult(new File(OUTPUT_FILE));

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer(xslCode);

        trans.transform(input, output);

    }
}
