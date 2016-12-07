import java.util.HashMap;
import java.util.ArrayList;

public class NaiveBayesian implements Algorithm{
	private HashMap<String, Double> classCounts = new HashMap<String, Double>();	// SalesPrice�� ���� Class c�� ����
	//private HashMap<String, Double> prob_c = new HashMap<String, Double>();	// �� c�� ���� Ȯ��
	private ArrayList<String[]> trains;
	private ArrayList<String[]> tests;
	HashMap<String, Double> avgBySection;
	private double total=0.0;	// �и�
	private HashMap<Integer, Double> predictions = new HashMap<Integer, Double>();
	
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
	public void runAlgorithm() {
		
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
		tests.parallelStream().forEach(test->{
			
			HashMap<String, Double> probs = new HashMap<String, Double>();
			double sumOfProbs = 0.0;
			double prediction = 0.0;
			HashMap<String,ArrayList<Double>> tmps = new HashMap<>();
			for(String key:classCounts.keySet()){
				double prob = 1.0;
				double classCount = classCounts.get(key);
				double smoothing = (classCount+1)/(total+1);
				//�̺κ� +1�� ���������� +(�и�/��ü)�� ������ ������ ���� ����� Ȯ���� ���� �˵�. ������ �Ʒ��� ���ڵ� (�и�/��ü)�� �������ϹǷ� �Ƹ� ���ڰ� ������.
				classCount+=1;
				
				// �켱 ���� ������ v�� �־���. i=0�� id�̹Ƿ� ��������, i=test.length-1�� ��쿡�� ���� test�� Ŭ�������� �� �����Ƿ� ��ŵ.
				ArrayList<Double> tmp = new ArrayList<>();
				for(int i=1;i<test.length-1;i++){
					//���⼭ classCount�� �������� Ȯ���� �����ϰ� ����.(�뷫 prob /= (classCount^79) �̱� ����) 
					//���� ������ ���� H�� J(�Ĺݺ� ī�װ�)�� ���������� ���̷� �����ϰ� ��. �� ������ ���� ������ ����.
					//�׷��� ���ö� �������� �ſ� ���� ��(������ classCount�� ������ ������ �ްԲ�)���� �ٲ���.
					prob *= ((getNum(trains, test[i], i, key)+smoothing) / classCount);	
				}
//				System.out.println();
				//������ �𸣰�����, ������ ī�װ��� ���ؼ� Ȯ������ ���� �ſ� ũ�� ����.
				tmp.add(prob);
				tmp.add(smoothing);
				tmps.put(key,tmp);
				prob *= (smoothing);

//				System.out.println();
				
				probs.put(key, prob);
				sumOfProbs += prob;
			}
			
			for(String key:probs.keySet())
				prediction += avgBySection.get(key)*probs.get(key)/sumOfProbs;
			
			predictions.put(Integer.valueOf(test[0]), prediction);
			
		});
	}
	
	@Override
	public HashMap<Integer, Double> getPredictions() {
		return predictions;
	}
	
}
