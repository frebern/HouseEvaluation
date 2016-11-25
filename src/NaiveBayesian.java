import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm{
	private HashMap<String, Double> c = new HashMap<String, Double>();	// SalesPrice의 각각 Class c의 개수
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// 각 c에 대한 확률
	private ArrayList<String[]> trains;
	private ArrayList<String[]> tests;
	HashMap<String, Double> avgBySection;
	private double total=0.0;	// 분모
	private HashMap<Integer, Double> predictions = new HashMap<Integer, Double>();
	
	private static NaiveBayesian instance;

	private NaiveBayesian() {

	}

	public static NaiveBayesian getInstance() {
		if (instance == null)
			instance = new NaiveBayesian();
		return instance;
	}
	
	
	// 분자를 구하는 함수
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
	public void runAlgorithm() {
		
		for(String[] arr:trains){
			if(c.containsKey(arr[arr.length-1])) c.put(arr[arr.length-1], (c.get(arr[arr.length-1])+1));
			else c.put(arr[arr.length-1], 1.0);
			total++;
		}
		//for(String s:c.keySet()) System.out.println(s+"="+c.get(s));
		//System.out.println(total);
		/*
		String[] test = tests.get(1107);
		System.out.println(test[0]);
		for(String key:c.keySet()){
			System.out.println(get_num(trains, test[1], 1, key));
		}
		*/
		
		for(String[] test:tests){
			int i;
			double prob;	// SalesPrice의 Class c에 대한 확률
			HashMap<String, Double> probs = new HashMap<String, Double>();
			double sum_of_probs = 0.0;
			double prediction = 0.0;
			//System.out.print("ID: "+test[0]+" prob of ");
			for(String key:c.keySet()){
				prob = 1.0;
				// 우선 곱할 수들을 v에 넣어줌. i=0은 id이므로 하지않음
				for(i=1;i<test.length;i++) {
					//System.out.println("분자: "+(get_num(trains, test[i], i, key)+1)+" 분모: "+(c.get(key)+1));
					prob *= (getNum(trains, test[i], i, key)+1) / (c.get(key)+1);
				}
				prob *= (c.get(key)+1) / (total+1);
				probs.put(key, prob);
				sum_of_probs += prob;
				//System.out.print(key+"="+prob+"\t");
			}
			
			for(String key:probs.keySet())
				prediction += avgBySection.get(key)*probs.get(key)/sum_of_probs;
			
			predictions.put(Integer.valueOf(test[0]), prediction);
//			System.out.println("Id: "+Integer.valueOf(test[0])+"\tprediction: "+prediction);
			/*
			double temp=0.0;
			for(String key:probs.keySet()){
				temp+=probs.get(key)/sum_of_probs;
				System.out.println(key+": "+probs.get(key)/sum_of_probs);
			}
			System.out.print("sum_of_prob="+temp);
			System.out.println("\n");
			*/
		}
	}
	
	@Override
	public HashMap<Integer, Double> getPredictions() {
		return predictions;
	}
	
}
