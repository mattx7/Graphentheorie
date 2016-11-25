package algorithms.optimal_ways;

import algorithms.Preconditions;
import com.google.common.collect.Lists;
import helper.GraphUtil;
import org.apache.log4j.Logger;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by MattX7 on 20.11.2016.
 *
 */
public class Dijkstra implements Algorithm {
    private static Logger logger = Logger.getLogger(Dijkstra.class);
    static boolean preview = true;

    Double distance;
    Integer hits = 0;

    private Node[] nodes;
    private Double[] entf;
    private Node[] vorg;
    private Boolean[] ok;
    private Graph graph;
    private Node source;
    private Node target;

    /**
     * Initialization of the algorithm. This method has to be called before the
     * {@link #compute()} method to initialize or reset the algorithm according
     * to the new given graph.
     *
     * @param graph The graph this algorithm is using.
     */
    public void init(Graph graph) {
        Preconditions.mustHaveWeights(graph);
        Preconditions.noNegativeWeights(graph);

        this.graph = graph;
        int size = graph.getNodeCount();
        nodes = new Node[size];
        entf = new Double[size];
        vorg = new Node[size];
        ok = new Boolean[size];
        setSourceAndTarget(graph.getNode(0), graph.getNode(size - 1));
    }

    /**
     * Run the algorithm. The {@link #init(Graph)} method has to be called
     * before computing.
     *
     * @see #init(Graph)
     */
    public void compute() {
        logger.debug("Starting Dijkstra with " + GraphUtil.graphToString(graph, false, false));
        // Preconditions
        if (graph == null || source == null || target == null) // have to be set
            throw new IllegalArgumentException("Attributes are missing");


        // Implementation
        setUp(); // Attribute setzen und mit Standartwerten belegen
        if (preview) printMatrix();
        calcNewDistance(source);
        do {
            // Knoten mit minimaler Distanz auswählen
            Node currentNode = withMinDistance();
            // Setze OKh := true.
            ok[getIndex(currentNode)] = true;

            // Berechne für alle noch unbesuchten Nachbarknoten die Summe des jeweiligen Kantengewichtes und der Distanz.
            // Ist dieser Wert für einen Knoten kleiner als die dort gespeicherte Distanz, aktualisiere sie und setze den aktuellen Knoten als Vorgänger.
            calcNewDistance(currentNode);
            if (preview)
                System.out.println("============================ " + currentNode.getId() + " ============================");
            if (preview) printMatrix();
        } while (asLongAsWeHaveNodesWithFalse());
        //distance = target.getAttribute("Distance"); // TODO
        distance = entf[getIndex(target)];
        reset();
    }

    /**
     * Returns the shortest Path from the Source to the Target
     *
     * @return the shortest Path from the Source to the Target
     */
    List<Node> getShortestPath() {
        if (hits == 0)
            throw new IllegalArgumentException("do compute before this method");

        List<Node> shortestPath = new LinkedList<Node>();

        shortestPath.add(target);
        Node current = target;
        while (hasPred(current)) {
            shortestPath.add(getPred(current));
            current = getPred(current);
        }
        return Lists.reverse(shortestPath);
    }

    /**
     * Sets source and target before compute()
     *
     * @param source source node
     * @param target target node
     */
    void setSourceAndTarget(@NotNull Node source,
                            @NotNull Node target) {
        if (this.source != null && this.source.hasAttribute("title"))
            this.source.removeAttribute("title");
        if (this.target != null && this.target.hasAttribute("title"))
            this.target.removeAttribute("title");
        this.source = source;
        this.target = target;
        source.setAttribute("title", "source");
        target.setAttribute("title", "target");
    }

    /**
     * Returns true if we still have some "false" in the ok list
     *
     * @return boolean
     */
    private boolean asLongAsWeHaveNodesWithFalse() {
        for (Boolean anOk : ok) {
            if (!anOk) return true;
        }
        return false;
    }

    private Node getPred(Node node) {
        return vorg[getIndex(node)];
    }

    private boolean hasPred(Node node) {
        return node != vorg[getIndex(node)];

    }

    // === PRIVATE ===

    private void setUp() {
        // nodes
        nodes[0] = source;
        Iterator<Node> nodeIterator = graph.getNodeIterator();
        int n = 1;
        while (nodeIterator.hasNext()) {
            Node next = nodeIterator.next();
            if (source != next) {
                nodes[n] = next;
                n++;
            }
        }

        // Entfernung
        entf[0] = 0.0;
        for (int i = 1; i < entf.length; i++) {
            entf[i] = Double.POSITIVE_INFINITY;
        }
        // Vorgänger
        vorg[0] = source;
        // OK?
        for (int i = 0; i < ok.length; i++) {
            ok[i] = false;
        }
    }

