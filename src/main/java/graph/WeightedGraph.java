package graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;



public class WeightedGraph implements DirectedGraph<Vertex, Edge>  {

	public ArrayList<Vertex> vertices=new ArrayList<Vertex>();
	public ArrayList<Edge> edges=new ArrayList<Edge>();
	
	public WeightedGraph(){
		
		
	}
	@Override
	public boolean addEdge(Edge edge, Vertex from, Vertex to) {
		
	

			
		for (Edge e:edges){
			if (edge.equals(e) )
				return false;
		}
		int y=0;
		if(from.name.equals(to.name)){
			y=0;
		}
		
		for(Vertex v:vertices)
			if(v.name.equals(from.name)){
				edge.from=v;
				from=v;
				break;
			}
		for(Vertex v:vertices)
			if(v.name.equals(to.name)){
				edge.to=v;
				to=v;
				break;
			}
		
	//	edge.from=from;
	//	edge.to=to;
		
		to.addInEdges(edge);
		from.addOutEdges(edge);
		edges.add(edge);
		addVertex(to);
		addVertex(from);
		return true;
	}

	@Override
	public boolean addEdge(Edge arg0, Vertex arg1, Vertex arg2, EdgeType arg3) {
		return false;
	}

	@Override
	public Vertex getDest(Edge edge) {
		return edge.to;
		
	}

	public double getSumWeightOutEdges(Vertex v){
		
		HashSet<Edge> outedges=v.outEdges;
		Iterator<Edge> it=outedges.iterator();
		double sum=0.0;
		while(it.hasNext()){
			Edge edge=it.next();
			sum+=edge.getWeight();
		}
		return sum;
	}
	@Override
	public Pair<Vertex> getEndpoints(Edge edge) {
		
		Pair<Vertex> pair=new Pair<Vertex>(edge.from, edge.to);
		return pair;
	}

	@Override
	public Collection<Edge> getInEdges(Vertex vertex) {
		
		return vertex.inEdges;
		
	}

	@Override
	public Vertex getOpposite(Vertex vertex, Edge edge) {
		
		if (edge.from.equals(vertex))
			return edge.to;
		else
			if (edge.to.equals(vertex))
				return edge.from;
		
		return null;
		
		
	}

	@Override
	public Collection<Edge> getOutEdges(Vertex vertex) {
		
		return vertex.outEdges;
	}

	@Override
	public int getPredecessorCount(Vertex vertex) {
		

		return getPredecessors(vertex).size();
	}

	@Override
	public Collection<Vertex> getPredecessors(Vertex vertex) {
		HashSet<Edge> inedges=vertex.inEdges;
		Iterator<Edge> it=vertex.inEdges.iterator();
		List<Vertex> predVertices=new ArrayList<Vertex>();
		while (it.hasNext()){
			
			Edge in=it.next();
			predVertices.add(in.from);
			
		}
		return predVertices;
	}

	@Override
	public Vertex getSource(Edge edge) {
		
		return edge.from;
	}

	@Override
	public int getSuccessorCount(Vertex vertex) {
		return getSuccessors(vertex).size();
	}

	@Override
	public Collection<Vertex> getSuccessors(Vertex vertex) {
		HashSet<Edge> outedges=vertex.outEdges;
		Iterator<Edge> it=vertex.outEdges.iterator();
		List<Vertex> succVertices=new ArrayList<Vertex>();
		while (it.hasNext()){
			
			Edge out=it.next();
			succVertices.add(out.to);
			
		}
		return succVertices;
		
	}

	@Override
	public int inDegree(Vertex vertex) {
		
		return vertex.inEdges.size();
	}

	@Override
	public boolean isDest(Vertex vertex, Edge edge) {
		
		return getDest(edge).equals(vertex);
	
	}

	@Override
	public boolean isPredecessor(Vertex v1, Vertex v2) {
		
		return getPredecessors(v1).contains(v2);
	}

	@Override
	public boolean isSource(Vertex vertex, Edge edge) {
	
		return getSource(edge).equals(vertex);
	}

	@Override
	public boolean isSuccessor(Vertex v1, Vertex v2) {
		

		return getSuccessors(v1).contains(v2);
	}

	@Override
	public int outDegree(Vertex vertex) {
		

		return vertex.outEdges.size();
	}

