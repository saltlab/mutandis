package mutandis.analyser;


import java.util.ArrayList;
import java.util.HashMap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import mutandis.graph.Edge;
import mutandis.graph.Vertex;
import mutandis.graph.WeightedGraph;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.scoring.PageRank;


public class FunctionRankCalculator {
	
	private WeightedGraph functionCallGraph;
	private HashMap<String, Double> functionRankMap;
	private HashMap<String, Double> functionProbabilities;
	public static final double alpha=0; 

	public FunctionRankCalculator(String outputFolder, String jsFilesFolder){
		
		functionRankMap=new HashMap<String, Double>();
		functionProbabilities=new HashMap<String, Double>();
		functionCallGraph=new WeightedGraph();
		FunctionCallTraceAnalyser functionAnalyser=new FunctionCallTraceAnalyser(outputFolder, jsFilesFolder);
		functionAnalyser.startAnalysingTraceFiles();
		TreeMap<String, ArrayList<ArrayList<Object>>> funcMap=functionAnalyser.getFunctionCallMap();
		Set<String> keySet=funcMap.keySet();
		Iterator<String> it=keySet.iterator();
	
		while(it.hasNext()){
			
			String funcName=it.next();
	//		String functionScope=funcName.split("::")[0];
	//		funcName=funcName.split("::")[1];
			Vertex callerVertex=new Vertex(funcName);
			ArrayList<ArrayList<Object>> calleeList=funcMap.get(funcName);
			boolean containRecursiveFunc=false;
			for(ArrayList<Object> callee:calleeList){
				
	
				String calleeName=(String) callee.get(0);
				if(calleeName.contains("_recursive_copy")){
					containRecursiveFunc=true;
					double weight=(Double) callee.get(1);
					Vertex copy=new Vertex(calleeName);
					functionCallGraph.addVertex(copy);
					Edge callerCopy=new Edge(callerVertex, copy, weight);		
					functionCallGraph.addEdge(callerCopy, callerVertex, copy);
					calleeList.remove(callee);
					for(int i=0;i<calleeList.size();i++){
						calleeName=(String) calleeList.get(i).get(0);
						weight=(Double) calleeList.get(i).get(1);
						Vertex calleeVertex=new Vertex(calleeName);
						functionCallGraph.addVertex(calleeVertex);
						Edge copyCallee=new Edge(copy, calleeVertex, weight);		
						functionCallGraph.addEdge(copyCallee, copy, calleeVertex);
					}
					
					break;
				}
			}
			if(!containRecursiveFunc){
				for(ArrayList<Object> callee:calleeList){
					
			//		String calleeName=functionScope+"::"+(String) callee.get(0);
					String calleeName=(String) callee.get(0);
					double weight=(Double) callee.get(1);
					Vertex calleeVertex=new Vertex(calleeName);
					functionCallGraph.addVertex(calleeVertex);
					if(funcName.equals(calleeName)){ 
						continue;
					}
					
					Edge callerCallee=new Edge(callerVertex, calleeVertex, weight);
				
						
					functionCallGraph.addEdge(callerCallee, callerVertex, calleeVertex);
					
				}
			}
			
		}
		
		ArrayList<Vertex> vertices=(ArrayList<Vertex>) functionCallGraph.getVertices();
		Vertex fakeVertex=new Vertex("fakeVertex");
		for(int i=0;i<vertices.size() && !vertices.get(i).name.equals("fakeVertex");i++){
			if(vertices.get(i).outEdges.size()!=0){
				if(Math.abs(vertices.get(i).getSumWeightOutEdges()-1)>0){
					if(!functionCallGraph.containsVertex(fakeVertex)){
						functionCallGraph.addVertex(fakeVertex);
					}
					double res=1-vertices.get(i).getSumWeightOutEdges();
					Edge fakeEdge=new Edge(vertices.get(i), fakeVertex, res);
					functionCallGraph.addEdge(fakeEdge, vertices.get(i), fakeVertex);
					
				/*	double resToadd=(double)res/vertices.get(i).outEdges.size();
					Iterator<Edge> iter=vertices.get(i).outEdges.iterator();
					while(iter.hasNext()){
						Edge edge=iter.next();
						double currWeght=edge.getWeight();
						edge.setWeight(currWeght-resToadd);
					}
				*/	
				}
			}
			
		}
		

		
	
		setFunctionRankMap();
	
	}
	
	private void setFunctionRankMap(){
		
		
		Transformer<Edge, Double> tr=new Transformer<Edge, Double>() {
			
			@Override
			public Double transform(Edge edge) {
				// TODO Auto-generated method stub
				return edge.getWeight();
			}
		};; ;
		
		PageRank<Vertex,Edge> functionRank=
					new PageRank<Vertex, Edge>(functionCallGraph, tr, alpha);
		
		functionRank.evaluate();
		ArrayList<Vertex> vertices=(ArrayList<Vertex>) functionCallGraph.getVertices();
	
	//	List<Double> scList=new ArrayList<Double>();
	//	double total=0.0;
		for(Vertex vertex:vertices){
			if(!"".equals(vertex.name) && !"fakeVertex".equals(vertex.name)){
				double score=functionRank.getVertexScore(vertex);
	/*			double roundedScore=((double)(Math.round(score*10000))/10000);
				total+=roundedScore;
	*/			functionRankMap.put(vertex.name, score);

			}
		}

		double totalScore=getTotalScores();
		Set<String> keySet=functionRankMap.keySet();
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			String funcName=it.next();
			double score=functionRankMap.get(funcName);
			functionProbabilities.put(funcName, (double)score/totalScore);
			
		}
	
		
		
		
	//	HashMap<String,Double> prob=getfunctionProbabilities();
	//	Set<String> keySets=prob.keySet();

		
	}
	
	public HashMap<String, Double> getFunctionRankMap(){
		
		return functionRankMap;
	}
	
	public WeightedGraph getFunctionCallGraph(){
		return functionCallGraph;
	}
	
	public HashMap<String,Double> getfunctionProbabilities(){
		
		return functionProbabilities;
	}
	
	public double getTotalScores(){
		Set<String> keySet=functionRankMap.keySet();
		double totalScore=0;
		Iterator<String> it=keySet.iterator();
		while(it.hasNext()){
			String funcName=it.next();
			totalScore+=functionRankMap.get(funcName);
			
		}
		return totalScore;
		
	}
	


}