    private void calcNewDistance(@NotNull Node currNode) {
        // Berechne für alle noch unbesuchten Nachbarknoten die Summe des jeweiligen Kantengewichtes und der Distanz.
        Iterator<Edge> leavingEdgeIterator = currNode.getLeavingEdgeIterator();
        while (leavingEdgeIterator.hasNext()) {
            Edge leavingEdge = leavingEdgeIterator.next();
            Node leavingNode = getRightNode(currNode, leavingEdge);
            hits += 2;

            if (!ok[getIndex(leavingNode)]) {
                Double entfCurr = entf[getIndex(currNode)]; // entf von aktuellen Knoten aus der Matrix holen
                Double entfLeaving = entf[getIndex(leavingNode)]; // entf vom neuten Knoten holen
                Double weightLeavingEdge = Double.parseDouble(leavingEdge.getAttribute("weight").toString());
                hits++;

                if (entfLeaving > entfCurr + weightLeavingEdge) {
                    // Ist dieser Wert für einen Knoten kleiner als die dort gespeicherte Distanz, aktualisiere sie und setze den aktuellen Knoten als Vorgänger.
                    entf[getIndex(leavingNode)] = entfCurr + weightLeavingEdge;
                    vorg[getIndex(leavingNode)] = currNode;
                }
            }
        }
    }

    /**
     * Returns index of a Node
     *
     * @param node from we want to know the index
     * @return index
     */
    @NotNull
    private Integer getIndex(@NotNull Node node) {
        for (int i = 0; i <= nodes.length; i++) {
            if (nodes[i] == node)
                return i;
        }
        throw new IllegalArgumentException();
    }

    @NotNull
    private Node getRightNode(@NotNull Node currNode, @NotNull Edge leavingEdge) {
        Node node;
        if (leavingEdge.getNode1().equals(currNode))
            node = leavingEdge.getNode0();
        else
            node = leavingEdge.getNode1();
        return node;
    }


    @NotNull
    private Node withMinDistance() {
        Node min;
        for (int i = 0; i < nodes.length; i++) {
            if (!ok[i]) {
                min = nodes[i];
                for (int j = 0; j < nodes.length; j++) {
                    if (!ok[j]) {
                        if (entf[j] < entf[getIndex(min)]) {
                            min = nodes[j];
                        }
                    }
                }
                return min;
            }
        }
        throw new IllegalArgumentException();
    }

    private void reset() {
        source.removeAttribute("title");
        target.removeAttribute("title");
    }

    /**
     * Output for the distance matrix
     */
    private void printMatrix() {
        System.out.print("\t\t");
        for (Node node : nodes) { // print x nodes
            System.out.print(node.getId() + "\t\t\t");
        }
        System.out.println();
        System.out.print("entf |\t");
        for (int i = 0; i < nodes.length; i++) {
            System.out.print((entf[i].isInfinite() ? "Inf." : entf[i]) + "\t\t\t");
        }
        System.out.println();
        System.out.print("vorg |\t");
        for (int i = 0; i < nodes.length; i++) {
            System.out.print(vorg[i] + "\t\t\t");
        }
        System.out.println();
        System.out.print("ok   |\t");
        for (int i = 0; i < nodes.length; i++) {
            System.out.print(ok[i] + "\t\t");
        }
        System.out.println();
        System.out.println();

    }

    // === MAIN ===

    public static void main(String[] args) throws Exception {
        // Graph aus den Folien
        // 02_GKA-Optimale Wege.pdf Folie 2 und 6
        Graph graph = new SingleGraph("graph");

        graph.addNode("v1");
        graph.addNode("v2");
        graph.addNode("v3");
        graph.addNode("v4");
        graph.addNode("v5");
        graph.addNode("v6");

        graph.addEdge("v1v2", "v1", "v2", true).addAttribute("weight", 1.0);
        graph.addEdge("v1v6", "v1", "v6", true).addAttribute("weight", 3.0);
        graph.addEdge("v2v3", "v2", "v3", true).addAttribute("weight", 5.0);
        graph.addEdge("v2v5", "v2", "v5", true).addAttribute("weight", 3.0);
        graph.addEdge("v2v6", "v2", "v6", true).addAttribute("weight", 2.0);

        graph.addEdge("v3v6", "v3", "v6", true).addAttribute("weight", 2.0);
        graph.addEdge("v3v5", "v3", "v5", true).addAttribute("weight", 2.0);
        graph.addEdge("v3v4", "v3", "v4", true).addAttribute("weight", 1.0);
        graph.addEdge("v5v4", "v5", "v4", true).addAttribute("weight", 3.0);
        graph.addEdge("v5v6", "v5", "v6", true).addAttribute("weight", 1.0);

        Dijkstra dijk = new Dijkstra();
        dijk.init(graph);
        dijk.setSourceAndTarget(graph.getNode("v1"), graph.getNode("v4"));
        dijk.compute();
    }
}