	@Override
	public boolean addEdge(Edge arg0, Collection<? extends Vertex> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addEdge(Edge arg0, Collection<? extends Vertex> arg1,
			EdgeType arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addVertex(Vertex vertex) {
		
		for (Vertex v:vertices){
			if (v.equals(vertex))
				return false;
		}
		vertices.add(vertex);
		return true;
		
	}

	@Override
	public boolean containsEdge(Edge edge) {
		
		return getEdges().contains(edge);
	}

	@Override
	public boolean containsVertex(Vertex vertex) {
		

		return getVertices().contains(vertex);
	}

	@Override
	public int degree(Vertex vertex) {
		return getIncidentEdges(vertex).size();
	}

	@Override
	public Edge findEdge(Vertex v1, Vertex v2) {
		for (Edge e:edges){
			if (v1.equals(e.getSource()) && v2.equals(e.getDest())){
				return e;
			}
		}
		return null;
	}

	@Override
	public Collection<Edge> findEdgeSet(Vertex v1, Vertex v2) {
		
		for (int i=0;i<edges.size();i++){
			if (v1.equals(edges.get(i).getSource()) && v2.equals(edges.get(i).getDest()))
				return (Collection<Edge>) edges.get(i);
		}
		return null;
		
	}

	@Override
	public EdgeType getDefaultEdgeType() {
	
		return EdgeType.DIRECTED;
	}

	@Override
	public int getEdgeCount() {
		

		return edges.size();
	}

	@Override
	public int getEdgeCount(EdgeType edgeType) {
		return edges.size();
	}

	@Override
	public EdgeType getEdgeType(Edge edge) {
		
		return EdgeType.DIRECTED;
	}

	@Override
	public Collection<Edge> getEdges() {

		return edges;
	}

	@Override
	public Collection<Edge> getEdges(EdgeType edgeType) {
		
		return edges;
	}

	@Override
	public int getIncidentCount(Edge edge) {
		return getIncidentVertices(edge).size();
	}

	@Override
	public Collection<Edge> getIncidentEdges(Vertex vertex) {
		
		HashSet<Edge> out=vertex.outEdges;
		HashSet<Edge> in=vertex.inEdges;
		
		HashSet<Edge> incEdges=new HashSet<Edge>();
		Iterator<Edge> outIt=vertex.outEdges.iterator();
		while(outIt.hasNext()){
			incEdges.add(outIt.next());
		}
		
		Iterator<Edge> inIt=vertex.inEdges.iterator();
		while(inIt.hasNext()){
			incEdges.add(inIt.next());
		}
		
		return incEdges;
	}

	@Override
	public Collection<Vertex> getIncidentVertices(Edge edge) {
		
		HashSet<Vertex> incVertices=new HashSet<Vertex>();
		incVertices.add(edge.from);
		incVertices.add(edge.to);
		return incVertices;
	}

	@Override
	public int getNeighborCount(Vertex vertex) {
		return getNeighbors(vertex).size();
	}

	@Override
	public Collection<Vertex> getNeighbors(Vertex vertex) {
		HashSet<Edge> out=vertex.outEdges;
		HashSet<Edge> in=vertex.inEdges;
		HashSet<Vertex> neighbors=new HashSet<Vertex>();
		Iterator<Edge> outIt=vertex.outEdges.iterator();
		
		while(outIt.hasNext()){
			
			Edge e=outIt.next();
			neighbors.add(e.to);
		}
		
		Iterator<Edge> inIt=vertex.inEdges.iterator();
		
		while(inIt.hasNext()){
			
			Edge e=inIt.next();
			neighbors.add(e.from);
		}
		return neighbors;
	}

	@Override
	public int getVertexCount() {
	
		return vertices.size();
	}

	@Override
	public Collection<Vertex> getVertices() {
		

		return vertices;
	}

	@Override
	public boolean isIncident(Vertex vertex, Edge edge) {
	
		return getIncidentEdges(vertex).contains(edge);
	}

	@Override
	public boolean isNeighbor(Vertex v1, Vertex v2) {
		return getNeighbors(v1).contains(v2);
	}

	@Override
	public boolean removeEdge(Edge arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeVertex(Vertex arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
