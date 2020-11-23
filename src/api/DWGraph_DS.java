package api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class DWGraph_DS implements directed_weighted_graph {

    private int numOfEdges = 0;
    private int modeCount = 0; //represents number of changes.
    private HashMap<Integer, node_data> nodes;

    public DWGraph_DS() {
        this.nodes = new HashMap<Integer, node_data>();
    }


    @Override
    public node_data getNode(int key) {
        return nodes.get(key);
    }

    @Override
    public edge_data getEdge(int src, int dest) {
        if (!nodes.containsKey(src)) return null;
        node_data sourceNode = nodes.get(src);
        return ((NodeData) sourceNode).getEdge(dest);
    }

    @Override
    public void addNode(node_data n) {
        this.modeCount += this.nodes.putIfAbsent(n.getKey(), n) == null ? 1 : 0; // if the node was already in the graph - it will simply do nothing, if it wasn't - it will add it to the graph and increment modeCount by 1
    }

    @Override
    public void connect(int src, int dest, double w) {
        if (src == dest) return; //do nothing if trying to connect a node to itself
        if (w < 0) return; //do nothing if weight is less than 0
        NodeData sourceNode = (NodeData) this.nodes.get(src);
        NodeData destNode = (NodeData) this.nodes.get(dest);
        numOfEdges += sourceNode.hasNi(destNode.getKey()) ? 0 : 1; // add 1 to numOfEdges if there is no connection yet.
        sourceNode.connectEdge(destNode, w); //connect source node to dest node / update weight if already connected.
        modeCount++;
    }

    @Override
    public Collection<node_data> getV() {
        return this.nodes.values();
    }

    @Override
    public Collection<edge_data> getE(int node_id) {
        return ((NodeData) this.nodes.get(node_id)).getNeighborEdges().values();
    }

    @Override
    public node_data removeNode(int key) {
        if (!this.nodes.containsKey(key))
            return null; // return null if the node we wish to remove does not exist in the graph

        NodeData nodeToRemove = (NodeData) this.nodes.get(key);
        this.numOfEdges -= nodeToRemove.getNeighborEdges().size();
        this.numOfEdges -= nodeToRemove.getEdgesConnectedToThisNode().size();

        Iterator<edge_data> sourceItr = nodeToRemove.getEdgesConnectedToThisNode().values().iterator();

        while (sourceItr.hasNext()) { // We are iterating over all edges directed at the node that we want to remove
            EdgeData edgeToRemove = (EdgeData) sourceItr.next();
            ((NodeData) (nodes.get(edgeToRemove.getSrc()))).getNeighborEdges().remove(edgeToRemove.getDest()); // Remove the edge from a node that directs to this node
            this.modeCount++;
        }

        Iterator<edge_data> destItr = getE(nodeToRemove.getKey()).iterator();

        while (destItr.hasNext()) { //We are iterating over all edges coming from the node that we want to remove
            EdgeData edgeToRemove = (EdgeData) destItr.next();
            NodeData nodeConnectedFromThisNode = (NodeData) nodes.get(edgeToRemove.getDest()); //get the node that the edge is directed at (the destination node)
            nodeConnectedFromThisNode.getEdgesConnectedToThisNode().remove(nodeToRemove.getKey()); //remove the edge from the destination node
            nodeToRemove.getNeighborEdges().remove(edgeToRemove.getDest()); //remove the edge from the source node
            this.modeCount++;
        }

        this.modeCount++;
        return this.nodes.remove(key);

    }

    @Override
    public edge_data removeEdge(int src, int dest) {
        NodeData nodeToRemove = (NodeData) this.nodes.get(src);
        if (!nodeToRemove.hasNi(dest)) return null; //return null if there is no edge from src to dest
        EdgeData edgeToRemove = (EdgeData) nodeToRemove.getEdge(dest);
        NodeData nodeConnectedFromThisNode = (NodeData) nodes.get(edgeToRemove.getDest()); //get the node that the edge is directed at (the destination node)
        nodeConnectedFromThisNode.getEdgesConnectedToThisNode().remove(nodeToRemove.getKey()); //remove the edge from the destination node
        nodeToRemove.getNeighborEdges().remove(edgeToRemove.getDest()); //remove the edge from the source node
        this.modeCount++;
        this.numOfEdges--;
        return edgeToRemove;
    }

    @Override
    public int nodeSize() {
        return this.nodes.size();
    }

    @Override
    public int edgeSize() {
        return this.numOfEdges;
    }

    @Override
    public int getMC() {
        return this.modeCount;
    }
}

// ---------------------------------------------------------------------------------------------------------------------------------------------------------------

class NodeData implements node_data {

    private int key;
    private HashMap<Integer, edge_data> neighborEdges; //edges coming out from this node.
    private HashMap<Integer, edge_data> edgesConnectedToThisNode; // when we want to remove a node, we need to have a reference to the nodes that are connected to this node.
    private double weight;
    private String info;
    private int tag;


    public NodeData(int key) {
        this.key = key;
        this.neighborEdges = new HashMap<Integer, edge_data>();
        this.edgesConnectedToThisNode = new HashMap<Integer, edge_data>();
        this.weight = 0.0;
        this.info = "";
        this.tag = 0;
    }

    public void connectEdge(NodeData destNode, double w) {
        EdgeData edge = new EdgeData(this.getKey(), destNode.getKey(), w);
        this.neighborEdges.put(destNode.getKey(), edge);
        destNode.edgesConnectedToThisNode.put(this.getKey(), edge);
    }

    @Override
    public int getKey() {
        return this.key;
    }

    @Override
    public geo_location getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setLocation(geo_location p) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getWeight() {
        return this.weight;
    }

    @Override
    public void setWeight(double w) {
        this.weight = w;

    }

    @Override
    public String getInfo() {
        return this.info;
    }

    @Override
    public void setInfo(String s) {
        this.info = s;

    }

    @Override
    public int getTag() {
        return this.tag;
    }

    @Override
    public void setTag(int t) {
        this.tag = t;

    }

    public boolean hasNi(int nodeKey) {
        return this.neighborEdges.get(nodeKey) != null ? true : false;
    }

    public edge_data getEdge(int nodeKey) {
        return this.neighborEdges.get(nodeKey);
    }

    public HashMap<Integer, edge_data> getNeighborEdges() {
        return this.neighborEdges;
    }

    public HashMap<Integer, edge_data> getEdgesConnectedToThisNode() {
        return this.edgesConnectedToThisNode;
    }
}

//---------------------------------------------------------------------------------------------------------------------------------------------------------------

class EdgeData implements edge_data {

    private int sourceKey;
    private int destKey;
    private double weight;
    private String info;
    private int tag;

    public EdgeData(int sourceKey, int destKey, double weight) {
        this.sourceKey = sourceKey;
        this.destKey = destKey;
        this.weight = weight;
        this.info = "";
        this.tag = 0;
    }

    @Override
    public int getSrc() {
        return this.sourceKey;
    }

    @Override
    public int getDest() {
        return this.destKey;
    }

    @Override
    public double getWeight() {
        return this.weight;
    }

    @Override
    public String getInfo() {
        return this.info;
    }

    @Override
    public void setInfo(String s) {
        this.info = s;
    }

    @Override
    public int getTag() {
        return this.tag;
    }

    @Override
    public void setTag(int t) {
        this.tag = t;
    }

}	