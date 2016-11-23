import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class HouseEvaluation {
	
	/* trains나 tests는 csv파일의 record 한줄 한줄을 아이템으로 갖고있습니다.
	따라서, 컬럼별로 바꾸기 위해서는 별도의 변환작업이 필요합니다. */
	private ArrayList<String[]> trains;
	private String[] fields;
	private ArrayList<String[]> tests;
	
	private ArrayList<Double> salePrices;
	private HashMap<String,Double> avgBySection;
	
	public HouseEvaluation(){
		
		//1. 트레이닝/테스트/카테고리 데이터를 읽고 저장합니다. SalePrice는 따로 빼놓습니다.
		readData();
		saveSalePrices();
		
		//2. Numeric을 Category로 바꿔줍니다. SalePrice는 카테고리별 평균을 미리 계산해 놓습니다.
		convertData();
		groupByAvg();
		
		//3. 데이터를 원하는 형식으로 바꿉니다.
		
		avgBySection.keySet().forEach(key->System.out.println("Key:"+key+",Value:"+avgBySection.get(key)));
		
		//4. 알고리즘을 돌립니다. 이 부분도 1,2와 같이 적절한 추상화 부탁드립니다.@수민이형
		
		
		//5. 결과를  writeResult 아래의 포맷에 맞게 바꿉니다. 메서드 하나 만드셔서 작업해주세요.@현호형
		//result : HashMap<ID:Integer, SalePrice:Double>
		
		//6. 결과를 파일로 씁니다. 주석만 해제하면됩니다.
		//writeResult(result);
		
		/*
		 * 출력을 위한 메서드로,
		 * printData() : Train, Test 데이터 모두 출력. 근데 출력 결과가 너무 길어서 위에 짤림.
		 * printTrainData() : Train 데이터 모두 출력. 마찬가지로 위에 짤림.
		 * printTestData() : Test 데이터 모두 출력. 마찬가지로 위에 짤림.
		 * 를 미리 작성해두었습니다.
		 */
//		printTrainData();
		
	}
	
	
	//뉴메릭컬한 SalePrice를 카테고리컬하게 변환하기 전에 미리 빼돌려놓습니다.
	private ArrayList<Double> saveSalePrices(){
		salePrices = trains.stream()
				   		   .map(train->Double.parseDouble(train[train.length-1]))
				   		   .collect(Collectors.toCollection(ArrayList::new));
		return salePrices;
	}
	
	//범위별로 SalePrice 그룹을 나누고 각 그룹별 평균을 구합니다.
	private HashMap<String,Double> groupByAvg(){
		ArrayList<Predicate<Double>> ranges = DomainConvertor.getInstance().getDefinition("SalePrice").ranges;
		avgBySection = new HashMap<>();
		ranges.stream().map(range->salePrices.stream()
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
//		Print Fields(출력결과가 너무 길어서 필드명 밑에 한번 더 출력)
		for(String field : fields)
			System.out.print(field+" ");
	}

	private void printTestData(){
		System.out.println("Test Cases");
		//Print Fields
		for(String field : fields)
			System.out.print(field+"\t");
		//Print Test Data
		tests.forEach(test->{
			System.out.println();
			for(int i=0;i<test.length;i++)
				System.out.print(test[i]+"\t");
		});
//		Print Fields(출력결과가 너무 길어서 필드명 밑에 한번 더 출력)
		for(String field : fields)
			System.out.print(field+" ");
	}
	

	//result<ID, SalePrice>
	private void writeResult(HashMap<Integer, Double> result){
		System.out.println("\n\nNow Writing...");
		final String filename = "output.csv";
		Writer.getInstance().write(result, filename);
		System.out.println("Write Done!");
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
		new HouseEvaluation();
	}
	
}
