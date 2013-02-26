package mutandis.graph;


public class Edge {
	
	private double weight=0.0;
	public Vertex from;
    public Vertex to;
    public Edge(Vertex from, Vertex to) {
      this.from = from;
      this.to = to;
    }
    
    public Edge(Vertex from, Vertex to, double weight) {
   
    	this.from = from;
        this.to = to;
        this.weight=weight;
      }
    @Override
    public boolean equals(Object obj) {
      Edge e = (Edge)obj;
      return from.name.equals(e.from.name) && to.name.equals(e.to.name);
    }

	public Vertex getSource() {
		return this.from;
	}
	
	public Vertex getDest() {
		return this.to;
	}
	public void setWeight(double weight){
		this.weight=weight;
	}
	
	public double getWeight(){
		return weight;
	}

}
