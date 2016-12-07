import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm{
<<<<<<< HEAD
	private HashMap<String, Double> classCounts = new HashMap<String, Double>();	// SalesPrice�� ���� Class c�� ����
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// �� c�� ���� Ȯ��
=======
	private HashMap<String, Double> c = new HashMap<String, Double>();	// class������ ����
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// �� class�� ���� Ȯ��
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
	private ArrayList<String[]> trains;
	private String[] test;
	private double total=0.0;	// �и�
<<<<<<< HEAD
	private HashMap<String,Double> probabilities = new HashMap<>(); 
=======
	private HashMap<Integer, String> predictions = new HashMap<Integer, String>(); // <Integer, Double> -> <Integer, String>���� �ٲ�
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
	
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
			//�̺κ� +1�� ���������� +(�и�/��ü)�� ������ ������ ���� ����� Ȯ���� ���� �˵�.(+1�� �� �� ���� ����� �����)
			classCount+=smoothing;

			// �켱 ���� ������ v�� �־���. i=0�� id�̹Ƿ� ��������, i=test.length-1�� ��쿡�� ���� test�� Ŭ�������� �� �����Ƿ� ��ŵ.
			for(int i=1;i<test.length-1;i++){
				//���⼭ classCount�� �������� Ȯ���� �����ϰ� ����.(�뷫 prob /= (classCount^79) �̱� ����) 
				//���� ������ ���� H�� J(�Ĺݺ� ī�װ�)�� ���������� ���̷� �����ϰ� ��. �� ������ ���� ������ ����.
				//�׷��� ���ö� �������� �ſ� ���� ��(������ classCount�� ������ ������ �ްԲ�)���� �ٲ���.
				prob *= ((getNum(trains, test[i], i, key)+smoothing) / classCount);	
=======

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
>>>>>>> 714c97d35d4002090e4604ba75022593cabc3e14
			}

//				System.out.println();
			//������ �𸣰�����, ������ ī�װ��� ���ؼ� Ȯ������ ���� �ſ� ũ�� ����.
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
