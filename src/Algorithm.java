import java.util.ArrayList;
import java.util.HashMap;

public interface Algorithm {

<<<<<<< HEAD
	public void readData(ArrayList<String[]> trains, String[] test);
	public void runAlgorithm();
	public HashMap<String, Double> getProbabilities();
=======
	public void readData(ArrayList<String[]> trains, ArrayList<String[]> tests, HashMap<String,Double> avgBySection);
	public void runAlgorithm(boolean isCat);
	public HashMap<Integer, String> getPredictions();
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
	
}
