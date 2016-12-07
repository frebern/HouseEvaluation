import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm{
<<<<<<< HEAD
	private HashMap<String, Double> classCounts = new HashMap<String, Double>();	// SalesPrice의 각각 Class c의 개수
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// 각 c에 대한 확률
=======
	private HashMap<String, Double> c = new HashMap<String, Double>();	// class종류별 개수
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// 각 class에 대한 확률
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
	private ArrayList<String[]> trains;
	private String[] test;
	private double total=0.0;	// 분모
<<<<<<< HEAD
	private HashMap<String,Double> probabilities = new HashMap<>(); 
=======
	private HashMap<Integer, String> predictions = new HashMap<Integer, String>(); // <Integer, Double> -> <Integer, String>으로 바뀜
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
	
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
		for(String[] line:data){
			//for(String s:line) System.out.print(s+", ");
			//System.out.println("");
			if ((line[line.length-1].equals(c))&&(line[index].equals(find))) n++;
<<<<<<< HEAD
=======
		}
		
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
		return n;
	}

	@Override
	public void readData(ArrayList<String[]> trains, String[] test) {
		this.trains = trains;
<<<<<<< HEAD
		this.test = test;
=======
		this.tests = tests;
		this.avgBySection = avgBySection;
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
	}
	@Override
	public void runAlgorithm(boolean isCat) {
		
		c.clear();
		predictions.clear();
		total=0.0;
		
		probabilities.clear();
		classCounts.clear();
		total = 0.0;
		
		for(String[] arr:trains){
			if(classCounts.containsKey(arr[arr.length-1])) classCounts.put(arr[arr.length-1], (classCounts.get(arr[arr.length-1])+1));
			else classCounts.put(arr[arr.length-1], 1.0);
			total++;
		}
<<<<<<< HEAD
		
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
=======

		tests.parallelStream().forEach(test->{
			
			HashMap<String, Double> probs = new HashMap<String, Double>();
			double sumOfProbs = 0.0;
			double prediction = 0.0;
			
			for(String key:c.keySet()){
				double prob = 1.0;
				// 우선 곱할 수들을 v에 넣어줌. i=0은 id이므로 하지않음
				for(int i=1;i<test.length;i++)
					prob *= (getNum(trains, test[i], i, key)+1) / (c.get(key)+1);
				
				prob *= (c.get(key)+1) / (total+1);
				probs.put(key, prob);
				sumOfProbs += prob;
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
			}

//				System.out.println();
			//이유는 모르겠지만, 마지막 카테고리에 대해서 확률들의 곱이 매우 크게 나옴.
			prob *= (smoothing);

			probs.put(key, prob);
			sumOfProbs += prob;
			
<<<<<<< HEAD
		}
//		System.out.println(probs);
		
		for(String key:probs.keySet())
			probabilities.put(key,probs.get(key)/sumOfProbs);
			
	}
	
	@Override
	public HashMap<String, Double> getProbabilities() {
		return probabilities;
=======
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
				for(String key:probs.keySet()){
					prediction += avgBySection.get(key)*probs.get(key)/sumOfProbs;
//					System.out.println("Key:"+key+",Prediction:"+prediction);
				}
				predictions.put(Integer.parseInt(test[0]), prediction+"");
				
			}
		});
	}
	
	@Override
	public HashMap<Integer, String> getPredictions() {
		return predictions;
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
	}
	
}
