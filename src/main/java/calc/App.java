package calc;

import calc.entity.MeteringPoint;
import calc.formula.sort.Graph;
import calc.formula.sort.Vertex;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.util.List;

@EntityScan(
    basePackageClasses = { App.class, Jsr310JpaConverters.class }
)
@SpringBootApplication
public class App  {
    public static void main(String[] args) {

        Graph theGraph = new Graph(20);
        theGraph.addVertex(new MeteringPoint("A"));
        theGraph.addVertex(new MeteringPoint("B"));
        theGraph.addVertex(new MeteringPoint("C"));
        theGraph.addVertex(new MeteringPoint("D"));
        theGraph.addVertex(new MeteringPoint("E"));
        theGraph.addVertex(new MeteringPoint("F"));
        theGraph.addVertex(new MeteringPoint("G"));
        theGraph.addVertex(new MeteringPoint("H"));

        theGraph.addEdge(0, 3);
        theGraph.addEdge(0, 4);
        theGraph.addEdge(1, 4);
        theGraph.addEdge(2, 5);
        theGraph.addEdge(3, 6);
        theGraph.addEdge(4, 6);
        theGraph.addEdge(5, 7);
        theGraph.addEdge(6, 7);

        theGraph.topo().stream().forEach(System.out::println);

        SpringApplication.run(App.class, args);
    }
}
