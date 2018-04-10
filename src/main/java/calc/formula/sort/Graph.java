package calc.formula.sort;

import calc.entity.MeteringPoint;

import java.util.*;

public class Graph {
    private int maxSize;
    private int curSize;
    private int adjMat[][];
    private List<MeteringPoint> meteringPoints = new ArrayList<>();
    private Deque<MeteringPoint> sortedList = new LinkedList<>();

    public Graph(int maxSize) {
        this.maxSize = maxSize;
        adjMat = new int[maxSize][maxSize];
        curSize = 0;

        for(int j=0; j<maxSize; j++)
            for(int k=0; k<maxSize; k++)
                adjMat[j][k] = 0;
    }

    public void addVertex(MeteringPoint meteringPoint) {
        meteringPoints.add(meteringPoint);
        curSize++;
    }

    public void addEdge(int start, int end) {
        adjMat[start][end] = 1;
    }

    public List<MeteringPoint> topo() {
        while(curSize > 0) {
            int current = noSuccessors();
            if(current == -1)
                throw new RuntimeException("Обнаружена циклическая формула");

            sortedList.addFirst(meteringPoints.get(current));
            deleteMeteringPoint(current);
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

    public void deleteMeteringPoint(int index) {
        meteringPoints.remove(index);

        if(index != curSize-1) {
            for(int row=index; row<curSize-1; row++)
                moveRowUp(row, curSize);

            for(int col=index; col<curSize-1; col++)
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
