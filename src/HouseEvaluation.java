import java.util.ArrayList;
import java.util.HashMap;
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
		
		//1. Ʈ���̴�/�׽�Ʈ/ī�װ��� �����͸� �а� �����մϴ�. SalePrice�� ���� �������ϴ�.
		readData();
		saveSalePrices();
		
		//2. Numeric�� Category�� �ٲ��ݴϴ�. SalePrice�� ī�װ����� ����� �̸� ����� �����ϴ�.
		convertData();
		groupByAvg();
		
		//3. �����͸� ���ϴ� �������� �ٲߴϴ�.
		
		avgBySection.keySet().forEach(key->System.out.println("Key:"+key+",Value:"+avgBySection.get(key)));
		
		//4. �˰������� �����ϴ�. �� �κе� 1,2�� ���� ������ �߻�ȭ ��Ź�帳�ϴ�.@��������
		
		
		//5. �����  writeResult �Ʒ��� ���˿� �°� �ٲߴϴ�. �޼��� �ϳ� ����ż� �۾����ּ���.@��ȣ��
		//result : HashMap<ID:Integer, SalePrice:Double>
		
		//6. ����� ���Ϸ� ���ϴ�. �ּ��� �����ϸ�˴ϴ�.
		//writeResult(result);
		
		/*
		 * ����� ���� �޼����,
		 * printData() : Train, Test ������ ��� ���. �ٵ� ��� ����� �ʹ� �� ���� ©��.
		 * printTrainData() : Train ������ ��� ���. ���������� ���� ©��.
		 * printTestData() : Test ������ ��� ���. ���������� ���� ©��.
		 * �� �̸� �ۼ��صξ����ϴ�.
		 */
//		printTrainData();
		
	}
	
	
	//���޸����� SalePrice�� ī�װ������ϰ� ��ȯ�ϱ� ���� �̸� �����������ϴ�.
	private ArrayList<Double> saveSalePrices(){
		salePrices = trains.stream()
				   		   .map(train->Double.parseDouble(train[train.length-1]))
				   		   .collect(Collectors.toCollection(ArrayList::new));
		return salePrices;
	}
	
	//�������� SalePrice �׷��� ������ �� �׷캰 ����� ���մϴ�.
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
//		Print Fields(��°���� �ʹ� �� �ʵ�� �ؿ� �ѹ� �� ���)
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
//		Print Fields(��°���� �ʹ� �� �ʵ�� �ؿ� �ѹ� �� ���)
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