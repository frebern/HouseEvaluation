import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm{
	private HashMap<String, Double> classCounts = new HashMap<String, Double>();	// SalesPrice�� ���� Class c�� ����
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// �� c�� ���� Ȯ��
	private ArrayList<String[]> trains;
	private String[] test;
	private double total=0.0;	// �и�
	private HashMap<String,Double> probabilities = new HashMap<>(); 
	
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
			//�̺κ� +1�� ���������� +(�и�/��ü)�� ������ ������ ���� ����� Ȯ���� ���� �˵�.(+1�� �� �� ���� ����� �����)
			classCount+=smoothing;

			// �켱 ���� ������ v�� �־���. i=0�� id�̹Ƿ� ��������, i=test.length-1�� ��쿡�� ���� test�� Ŭ�������� �� �����Ƿ� ��ŵ.
			for(int i=1;i<test.length-1;i++){
				//���⼭ classCount�� �������� Ȯ���� �����ϰ� ����.(�뷫 prob /= (classCount^79) �̱� ����) 
				//���� ������ ���� H�� J(�Ĺݺ� ī�װ�)�� ���������� ���̷� �����ϰ� ��. �� ������ ���� ������ ����.
				//�׷��� ���ö� �������� �ſ� ���� ��(������ classCount�� ������ ������ �ްԲ�)���� �ٲ���.
				prob *= ((getNum(trains, test[i], i, key)+smoothing) / classCount);	
			}

//				System.out.println();
			//������ �𸣰�����, ������ ī�װ��� ���ؼ� Ȯ������ ���� �ſ� ũ�� ����.
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
