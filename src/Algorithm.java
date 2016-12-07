import java.util.ArrayList;
import java.util.HashMap;

public interface Algorithm {

	public void readData(ArrayList<String[]> trains, String[] test);
	public void runAlgorithm();
	public HashMap<String, Double> getProbabilities();
	
}
