package calc.formula.sort;

import java.util.*;

public class Graph {
    private int maxSize;
    private int curSize;
    private int adjMat[][];
    private List<Vertex> vertexList = new ArrayList<>();
    private Deque<Vertex> sortedList = new LinkedList<>();

    public Graph(int maxSize) {
        this.maxSize = maxSize;
        adjMat = new int[maxSize][maxSize];
        curSize = 0;

        for(int j=0; j<maxSize; j++)
            for(int k=0; k<maxSize; k++)
                adjMat[j][k] = 0;
    }

    public void addVertex(Vertex vertex) {
        vertexList.add(vertex);
        curSize++;
    }

    public void addEdge(int start, int end) {
        adjMat[start][end] = 1;
    }

    public List<Vertex> topo() {
        while(curSize > 0) {
            int currentVertex = noSuccessors();
            if(currentVertex == -1)
                throw new RuntimeException("Обнаружена циклическая формула");

            sortedList.addFirst(vertexList.get(currentVertex));
            deleteVertex(currentVertex);
        }

        return new ArrayList<>(sortedList);
    }

    public int noSuccessors() {
        boolean isEdge;

        for (int row=0; row<curSize; row++) {
            isEdge = false;
            for(int col=0; col<curSize; col++) {
                if( adjMat[row][col] > 0 ) {
                    isEdge = true;
                    break;
                }
            }
            if( !isEdge )
                return row;
        }

        return -1;
    }

    public void deleteVertex(int delVert) {
        vertexList.remove(delVert);

        if(delVert != curSize-1) {
            for(int row=delVert; row<curSize-1; row++)
                moveRowUp(row, curSize);

            for(int col=delVert; col<curSize-1; col++)
                moveColLeft(col, curSize-1);
        }

        curSize--;
    }

    private void moveRowUp(int row, int length) {
        for(int col=0; col<length; col++)
            adjMat[row][col] = adjMat[row+1][col];
    }

    private void moveColLeft(int col, int length) {
        for(int row=0; row<length; row++)
            adjMat[row][col] = adjMat[row][col+1];
    }
}
