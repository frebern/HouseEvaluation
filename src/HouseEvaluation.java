import java.util.ArrayList;
import java.util.HashMap;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class HouseEvaluation {
	
	/* trains�� tests�� csv������ record ���� ������ ���������� �����ֽ��ϴ�.
	����, �÷����� �ٲٱ� ���ؼ��� ������ ��ȯ�۾��� �ʿ��մϴ�. */
	private ArrayList<String[]> trains;
	private String[] fields;
	private ArrayList<String[]> tests;
	
	private ArrayList<Double> salePrices;
	private HashMap<String,Double> avgBySection;
	
	public HouseEvaluation(){
		
		//1. Ʈ���̴�/�׽�Ʈ/ī�װ� �����͸� �а� �����մϴ�. SalePrice�� ���� �������ϴ�.
		readData();
		saveSalePrices();
		
		attrDevide();
		
		//2. Numeric�� Category�� �ٲ��ݴϴ�. SalePrice�� ī�װ��� ����� �̸� ����� �����ϴ�.
		convertData();
		groupByAvg();
		
//		printData();
		
		long start = System.currentTimeMillis();
		
		//3. � �˰����̵� ������Բ� �������̽� ���.
		System.out.print("Now Running... ");
		Algorithm algorithm = NaiveBayesian.getInstance();
		//algorithm.readData(trains, tests, avgBySection);
		algorithm.runAlgorithm();
		System.out.println("Done!");
		
		//4. ����� ���Ϸ� ���ϴ�. output.svc�� �����ϴ�.
		//writeResult(algorithm.getProbabilities());
		System.out.println("Time Elapsed: "+(System.currentTimeMillis()-start)/1000.0+"sec");
		
		
	}
	
	// train�� ���ݾ� ������ ������ ���� Ȯ���� ���ϰ� recursive�ϰ� �����Ѵ�.
	private double recursion(ArrayList<String[]> trains_num, String[] test, double beforeProb){
		ArrayList<String[]> trains_upper = new ArrayList<>();
		ArrayList<String[]> trains_lower = new ArrayList<>();
		ArrayList<String[]> trains_cat = new ArrayList<>();
		double upperProb, lowerProb;
		divide(trains_num, trains_cat, trains_upper, trains_lower);
		
		Algorithm algorithm = NaiveBayesian.getInstance();
		//algorithm.readData(trains_cat, test);
		//algorithms.runAlgorithm();
		upperProb = algorithm.getProbabilities().get("upper");
		lowerProb = algorithm.getProbabilities().get("lower");
		
		if(stoppingCriteria(trains_upper, trains_lower, upperProb, lowerProb))
			return (average(trains_upper)*upperProb + average(trains_lower)*lowerProb) * beforeProb;
		else{
			ArrayList<String[]> maxSet = upperProb>lowerProb?trains_upper:trains_lower;
			ArrayList<String[]> minSet = upperProb<=lowerProb?trains_upper:trains_lower;
			double maxProb = Math.max(upperProb, lowerProb);
			double minProb = Math.min(upperProb, lowerProb);
			return recursion(maxSet, test, maxProb*beforeProb) + average(minSet)*minProb*beforeProb;
		}
		
	}
	
	// salesPrice�� ����� ���ϴ� �޼ҵ�
	private double average(ArrayList<String[]> trains) {
		double salesAverage;
		salesAverage=trains.stream().mapToDouble((item)->Double.parseDouble(item[item.length-1])).average().getAsDouble();
		
		return salesAverage;
	}

	// Recursion method�� �ߴ�����. �� Ȯ���� ������ ����Ȯ��(0.7)�� �Ѱų�, ���� trains�� size�� ���� ���� ������ �� true return.
	private boolean stoppingCriteria(ArrayList<String[]> trains_upper, ArrayList<String[]> trains_lower, double upperProb, double lowerProb){
		if(Math.max(upperProb, lowerProb)>0.7) return true;
		else if(Math.min(trains_upper.size(), trains_lower.size()) <= 1) return true;
		else return false;
	}
	
	// trains_num�� salesPrice�� sorting�ϰ� ���ݾ� trains_upper, trains_lower�� add�Ѵ�.
	private void divide(ArrayList<String[]> trains_num, ArrayList<String[]> trains_cat, 
			ArrayList<String[]> trains_upper, ArrayList<String[]> trains_lower){
		
		final int index = trains_num.get(0).length-1;
		
		trains_num.sort((t1,t2)->{
			double sp1 = Double.parseDouble(t1[index]);
			double sp2 = Double.parseDouble(t2[index]);
			return Double.compare(sp1,sp2);
		});
		int midIndex = trains_num.size()/2;
		double pivot = Double.parseDouble(trains_num.get(midIndex)[index]);
		//trains_lower
		trains_num.stream()
				  .filter(train->Double.parseDouble(train[index])<=pivot)
				  .forEach(train->trains_lower.add(train));
		
		//trains_upper
		trains_num.stream()
				  .filter(train->Double.parseDouble(train[index])>pivot)
				  .forEach(train->trains_upper.add(train));
		
		copyTo(trains_num,trains_cat);
		
		trains_cat.stream().forEach(train->{
			double value = Double.parseDouble(train[index]);
			train[index] = value<=pivot?"lower":"upper";
		});
		
	}
	
	//from���� to�� Deep Copy�մϴ�.
	private void copyTo(ArrayList<String[]> from, ArrayList<String[]> to) {
		to.clear();
		from.forEach(origin->{
			final int SIZE = origin.length;
			String[] newOne = new String[SIZE];
			for(int i=0;i<SIZE;i++)
				newOne[i] = new StringBuilder(origin[i]).append("").toString().trim();
			to.add(newOne);
		});
	}
	
	//���޸����� SalePrice�� ī�װ����ϰ� ��ȯ�ϱ� ���� �̸� �����������ϴ�.
	private ArrayList<Double> saveSalePrices(){
		salePrices = trains.parallelStream()
				   		   .map(train->Double.parseDouble(train[train.length-1]))
				   		   .collect(Collectors.toCollection(ArrayList::new));
		return salePrices;
	}
	
	//�������� SalePrice �׷��� ������ �� �׷캰 ����� ���մϴ�.
	private HashMap<String,Double> groupByAvg(){
		ArrayList<Predicate<Double>> ranges = DomainConvertor.getInstance().getDefinition("SalePrice").ranges;
		avgBySection = new HashMap<>();
		ranges.parallelStream()
			  .map(range->salePrices.parallelStream()
									.filter(range)
									.mapToDouble(s->(double)s)
									.average()
									.getAsDouble()
			  )
			  .forEach(avg->{
				  String key = DomainConvertor.getInstance().getCategory("SalePrice", avg+"");
				  avgBySection.put(key, avg);
			  });
		
		return avgBySection;
	}
	
	private void convertData() {
		DomainConvertor.getInstance().convert(fields, trains);
		DomainConvertor.getInstance().convert(fields, tests);
	}
	
	
	private void readData(){
		//Read Training Data
		trains = Reader.getInstance().getTrains();
		//Read Field Names
		fields = Reader.getInstance().getFields();
		//Read Test cases Data
		tests = Reader.getInstance().getTests();
	}
	
	private void printData(){
		printTrainData();
		System.out.println();
		printTestData();
	}

	private void printTrainData() {
		System.out.println("Training Datas");
		//Print Fields
		for(String field : fields)
			System.out.print(field+"\t");
		//Print Training Data
		trains.forEach(train->{
			System.out.println();
			for(int i=0;i<train.length;i++)
				System.out.print(train[i]+"\t");
		});
		System.out.println();
//		Print Fields(��°���� �ʹ� �� �ʵ�� �ؿ� �ѹ� �� ���)
		for(String field : fields)
			System.out.print(field+" ");
	}

	private void printTestData(){
		System.out.println("Test Cases");
		//Print Fields
		for(String field : fields)
			System.out.printf("%16s",field);
		//Print Test Data
		tests.forEach(test->{
			System.out.println();
			for(int i=0;i<test.length;i++)
				System.out.printf("%16s",test[i]);
		});
//		Print Fields(��°���� �ʹ� �� �ʵ�� �ؿ� �ѹ� �� ���)
		System.out.println();
		for(String field : fields)
			System.out.printf("%16s",field);
		System.out.println();
	}
	

	//result<ID, SalePrice>
	private void writeResult(HashMap<Integer, Double> result){
		System.out.print("Now Writing... ");
		final String filename = "output.csv";
		Writer.getInstance().write(result, filename);
		System.out.println("Done!");
	}


//	@SuppressWarnings("unused")
	private void attrDevide(){
		/***** Attribute Spliter *****/
		String[] fields = Reader.getInstance().getFields();
		for(int i = 0;i<fields.length;i++){
			String field = fields[i];
			trains.sort((t1,t2)->{
				Long n1 = Long.parseLong(t1[t1.length-1]);
				Long n2 = Long.parseLong(t2[t2.length-1]);
				return n1.compareTo(n2);
			});
			int index = i;
			ArrayList<String> keys = new ArrayList<>();
			ArrayList<Double> values = new ArrayList<>();
			trains.forEach(train->{
				keys.add(train[index]);
				values.add(Double.parseDouble(train[train.length-1]));
			});
			
			Writer.getInstance().devide(keys, values, field);
		}
	}
	

	public static void main(String[] args) {
		new HouseEvaluation();
	}
	
}
