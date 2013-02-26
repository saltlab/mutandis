package mutandis.systemProperties;

import java.util.Random;

public class RandomGenerator {
	
	private static Random random;
	
	public RandomGenerator(long seed){
		random=new Random(SystemProps.seed);
	}
	public int getNextRandomInt(int max){
		return (random.nextInt(max));
	}
	public double getNextDouble(){
		return random.nextDouble();
	}

}
