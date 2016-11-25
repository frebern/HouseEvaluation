import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm {

	/* For Singleton */
	private static NaiveBayesian instance;

	private NaiveBayesian() {}

	public static NaiveBayesian getInstance() {
		if (instance == null)
			instance = new NaiveBayesian();
		return instance;
	}
	
	
	// 데이터들
	private ArrayList<String[]> trains;
	private ArrayList<String[]> tests;
	private HashMap<String, Double> avgBySection;
	
	// SalesPrice의 각각 Class c의 개수
	private HashMap<String, Double> classes = new HashMap<String, Double>();
	private double total; // 분모

	// 예측 결과
	private HashMap<Integer, Double> predictions = new HashMap<Integer, Double>();

	
	/* HouseEvaluation으로부터 trains, tests, avgBySection 받는 부분. */
	@Override
	public void readData(ArrayList<String[]> trains, ArrayList<String[]> tests,
			HashMap<String, Double> avgBySection) {
		this.trains = trains;
		this.tests = tests;
		this.avgBySection = avgBySection;
	}

	
	/* 예상 SalePrices를 리턴하는 부분 */
	@Override
	public HashMap<Integer, Double> getPredictions() {
		return predictions;
	}

	
	/* 실제 알고리즘이 돌아가는 파트 */
	@Override
	public void runAlgorithm() {

		trains.parallelStream()
			  .map(arr -> arr[arr.length - 1])
			  .forEach(arr -> classes.put(arr,classes.getOrDefault(arr, 0.0) + 1.0));
		total = trains.size();

		tests.parallelStream()
			 .forEach(test -> {
				double prob; // SalesPrice의 Class c에 대한 확률
				HashMap<String, Double> probs = new HashMap<String, Double>();
				double probSum = 0.0;
				double prediction = 0.0;
				for (String key : classes.keySet()) {
					prob = 1.0;
					// 우선 곱할 수들을 v에 넣어줌. i=0은 id이므로 하지않음
					for (int i = 1; i < test.length; i++)
						prob *= ((getNum(trains, test[i], i, key) + 1) / (classes.get(key) + 1));
					 
					prob *= ((classes.get(key) + 1) / (total + 1));
					probs.put(key, prob);
					probSum += prob;
				}
				 
				final double tmpProbSum = probSum;
				prediction = probs.keySet().stream()
						 				   .mapToDouble(key -> avgBySection.get(key) * probs.get(key) / tmpProbSum)
						 				   .sum();
				 
				predictions.put(Integer.parseInt(test[0]), prediction);
//				System.out.println("Id: " + Integer.valueOf(test[0])+"\tprediction: " + prediction);
				 
			 });
	}

	
	/* 분자를 구하는 메서드 */
	private double getNum(ArrayList<String[]> data, String find, int index, String c) {
		return data.stream()
				   .filter(line -> (line[line.length - 1].equals(c))&& (line[index].equals(find)))
				   .count();
	}

}
