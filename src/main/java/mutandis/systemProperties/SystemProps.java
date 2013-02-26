package mutandis.systemProperties;


public class SystemProps {
	
	
	public static long seed=100;
	public static long funcSelecSeed=1;
	public static long varSelecSeed=10;
	public static long oprSelecSeed=20;
	public static RandomGenerator rnd=new RandomGenerator(seed);
	public static RandomGenerator funcSelecRnd=new RandomGenerator(funcSelecSeed);
	public static RandomGenerator varSelecRnd=new RandomGenerator(varSelecSeed);
	public static RandomGenerator oprSelecRnd=new RandomGenerator(oprSelecSeed);
//	public static double threshold=0.5;

}
