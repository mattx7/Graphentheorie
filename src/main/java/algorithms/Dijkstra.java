package algorithms;

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
 * Created by MattX7 on 03.11.2016.
 */
public class Dijkstra implements Algorithm {
    private static Logger logger = Logger.getLogger(Dijkstra.class);

    public static boolean preview = true;
    public Double distance;
    public Integer steps = 0;
    private Graph graph;
    private Node source;
    private Node target;
    private LinkedList<Node> uncheckedNodes; // TODO could be a set

    /**
     * Initialisation
     *
     * @param graph
     */
    public void init(Graph graph) {
        this.graph = graph;
        //  setSourceAndTarget(graph.getNode(0), graph.getNode(graph.getNodeCount() - 1));
        uncheckedNodes = new LinkedList<Node>();
    }

    /**
     * starts the algorithm
     */
    public void compute() {
        logger.debug("Starting Dijkstra with " + GraphUtil.graphToString(graph, false, false));
        // Preconditions
        if (graph == null || source == null || target == null) // have to be set
            throw new IllegalArgumentException();
        if (!hasWeights(graph))
            throw new IllegalArgumentException();

        // Implementation
        if (preview) GraphUtil.buildForDisplay(graph).display(); // TODO visualisierung des algos
        setUp(); // Attribute setzen und mit Standartwerten belegen

        calcNewDistance(source);

        while (!uncheckedNodes.isEmpty()) {
            logger.debug(uncheckedNodes.toString());
            // Knoten mit minimaler Distanz auswählen
            Node currentNode = withMinDistance();
            // speichere, dass dieser Knoten schon besucht wurde.
            uncheckedNodes.remove(currentNode);
            // Berechne für alle noch unbesuchten Nachbarknoten die Summe des jeweiligen Kantengewichtes und der Distanz.
            // Ist dieser Wert für einen Knoten kleiner als die dort gespeicherte Distanz, aktualisiere sie und setze den aktuellen Knoten als Vorgänger.
            calcNewDistance(currentNode);
            steps += 1;
        }
        distance = target.getAttribute("Distance");
        reset();
    }

    /**
     * Returns the shortest Path from the Source to the Target
     *
     * @return the shortest Path from the Source to the Target
     */
    public List<Node> getShortestPath() {
        // TODO getShortestPath()
        return null;
    }

    /**
     * Sets source and target before compute()
     *
     * @param source source node
     * @param target target node
     */
    public void setSourceAndTarget(@NotNull Node source,
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

    // === PRIVATE ===

    private void setUp() {
        for (Node node : graph) {
            if (!node.equals(source)) {
                node.addAttribute("Distance", Double.POSITIVE_INFINITY);
                node.addAttribute("OK", false);
                node.addAttribute("Predecessor", 0);
                uncheckedNodes.add(node); // add all nodes
                logger.debug(node.getId() + " | Dist.: Inf. | OK: false | Pred.: 0");
                if (preview) updateLabel(node);
            } else {
                source.addAttribute("Distance", 0.0);
                source.addAttribute("OK", true);
                source.addAttribute("Predecessor", source);
                logger.debug(source.getId() + " | Dist.: 0 | OK: true | Pred.: " + source.getId());
            }
        } // TODO unterschied set und add Attribut
        for (Edge edge : graph.getEachEdge()) {
            edge.addAttribute("ui.label", edge.getAttribute("weight"));
        }

    }

    private void calcNewDistance(@NotNull Node currNode) {
        // Berechne für alle noch unbesuchten Nachbarknoten die Summe des jeweiligen Kantengewichtes und der Distanz.
        Iterator<Edge> leavingEdgeIterator = currNode.getLeavingEdgeIterator();
        if (preview) currNode.setAttribute("ui.class", "markRed");
        GraphUtil.sleepLong();
        while (leavingEdgeIterator.hasNext()) {
            Edge leavingEdge = leavingEdgeIterator.next();
            // Ist dieser Wert für einen Knoten kleiner als die dort gespeicherte Distanz, aktualisiere sie und setze den aktuellen Knoten als Vorgänger.
            String weight1 = currNode.getAttribute("Distance").toString();
            String weight2 = leavingEdge.getAttribute("weight").toString();
            Double newDist = (Double.parseDouble(weight1)) + (Double.parseDouble(weight2));
            Node nodeFromLeavingEdge = getRightNode(currNode, leavingEdge); // TODO ist das notwendig?

            if (preview) nodeFromLeavingEdge.setAttribute("ui.class", "markBlue");
            if (!((Boolean) nodeFromLeavingEdge.getAttribute("OK")) &&
                    (newDist < (Double) nodeFromLeavingEdge.getAttribute("Distance"))) {
                nodeFromLeavingEdge.setAttribute("Distance", newDist);
                nodeFromLeavingEdge.setAttribute("Predecessor", currNode);
                if (preview) updateLabel(nodeFromLeavingEdge);
            }
            GraphUtil.sleepLong();
            if (preview) nodeFromLeavingEdge.setAttribute("ui.class", "");
        }
        currNode.setAttribute("ui.class", "");
    }

    private void updateLabel(Node node) {
        node.setAttribute("ui.label", node.getId()
                + " | Dist.: " + node.getAttribute("Distance")
                + " | OK.: " + node.getAttribute("OK")
                + " | Pred..: " + node.getAttribute("Predecessor")
        );
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
        Node min = uncheckedNodes.getFirst();
        for (Node cur : uncheckedNodes) {
            if (((Double) cur.getAttribute("Distance") < ((Double) min.getAttribute("Distance"))))
                min = cur;
        }
        return min;
    }

    @NotNull
    private Boolean hasWeights(@NotNull Graph graph) {
        boolean hasWeight = true;
        for (Edge edge : graph.getEachEdge()) {
            if (!edge.hasAttribute("weight"))
                hasWeight = false;
        }
        return hasWeight;
    }

    private void reset() {
        for (Node node : graph.getEachNode()) {
            node.removeAttribute("Distance");
            node.removeAttribute("OK");
            node.removeAttribute("Predecessor");
        }
        source.removeAttribute("title");
        target.removeAttribute("title");
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

        graph.addEdge("v1v2", "v1", "v2").addAttribute("weight", 1.0);
        graph.addEdge("v1v6", "v1", "v6").addAttribute("weight", 3.0);
        graph.addEdge("v2v3", "v2", "v3").addAttribute("weight", 5.0);
        graph.addEdge("v2v5", "v2", "v5").addAttribute("weight", 2.0);
        graph.addEdge("v2v6", "v2", "v6").addAttribute("weight", 3.0);

        graph.addEdge("v3v6", "v3", "v6").addAttribute("weight", 2.0);
        graph.addEdge("v3v5", "v3", "v5").addAttribute("weight", 2.0);
        graph.addEdge("v3v4", "v3", "v4").addAttribute("weight", 1.0);
        graph.addEdge("v5v4", "v5", "v4").addAttribute("weight", 3.0);
        graph.addEdge("v5v6", "v5", "v6").addAttribute("weight", 1.0);

        Dijkstra.preview = true;
        Dijkstra dijk = new Dijkstra();
        dijk.init(graph);
        dijk.setSourceAndTarget(graph.getNode("v1"), graph.getNode("v4"));
        dijk.compute();
    }
}
