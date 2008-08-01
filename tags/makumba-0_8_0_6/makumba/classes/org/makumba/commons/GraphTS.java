package org.makumba.commons;

/**
 * Graph which can do a topological sort of its elements. In our case, the elements are MultipleKeys of form tags.
 * 
 * @author http://www.java2s.com/Code/Java/Collections-Data-Structure/Topologicalsorting.htm
 * @author Manuel Gay
 * @version $Id: GraphTS.java,v 1.1 23.10.2007 16:34:34 Manuel Exp $
 */
public class GraphTS {
    private final int MAX_VERTS = 20;

    private Vertex vertexList[]; // list of vertices

    private int matrix[][]; // adjacency matrix

    private int numVerts; // current number of vertices

    private MultipleKey sortedArray[];

    public GraphTS() {
        vertexList = new Vertex[MAX_VERTS];
        matrix = new int[MAX_VERTS][MAX_VERTS];
        numVerts = 0;
        for (int i = 0; i < MAX_VERTS; i++)
            for (int k = 0; k < MAX_VERTS; k++)
                matrix[i][k] = 0;
        sortedArray = new MultipleKey[MAX_VERTS]; // sorted vert labels
    }

    public void addVertex(char lab) {
        vertexList[numVerts++] = new Vertex(lab);
    }

    public int addVertex(MultipleKey tagKey) {
        vertexList[numVerts++] = new Vertex(tagKey);
        return (numVerts - 1);
    }

    public void addEdge(int start, int end) {
        matrix[start][end] = 1;
    }

    public void displayVertex(int v) {
        System.out.print(vertexList[v].label);
    }

    public MultipleKey[] getSortedKeys() {
        return this.sortedArray;
    }

    public void topo() {// toplogical sort
        while (numVerts > 0) {// while vertices remain,
            // get a vertex with no successors, or -1
            int currentVertex = noSuccessors();
            if (currentVertex == -1) {// must be a cycle
                System.out.println("ERROR: Graph has cycles");
                return;
            }
            // insert vertex label in sorted array (start at end)
            sortedArray[numVerts - 1] = vertexList[currentVertex].tagKey;

            deleteVertex(currentVertex); // delete vertex
        }

        // vertices all gone; display sortedArray
        /*
         * System.out.print("Topologically sorted order: "); for (int j = 0; j < orig_nVerts; j++) {
         * System.out.println("*** Key number " + j); MultipleKey thisKey = sortedArray[j]; Iterator it =
         * thisKey.iterator(); while (it.hasNext()) { System.out.println("*** *** Key content: " + it.next()); } }
         * System.out.println("");
         */
    }

    public int noSuccessors() { // returns vert with no successors (or -1 if no such verts)
        boolean isEdge; // edge from row to column in adjMat

        for (int row = 0; row < numVerts; row++) {
            isEdge = false; // check edges
            for (int col = 0; col < numVerts; col++) {
                if (matrix[row][col] > 0) {// if edge to another,
                    isEdge = true;
                    break; // this vertex has a successor try another
                }
            }
            if (!isEdge) {// if no edges, has no successors
                return row;
            }
        }
        return -1; // no
    }

    public void deleteVertex(int delVert) {
        if (delVert != numVerts - 1) // if not last vertex, delete from vertexList
        {
            for (int j = delVert; j < numVerts - 1; j++)
                vertexList[j] = vertexList[j + 1];

            for (int row = delVert; row < numVerts - 1; row++)
                moveRowUp(row, numVerts);

            for (int col = delVert; col < numVerts - 1; col++)
                moveColLeft(col, numVerts - 1);
        }
        numVerts--; // one less vertex
    }

    private void moveRowUp(int row, int length) {
        for (int col = 0; col < length; col++)
            matrix[row][col] = matrix[row + 1][col];
    }

    private void moveColLeft(int col, int length) {
        for (int row = 0; row < length; row++)
            matrix[row][col] = matrix[row][col + 1];
    }

    public static void main(String[] args) {
        GraphTS g = new GraphTS();
        g.addVertex('A'); // 0
        g.addVertex('B'); // 1
        g.addVertex('C'); // 2
        g.addVertex('D'); // 3
        g.addVertex('E'); // 4
        g.addVertex('F'); // 5
        g.addVertex('G'); // 6
        g.addVertex('H'); // 7

        g.addEdge(0, 3); // AD
        g.addEdge(0, 4); // AE
        g.addEdge(1, 4); // BE
        g.addEdge(2, 5); // CF
        g.addEdge(3, 6); // DG
        g.addEdge(4, 6); // EG
        g.addEdge(5, 7); // FH
        g.addEdge(6, 7); // GH

        g.topo(); // do the sort
    }
}

class Vertex {
    protected char label;

    protected MultipleKey tagKey;

    public Vertex(char lab) {
        label = lab;
    }

    public Vertex(MultipleKey tagKey) {
        this.tagKey = tagKey;
    }

}
