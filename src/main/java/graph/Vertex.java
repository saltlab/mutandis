package graph;

import java.util.HashSet;
import java.util.Iterator;


public class Vertex {
	
    public String name;
    public HashSet<Edge> inEdges;
    public HashSet<Edge> outEdges;
    public Vertex(String name) {
      this.name = name;
      inEdges = new HashSet<Edge>();
      outEdges = new HashSet<Edge>();
    }
    public Vertex addEdge(Vertex node){
      Edge e = new Edge(this, node);
      outEdges.add(e);
      node.inEdges.add(e);
      return this;
    }
    @Override
    public String toString() {
      return name;
    }
    
    @Override
    public boolean equals(Object obj) {
      Vertex v = (Vertex)obj;
      return name.equals(v.name);
    }
    public void addInEdges(Edge e){
    	Iterator<Edge> it=inEdges.iterator();
    	while(it.hasNext()){
    		Edge edge=(Edge) it.next();
    		if(e.equals(edge))
    			return;
    		
    	}
    	
    	inEdges.add(e);
    	
    }
    
    public void addOutEdges(Edge e){
    	Iterator<Edge> it=outEdges.iterator();
    	while(it.hasNext()){
    		Edge edge=(Edge) it.next();
    		if(e.equals(edge))
    			return;
    		
    	}
    	outEdges.add(e);
    }
    
	public double getSumWeightOutEdges(){
		
		HashSet<Edge> outedges=this.outEdges;
		Iterator<Edge> it=outedges.iterator();
		double sum=0.0;
		while(it.hasNext()){
			Edge edge=it.next();
			sum+=edge.getWeight();
		}
		return sum;
	}

}
