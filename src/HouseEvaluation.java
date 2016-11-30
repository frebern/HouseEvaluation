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
	
	private ArrayList<String[]> trains_num;
	private ArrayList<String[]> tests_num;
	
	private ArrayList<String[]> trains_cat;
	private ArrayList<String[]> tests_cat;
	
	private ArrayList<String[]> trains_next;
	private ArrayList<String[]> tests_next;
	
	private ArrayList<String> naFields = new ArrayList<>();
	
	private HashMap<String, ArrayList<Integer>> whoIsNA = new HashMap<>();
	
	//HashMap<FieldName:String, Result:HashMap<ID,Result>>
	private HashMap<String,HashMap<Integer,String>> results = new HashMap<>();
	
	ArrayList<String[]> table_old;
	
	public HouseEvaluation(){
		
		//1. ���� Ʈ���̴�/�׽�Ʈ/ī�װ� �����͸� �а� �����մϴ�.
		/* Reader���� �о���� trains�� tests�� trains.csv�� test.csv�Դϴ�.
		 * ����, �ϴ� ���� tests�� ����ִ� ���� �����Ⱚ�̶�� �����ϰ� ó���մϴ�.*/
		readData();
		
		//�켱 trains�� NA�� ���� ���մϴ�. tests�� NA�� ���߿� ���մϴ�.
		ArrayList<String[]> table = trains;
		
		//NA�� �ִ� �ʵ尡 � �͵����� ���س����ϴ�.
		initNaFields(table, naFields);
		
		//�� NA�ʵ忡���ؼ� � ID�� NA���� ���մϴ�.
		initWhoIsNA(table, naFields);
		
		do{
			
			//����� Ŭ���� �մϴ�.
			results.clear();
			
			//�� NA �ʵ忡 ���ؼ�.
			for(String fieldName : naFields){
				//���� ���޸��� �������̺��� trains_num�� tests_num���� �����ϴ�.
				seperateTables(table, whoIsNA.get(fieldName));
				
				//��ī�� �մϴ�.
				copyTo(table,table_old);
				copyTo(trains_num,trains_cat);
				copyTo(tests_num,tests_cat);
				copyTo(trains_num,trains_next);
				copyTo(tests_num,tests_next);
				
				//cat�� Naive Bayesian ������ ���� ī�װ����ϰ� �ٲ��ݴϴ�.
				convertData(trains_cat,tests_cat);
				
				//trains_cat�� tests_cat�� �ش� �÷��� �� �ڷ� �о��ݴϴ�.
				columnToClass(fieldName, trains_cat);
				columnToClass(fieldName, tests_cat);
				
				//���� ���� class�� ���� numeric�̶�� �� ������ ��յ� ���س����ϴ�.
				boolean isCat = !isNumericField(fieldName);
				if(!isCat)
					avgBySection = groupByAvg(fieldName);
				
				//�˰��� �����ϴ�.
				Algorithm algorithm = NaiveBayesian.getInstance();
				algorithm.readData(trains_cat, tests_cat, avgBySection);
				algorithm.runAlgorithm(isCat);
				HashMap<Integer, String> result = algorithm.getPredictions();
			
				results.put(fieldName, result);
				
			}
			
			//�ٲ� ������� table�� �ݿ��մϴ�.
			reflectResults(table, results);
			
		}while(!isConverge(table_old, table)); //�������� ������ ��� �����ϴ�.
	
		Writer.getInstance().writeNonNaTable("train_non_na.csv",table);
		
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

	//oldTable�� reflectedTable�� ���빰�� ���� ����ġ Ȯ���մϴ�.(�����ϴ��� ���� Ȯ��)
	private boolean isConverge(ArrayList<String[]> oldTable, ArrayList<String[]> reflectedTable) {
		// TODO Auto-generated method stub
		
		return false;
	}

	//results���� Prediction���� ���� ������� <ID, Result> ���� ����ִµ�, key�� �ش� �ʵ���Դϴ�.
	//originTable�� results�� �ѹ��� �ݿ��մϴ�.
	private void reflectResults(ArrayList<String[]> originTable, HashMap<String, HashMap<Integer, String>> results) {
		// TODO Auto-generated method stub
		
		for(int i=0;i<originTable.size();i++)
		{
			for(int j=0;j<originTable.get(i).length;j++)
			{
				
			}
		}
		
	}

	//�ش� �ʵ尡 ���޸� �ʵ����� �˾Ƴ��ϴ�. DomainConvertor Ŭ������ ���� �� ���Դϴ�.
	private boolean isNumericField(String fieldName) {
		// TODO Auto-generated method stub
		
		return false;
	}

	//�ش� �÷��� ���̺��� �� �ڷ� �����ϴ�.
	private void columnToClass(String fieldName, ArrayList<String[]> table) {
		// TODO Auto-generated method stub
		
	}

	//���̺��� na_IDs �γ��� �ƴѳ��� �����ϴ�.
	private void seperateTables(ArrayList<String[]> table, ArrayList<Integer> na_IDs) {
		// TODO Auto-generated method stub
		
	}

	//whoIsNA�� �ʱ�ȭ�մϴ�. 
	//whoIsNA�� HashMap<FieldName:String, IDs:ArrayList<ID:Integer>> �� �����Դϴ�. 
	private void initWhoIsNA(ArrayList<String[]> table, ArrayList<String> naFields) {
		
		for(int i=0;i<table.size();i++)
		{	
			for(int j=0;j<table.get(i).length;j++)
			{
				if(table.get(i)[j].equals("NA"))
				{
					if(whoIsNA.get(fields[j])!=null)
					{
						ArrayList<Integer> naIndex=new ArrayList<Integer>();
						naIndex=whoIsNA.get(fields[j]);
						naIndex.add(j);
						whoIsNA.put(fields[j],naIndex);
					}
					else
					{
						ArrayList<Integer> naIndex=new ArrayList<Integer>();
						naIndex.add(j);
						whoIsNA.put(fields[j],naIndex);
					}
				}
			}
		}
	}

	//NA Field�� ��͵��� �ִ��� �ʱ�ȭ�մϴ�.
	private void initNaFields(ArrayList<String[]> table, ArrayList<String> naFields) {
		// TODO Auto-generated method stub
		int i;
		ArrayList<ArrayList<String>> fieldDatas = new ArrayList<ArrayList<String>>();
		for(String field:fields) fieldDatas.add(new ArrayList<String>());
		for(String[] line:table)
			for(i=0;i<line.length;i++)
				fieldDatas.get(i).add(line[i]);
		for(i=0;i<fieldDatas.size();i++)
			if(fieldDatas.get(i).contains("NA")) naFields.add(fields[i]);
		
	}


	//���޸����� SalePrice�� ī�װ����ϰ� ��ȯ�ϱ� ���� �̸� �����������ϴ�.
	@SuppressWarnings("unused")
	private ArrayList<Double> saveSalePrices(){
		salePrices = trains.parallelStream()
				   		   .map(train->Double.parseDouble(train[train.length-1]))
				   		   .collect(Collectors.toCollection(ArrayList::new));
		return salePrices;
	}
	
	//�������� SalePrice �׷��� ������ �� �׷캰 ����� ���մϴ�.
	private HashMap<String,Double> groupByAvg(String fieldName){
		ArrayList<Predicate<Double>> ranges = DomainConvertor.getInstance().getDefinition(fieldName).ranges;
		avgBySection = new HashMap<>();
		ranges.parallelStream()
			  .map(range->salePrices.parallelStream()
									.filter(range)
									.mapToDouble(s->(double)s)
									.average()
									.getAsDouble()
			  )
			  .forEach(avg->{
				  String key = DomainConvertor.getInstance().getCategory(fieldName, avg+"");
				  avgBySection.put(key, avg);
			  });
		
		return avgBySection;
	}
	
	private void convertData(ArrayList<String[]> trains_cat, ArrayList<String[]> tests_cat) {
		DomainConvertor.getInstance().convert(fields, trains_cat);
		DomainConvertor.getInstance().convert(fields, tests_cat);
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
		System.out.print("Now Running... ");
		new HouseEvaluation();
		System.out.println("Done!");
		System.out.println("Time Elapsed: "+(System.currentTimeMillis()-start)/1000.0+"sec");
	}
	
}

