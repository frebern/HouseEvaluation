import java.util.ArrayList;
import java.util.HashMap;

public class HouseEvaluation {
	
	/* trains나 tests는 csv파일의 record 한줄 한줄을 아이템으로 갖고있습니다.
	따라서, 컬럼별로 바꾸기 위해서는 별도의 변환작업이 필요합니다. */
	private ArrayList<String[]> trains;
	private String[] fields;
	private ArrayList<String[]> tests;
	
	public HouseEvaluation(){
		
		//1. 트레이닝/테스트/카테고리 데이터를 읽고 저장합니다. SalePrice는 따로 빼놓습니다.
		readData();
		
		//2. Numeric을 Category로 바꿔줍니다.
		convertData();
		
		//3. 어떤 알고리즘이든 상관없게끔 인터페이스 사용.
		HashMap<Integer, Double> results = new HashMap<Integer, Double>();
		tests.forEach(test->{
			cnt=0;
			int id = Integer.parseInt(test[0]);
			double salePrice = recursion(trains,test,1.0);
			results.put(id, salePrice);
		});
		
		//4. 결과를 파일로 씁니다. output.svc에 써집니다.
		writeResult(results);
		
	}
	
	int cnt=0;
	// train을 절반씩 나누며 각각에 대한 확률을 구하고 recursive하게 수행한다.
	private double recursion(ArrayList<String[]> trains_num, String[] test, double beforeProb){
		System.out.println("Call #"+cnt++);
		ArrayList<String[]> trains_upper = new ArrayList<>();
		ArrayList<String[]> trains_lower = new ArrayList<>();
		ArrayList<String[]> trains_cat = new ArrayList<>();
		double upperProb, lowerProb;
		divide(trains_num, trains_cat, trains_upper, trains_lower);
		
		Algorithm algorithm = NaiveBayesian.getInstance();
		algorithm.readData(trains_cat, test);
		algorithm.runAlgorithm();
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
	
	// salesPrice의 평균을 구하는 메소드
	private double average(ArrayList<String[]> trains) {
		double salesAverage;
		salesAverage=trains.stream().mapToDouble((item)->Double.parseDouble(item[item.length-1])).average().getAsDouble();
		
		return salesAverage;
	}

	// Recursion method의 중단조건. 두 확률중 한쪽이 일정확률(0.7)을 넘거나, 남은 trains의 size가 일정 개수 이하일 시 true return.
	private boolean stoppingCriteria(ArrayList<String[]> trains_upper, ArrayList<String[]> trains_lower, double upperProb, double lowerProb){
		if(Math.max(upperProb, lowerProb)>0.7) return true;
		else if(Math.min(trains_upper.size(), trains_lower.size()) <= 1) return true;
		else return false;
	}
	
	// trains_num의 salesPrice로 sorting하고 절반씩 trains_upper, trains_lower에 add한다.
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
	
	//from에서 to로 Deep Copy합니다.
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
//		Print Fields(출력결과가 너무 길어서 필드명 밑에 한번 더 출력)
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
//		Print Fields(출력결과가 너무 길어서 필드명 밑에 한번 더 출력)
		System.out.println();
		for(String field : fields)
			System.out.printf("%16s",field);
		System.out.println();
	}
	

	//result<ID, SalePrice>
	private void writeResult(HashMap<Integer, Double> result){
		final String filename = "output.csv";
		Writer.getInstance().write(result, filename);
	}


	@SuppressWarnings("unused")
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
		long start = System.currentTimeMillis();
		new HouseEvaluation();
		System.out.println("Done!");
		System.out.println("Time Elapsed: "+(System.currentTimeMillis()-start)/1000.0+"sec");
	}
	
}
