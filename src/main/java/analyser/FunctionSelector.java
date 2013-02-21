package analyser;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import systemProperties.RandomGenerator;
import systemProperties.SystemProps;



public class FunctionSelector {
	
	private FunctionRankCalculator functionRankCalculator;
	private JSCyclCompxCalc cyclCompxCalc;
	private HashMap<String,Double> sortedProbMap;


	public FunctionSelector(String outputdir){
	
		functionRankCalculator=new FunctionRankCalculator(outputdir);
		cyclCompxCalc=new JSCyclCompxCalc(outputdir);
		sortedProbMap=new LinkedHashMap<String,Double>();
		sortedProbMap=convertToSortedProbMap
			((HashMap<String, Double>) functionRankCalculator.getfunctionProbabilities().clone());
		
	}
	
	private HashMap<String,Double> convertToSortedProbMap(HashMap<String,Double> map){
	//	HashMap<String,Double> functionProbs=new HashMap<String, Double>();
	//	functionProbs=(HashMap<String, Double>) functionRankCalculator.getfunctionProbabilities().clone();		
		HashMap<String,Double> sortedMap=new LinkedHashMap<String, Double>();
		ValueComparator bvc =  new ValueComparator(map);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>((Comparator<? super String>) bvc);
       
        sorted_map.putAll(map);
        
        sortedMap.putAll(sorted_map);
        return sortedMap;
     
 
    }
	
	private HashMap<String,Double> getSortedProbSumMap(HashMap<String,Double> map){
		LinkedHashMap<String,Double> sortedMap=new LinkedHashMap<String,Double>();
		sortedMap.putAll(convertToSortedProbMap(map));
        LinkedHashMap<String,Double> sortedMapProb=new LinkedHashMap<String,Double>();
        sortedMapProb.putAll((Map<String,Double>) sortedMap.clone());
        Set<String> keySet=sortedMap.keySet();
        Iterator<String> it=keySet.iterator();
        String funcName;
        double sumProb=0;
        while(it.hasNext()){
        	
        	funcName=it.next();
        //	sumProb+=sorted_map.get(funcName);
        	sumProb+=((double)(Math.round(sortedMap.get(funcName)*10000))/10000);
        	sortedMapProb.put(funcName, sumProb);
        	
        }
        
        return sortedMapProb;
 
    }
	/**
	 * Get selected function name and path chosen based on its probability. This method
	 * returns selected function based on just function rank probabilities.
	 * @return
	 */
	public String getSelectedFunctionNameandPath(){
		HashMap<String,Double> sortedProbMap=new LinkedHashMap<String,Double>();
		sortedProbMap=getSortedProbSumMap(this.sortedProbMap);
		Set<String> keySet=sortedProbMap.keySet();
		Iterator<String> it=keySet.iterator();
		double prob=0.0;
		String selectedfunc="";
		double rand=SystemProps.funcSelecRnd.getNextDouble();
		if (rand==0)
			return it.next();
		while(it.hasNext()){
			
			String funcname=it.next();
			if(prob<rand && rand<=sortedProbMap.get(funcname)){
				selectedfunc=funcname;
				break;
				
			}
			
			prob=sortedProbMap.get(funcname);
					
		}
		return selectedfunc;
		
	}
	
	private HashMap<String,Double> getNormalizedProbs(HashMap<String,Double> map){
		Set<String> keyset=map.keySet();
		Iterator<String> it=keyset.iterator();
		double sum=0.0;
		while(it.hasNext()){
			String funcname=(String) it.next();
			sum+=map.get(funcname);
		}
		it=keyset.iterator();
		double normalized=0.0;
		while(it.hasNext()){
			String funcname=(String) it.next();
			normalized=(double)map.get(funcname)/sum;
			map.put(funcname, normalized);
		}
		return map;
	}
	
	/**
	 * get selected function based on both rank probability and the cyclomatic complexity rate
	 */
	
	public String getCycloRankSelectedfunction(){
		
		HashMap<String,Double> sortedProbMap=new LinkedHashMap<String,Double>();
		Set<String> keySet=this.sortedProbMap.keySet();
		Iterator<String> it=keySet.iterator();
		HashMap<String,Double> cycloRateMap=cyclCompxCalc.getCyclCompxRate();
		while(it.hasNext()){
			double cycloRankProb=0.0;
			String funcname=it.next();
			if(cycloRateMap.containsKey(funcname)){
				cycloRankProb=this.sortedProbMap.get(funcname)*
									cycloRateMap.get(funcname);
				cycloRankProb=((double)(Math.round(cycloRankProb*10000))/10000);
			}
			sortedProbMap.put(funcname, cycloRankProb);
		}
		sortedProbMap=getNormalizedProbs(sortedProbMap);
		sortedProbMap=getSortedProbSumMap(sortedProbMap);
		keySet=sortedProbMap.keySet();
		it=keySet.iterator();
		double prob=0.0;
		String selectedfunc="";
		double rand=SystemProps.funcSelecRnd.getNextDouble();
		if (rand==0)
			return it.next();
		while(it.hasNext()){
			
			String funcname=it.next();
			if(prob<rand && rand<=sortedProbMap.get(funcname)){
				selectedfunc=funcname;
				break;
				
			}
			
			prob=sortedProbMap.get(funcname);
					
		}
		return selectedfunc;
		
	}
	
	
	

}
class ValueComparator implements Comparator<String>{

    HashMap<String, Double> base;
    public ValueComparator(HashMap<String, Double> base) {
        this.base = base;
    }

    public int compare(String a, String b) {
        int cmp=base.get(a).compareTo(base.get(b));
        if(cmp==0)
        	return a.compareTo(b);
        return base.get(a).compareTo(base.get(b));
    }
				
		
}


