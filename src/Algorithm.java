import java.util.ArrayList;
import java.util.HashMap;

public interface Algorithm {

	public void readData(ArrayList<String[]> trains, ArrayList<String[]> tests, HashMap<String,Double> avgBySection);
	public void runAlgorithm(boolean isCat);
	public HashMap<Integer, String> getPredictions();
	
}
