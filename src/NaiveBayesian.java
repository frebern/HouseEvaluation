import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm{
	private HashMap<String, Double> c = new HashMap<String, Double>();	// class������ ����
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// �� class�� ���� Ȯ��
	private ArrayList<String[]> trains;
	private ArrayList<String[]> tests;
	HashMap<String, Double> avgBySection;
	private double total=0.0;	// �и�
	private HashMap<Integer, String> predictions = new HashMap<Integer, String>(); // <Integer, Double> -> <Integer, String>���� �ٲ�
	
	private static NaiveBayesian instance;

	private NaiveBayesian() {

	}

	public static NaiveBayesian getInstance() {
		if (instance == null)
			instance = new NaiveBayesian();
		return instance;
	}
	
	
	// ���ڸ� ���ϴ� �Լ�
	private Double getNum(ArrayList<String[]> data, String find, int index, String c) {
		double n=0.0;
		for(String[] line:data)
			if ((line[line.length-1].equals(c))&&(line[index].equals(find))) n++;
		
		return n;
	}

	@Override
	public void readData(ArrayList<String[]> trains, ArrayList<String[]> tests,
			HashMap<String, Double> avgBySection) {
		this.trains = trains;
		this.tests = tests;
		this.avgBySection = avgBySection;
		
	}
	@Override
	public void runAlgorithm(boolean isCat) {
		
		for(String[] arr:trains){
			if(c.containsKey(arr[arr.length-1])) c.put(arr[arr.length-1], (c.get(arr[arr.length-1])+1));
			else c.put(arr[arr.length-1], 1.0);
			total++;
		}

		tests.parallelStream().forEach(test->{
			
			HashMap<String, Double> probs = new HashMap<String, Double>();
			double sumOfProbs = 0.0;
			double prediction = 0.0;
			
			for(String key:c.keySet()){
				double prob = 1.0;
				// �켱 ���� ������ v�� �־���. i=0�� id�̹Ƿ� ��������
				for(int i=1;i<test.length;i++)
					prob *= (getNum(trains, test[i], i, key)+1) / (c.get(key)+1);
				
				prob *= (c.get(key)+1) / (total+1);
				probs.put(key, prob);
				sumOfProbs += prob;
			}
			
			if(isCat){
				String maxKey="";
				double maxProb=0.0;
				for(String key:probs.keySet()){
					if(probs.get(key) > maxProb){
						maxProb = probs.get(key);
						maxKey = key;
					}
				}
				predictions.put(Integer.valueOf(test[0]), maxKey);
			}
			else{
				for(String key:probs.keySet())
					prediction += avgBySection.get(key)*probs.get(key)/sumOfProbs;
			
				predictions.put(Integer.valueOf(test[0]), String.valueOf(prediction));
			}
		});
	}
	
	@Override
	public HashMap<Integer, String> getPredictions() {
		return predictions;
	}
	
}
