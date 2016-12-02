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
	private HashMap<String,Double> avgBySection = new HashMap<>();
	
	private ArrayList<String[]> trains_num = new ArrayList<>();
	private ArrayList<String[]> tests_num = new ArrayList<>();
	
	private ArrayList<String[]> trains_cat = new ArrayList<>();
	private ArrayList<String[]> tests_cat = new ArrayList<>();
	
	private ArrayList<String[]> trains_next = new ArrayList<>();
	private ArrayList<String[]> tests_next = new ArrayList<>();
	
	private ArrayList<String> naFields = new ArrayList<>();
	
	private HashMap<String, ArrayList<Integer>> whoIsNA = new HashMap<>();
	
	//HashMap<FieldName:String, Result:HashMap<ID,Result>>
	private HashMap<String,HashMap<Integer,String>> results = new HashMap<>();
	
	ArrayList<String[]> table_old = new ArrayList<>();
	
	public HouseEvaluation(){
		int count=0;
		
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
		System.out.println(whoIsNA.get("LotFrontage"));
		
		int iteration = 1;
		do{
			
			System.out.println("Iteration #"+iteration++);
			
			//����� �߰��� ����� ��̸���Ʈ ��� Ŭ���� �մϴ�.
			clearAll();
			
			//�� NA �ʵ忡 ���ؼ�.
			for(String fieldName : naFields){
				
				System.out.println("Debug For "+fieldName);
				
				//���� ���޸��� �������̺��� trains_num�� tests_num���� �����ϴ�.
				//System.out.println("\nKey:"+fieldName+",Values:"+whoIsNA.get(fieldName));
				copyTo(table,table_old);
				
				seperateTables(table, whoIsNA.get(fieldName));
				
				//��ī�� �մϴ�.
				
				copyTo(trains_num,trains_cat);
				copyTo(tests_num,tests_cat);
				copyTo(trains_num,trains_next);
				copyTo(tests_num,tests_next);
				
				//cat�� Naive Bayesian ������ ���� ī�װ����ϰ� �ٲ��ݴϴ�.
				convertData(trains_cat,tests_cat);
				
				//trains_cat�� tests_cat�� �ش� �÷��� �� �ڷ� �о��ݴϴ�.
				columnToClass(fieldName, trains_num);
				columnToClass(fieldName, tests_num);
				columnToClass(fieldName, trains_cat);
				columnToClass(fieldName, tests_cat);
				
				//���� ���� class�� ���� numeric�̶�� �� ������ ��յ� ���س����ϴ�.
				boolean isCat = !isNumericField(fieldName);
				if(!isCat){
					/* ĸ��ȭ �ʿ�. */
					System.out.println(fieldName+" is Numeric Field");
					
					System.out.printf("Value of '"+fieldName+"' in trains_num:\t");
					trains_num.forEach(train->
						System.out.print(train[train.length-1]+"\t"));
					System.out.printf("\nValue of '"+fieldName+"' in trains_cat:\t");
					trains_cat.forEach(train->
						System.out.print(train[train.length-1]+"\t"));
					
					System.out.println();
					ArrayList<Double> values = trains_num.parallelStream()
					   		   						 	 .map(train->Double.parseDouble(train[train.length-1]))
					   		   						 	 .collect(Collectors.toCollection(ArrayList::new));
					groupByAvg(fieldName,values);
				}
				
				//�˰��� �����ϴ�.
				Algorithm algorithm = NaiveBayesian.getInstance();
				algorithm.readData(trains_cat, tests_cat, avgBySection);
				algorithm.runAlgorithm(isCat);
				HashMap<Integer, String> result = algorithm.getPredictions();
				HashMap<Integer, String> new_result = new HashMap<>();
				new_result.putAll(result);
			
				results.put(fieldName, new_result);
				
			}
			//�ٲ� ������� table�� �ݿ��մϴ�.
			reflectResults(table, results);
			count++;
			if(count==20)
			{
				Writer.getInstance().writeNonNaTable("train_non_na.csv",table,fields);
			}
			if(count==21)
			{
				break;
			}
			
		}while(!isConverge(table_old, table)); //�������� ������ ��� �����ϴ�.
	
		/*for(int i=0;i<table.size();i++)
		{
			for(int j=0;j<table.get(i).length;j++)
			{
				System.out.print(table.get(i)[j]+" ");
			}
			System.out.println();
		}*/
		Writer.getInstance().writeNonNaTable("train_non_na1.csv",table,fields);
		
	}
	
	
	private void clearAll() {
		results.clear();
		trains_num.clear();
		trains_cat.clear();
		trains_next.clear();
		tests_num.clear();
		tests_cat.clear();
		tests_next.clear();
		avgBySection.clear();
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
		
		for(int i=0;i<oldTable.size();i++)
		{
			for(int j=0;j<oldTable.get(i).length;j++)
			{
				if(!oldTable.get(i)[j].equals(reflectedTable.get(i)[j]))
				{
					return false;
				}
				
			}
		}
		
		return true;
	}

	//results���� Prediction���� ���� ������� <ID, Result> ���� ����ִµ�, key�� �ش� �ʵ���Դϴ�.
	//originTable�� results�� �ѹ��� �ݿ��մϴ�.
	private void reflectResults(ArrayList<String[]> originTable, HashMap<String, HashMap<Integer, String>> results) {

		for(int i=0;i<fields.length;i++)
		{
			if(results.get(fields[i])!=null)
			{
				for(int j=0;j<originTable.size();j++)
				{
					if(results.get(fields[i]).get(j)!=null)
					{
						originTable.get(j)[i]=results.get(fields[i]).get(j);
					}
				}
			}
		}
		
	}
	
	//�ش� �ʵ尡 ���޸� �ʵ����� �˾Ƴ��ϴ�. DomainConvertor Ŭ������ ���� �� ���Դϴ�.
	private boolean isNumericField(String fieldName) {
		return DomainConvertor.getInstance().isNumericField(fieldName);
	}

	//�ش� �÷��� ���̺��� �� �ڷ� �����ϴ�.
		private void columnToClass(String fieldName, ArrayList<String[]> table) {
			int index=0,i;
			// fieldName�� index�� ���Ѵ�.
			for(i=0;i<fields.length;i++)
				if(fields[i].equals(fieldName))
					index=i;
			for(String[] line:table){
//				 ������ �ٲ� row -> temp
				String[] temp = new String[line.length];
				for(i=0;i<line.length;i++){
					if(i!=index) temp[i]=line[i]; // �ٲ� index�� ���������� �׳� ����
					else{	// ������ �ǵڷ� �����ϰ�
						temp[temp.length-1] = line[i];
						i++;
						// i�� 1�ø��� ���� �������� ��ĭ�� �з��� �߰�. �ǵڷ� �ϳ��� �߰��� �ڹǷ� length-1������ �����Ѵ�.
						for(;i<line.length;i++) temp[i-1] = line[i];
						// i�� ����� �ö󰬰����� ��������� break.
						break;
					}
				}

//				System.out.print(line.length+"LINE: ");
//				for(String s:line) System.out.print(s+",");
//				System.out.println("");
//				System.out.print(temp.length+"TEMP: ");
//				for(String s:temp) System.out.print(s+",");
//				System.out.println("");
				table.set(table.indexOf(line), temp);	// �ش� line�� ������ �ٲ� temp�� ��ü
			}
			
		}

	//���̺��� na_IDs �γ��� �ƴѳ��� �����ϴ�. ArrayList<String[]> trains_num�� ArrayList<String[]> tests_num.
	private void seperateTables(ArrayList<String[]> table, ArrayList<Integer> na_IDs) {
		trains_num.clear();
		tests_num.clear();
		for(String[] line:table){
			if(na_IDs.contains(Integer.valueOf(line[0]))) tests_num.add(line);
			else trains_num.add(line);
		}
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
						naIndex.addAll(whoIsNA.get(fields[j]));
						naIndex.add(Integer.parseInt(table.get(i)[0]));
						whoIsNA.put(fields[j],naIndex);
					}
					else
					{
						ArrayList<Integer> naIndex=new ArrayList<Integer>();
						naIndex.add(Integer.parseInt(table.get(i)[0]));
						whoIsNA.put(fields[j],naIndex);
					}
				}
			}
		}
	}

	//NA Field�� ��͵��� �ִ��� �ʱ�ȭ�մϴ�.
	private void initNaFields(ArrayList<String[]> table, ArrayList<String> naFields) {
		int i;
		ArrayList<ArrayList<String>> fieldDatas = new ArrayList<ArrayList<String>>();
		for(String field:fields) fieldDatas.add(new ArrayList<String>());
		for(String[] line:table)
			for(i=0;i<line.length;i++)
				fieldDatas.get(i).add(line[i]);
		for(i=0;i<fieldDatas.size();i++)
			if(fieldDatas.get(i).contains("NA")) naFields.add(fields[i]);
		
	}

	//�������� SalePrice �׷��� ������ �� �׷캰 ����� ���մϴ�.
	private HashMap<String,Double> groupByAvg(String fieldName, ArrayList<Double> values){
		ArrayList<Predicate<Double>> ranges = DomainConvertor.getInstance().getDefinition(fieldName).ranges;
		avgBySection.clear();
		ranges.parallelStream()
			  .map(range->values.stream()
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
	
	

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		System.out.println("Now Running... ");
		new HouseEvaluation();
		System.out.println("Done!");
		System.out.println("Time Elapsed: "+(System.currentTimeMillis()-start)/1000.0+"sec");
	}
	
}

