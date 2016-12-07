import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm{
	private HashMap<String, Double> classCounts = new HashMap<String, Double>();	// SalesPrice의 각각 Class c의 개수
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// 각 c에 대한 확률
	private ArrayList<String[]> trains;
	private String[] test;
	private double total=0.0;	// 분모
	private HashMap<String,Double> probabilities = new HashMap<>(); 
	
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
	public void readData(ArrayList<String[]> trains, String[] test) {
		this.trains = trains;
		this.test = test;
	}
	@Override
	public void runAlgorithm() {
		
		probabilities.clear();
		classCounts.clear();
		total = 0.0;
		
		for(String[] arr:trains){
			if(classCounts.containsKey(arr[arr.length-1])) classCounts.put(arr[arr.length-1], (classCounts.get(arr[arr.length-1])+1));
			else classCounts.put(arr[arr.length-1], 1.0);
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
		HashMap<String, Double> probs = new HashMap<String, Double>();
		double sumOfProbs = 0.0;
//		System.out.println("values:"+classCounts.values());
		for(String key:classCounts.keySet()){
			double prob = 1.0;
			double classCount = classCounts.get(key);
			double smoothing = (classCount+1)/(total+1);
			//이부분 +1로 스무딩할지 +(분모/전체)로 스무딩 할지는 직접 결과를 확인해 봐야 알듯.(+1이 좀 더 좋은 결과를 줬었음)
			classCount+=smoothing;

			// 우선 곱할 수들을 v에 넣어줌. i=0은 id이므로 하지않음, i=test.length-1의 경우에는 현재 test에 클래스까지 들어가 있으므로 스킵.
			for(int i=1;i<test.length-1;i++){
				//여기서 classCount가 작은놈이 확률이 유리하게 나옴.(대략 prob /= (classCount^79) 이기 때문) 
				//따라서 갯수가 적은 H나 J(후반부 카테고리)가 무지막지한 차이로 유리하게 됨. 이 구조는 뭔가 문제가 있음.
				//그래서 라플라스 스무딩을 매우 작은 값(하지만 classCount의 갯수에 영향을 받게끔)으로 바꿔줌.
				prob *= ((getNum(trains, test[i], i, key)+smoothing) / classCount);	
			}

//				System.out.println();
			//이유는 모르겠지만, 마지막 카테고리에 대해서 확률들의 곱이 매우 크게 나옴.
			prob *= (smoothing);

			probs.put(key, prob);
			sumOfProbs += prob;
			
		}
//		System.out.println(probs);
		
		for(String key:probs.keySet())
			probabilities.put(key,probs.get(key)/sumOfProbs);
			
	}
	
	@Override
	public HashMap<String, Double> getProbabilities() {
		return probabilities;
	}
	
}
