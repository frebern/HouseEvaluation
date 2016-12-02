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
		
		//1. 최초 트레이닝/테스트/카테고리 데이터를 읽고 저장합니다.
		/* Reader에서 읽어오는 trains와 tests는 trains.csv와 test.csv입니다.
		 * 따라서, 일단 현재 tests에 들어있는 값은 쓰레기값이라고 생각하고 처리합니다.*/
		readData();
		
		//우선 trains의 NA를 먼저 구합니다. tests의 NA는 나중에 구합니다.
		ArrayList<String[]> table = trains;
		
		//NA가 있는 필드가 어떤 것들인지 구해놓습니다.
		initNaFields(table, naFields);
		
		//각 NA필드에대해서 어떤 ID가 NA인지 구합니다.
		initWhoIsNA(table, naFields);
		
		do{
			
			//결과와 중간에 사용한 어레이리스트 모두 클리어 합니다.
			clearAll();
			
			//각 NA 필드에 대해서.
			for(String fieldName : naFields){
				//현재 뉴메릭한 통합테이블을 trains_num과 tests_num으로 나눕니다.
				//System.out.println("\nKey:"+fieldName+",Values:"+whoIsNA.get(fieldName));
				
				seperateTables(table, whoIsNA.get(fieldName));
				
				//딥카피 합니다.
				copyTo(table,table_old);
				copyTo(trains_num,trains_cat);
				copyTo(tests_num,tests_cat);
				copyTo(trains_num,trains_next);
				copyTo(tests_num,tests_next);
				
				//cat은 Naive Bayesian 돌리기 위해 카테고리컬하게 바꿔줍니다.
				convertData(trains_cat,tests_cat);
				
				//trains_cat과 tests_cat의 해당 컬럼을 맨 뒤로 밀어줍니다.
				columnToClass(fieldName, trains_num);
				columnToClass(fieldName, tests_num);
				columnToClass(fieldName, trains_cat);
				columnToClass(fieldName, tests_cat);
				
				//만약 현재 class가 원래 numeric이라면 각 구간별 평균도 구해놓습니다.
				boolean isCat = !isNumericField(fieldName);
				if(!isCat){
					/* 캡슐화 필요. */
//					System.out.println("DEBUG:");
//					trains_num.forEach(train->{
//						for(String word:train)
//							System.out.print(word+" ");
//						System.out.println();
//					});
					ArrayList<Double> values = trains_num.parallelStream()
					   		   						 	 .map(train->Double.parseDouble(train[train.length-1]))
					   		   						 	 .collect(Collectors.toCollection(ArrayList::new));
					avgBySection = groupByAvg(fieldName,values);
				}
				
				//알고리즘 돌립니다.
				Algorithm algorithm = NaiveBayesian.getInstance();
				algorithm.readData(trains_cat, tests_cat, avgBySection);
				algorithm.runAlgorithm(isCat);
				HashMap<Integer, String> result = algorithm.getPredictions();
			
				results.put(fieldName, result);
				
			}
			
			//바뀐 결과들을 table에 반영합니다.
			reflectResults(table, results);
			
		}while(!isConverge(table_old, table)); //수렴하지 않으면 계속 돌립니다.
	
		Writer.getInstance().writeNonNaTable("train_non_na.csv",table,fields);
		
	}
	
	
	private void clearAll() {
		results.clear();
		trains_num.clear();
		trains_cat.clear();
		trains_next.clear();
		tests_num.clear();
		tests_cat.clear();
		tests_next.clear();
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

	//oldTable과 reflectedTable의 내용물이 전부 같은치 확인합니다.(수렴하는지 여부 확인)
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

	//results에는 Prediction으로 나온 결과들이 <ID, Result> 페어로 들어있는데, key는 해당 필드명입니다.
	//originTable에 results를 한번에 반영합니다.
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
	
	//해당 필드가 뉴메릭 필드인지 알아냅니다. DomainConvertor 클래스를 쓰면 될 것입니다.
	private boolean isNumericField(String fieldName) {
		return DomainConvertor.getInstance().getDefinition(fieldName)!=null;
	}

	//해당 컬럼을 테이블의 맨 뒤로 보냅니다.
		private void columnToClass(String fieldName, ArrayList<String[]> table) {
			int index=0,i;
			// fieldName의 index를 구한다.
			for(i=0;i<fields.length;i++)
				if(fields[i].equals(fieldName))
					index=i;
			for(String[] line:table){
//				 순서가 바뀔 row -> temp
				String[] temp = new String[line.length];
				for(i=0;i<line.length;i++){
					if(i!=index) temp[i]=line[i]; // 바꿀 index를 만날때까지 그냥 복사
					else{	// 만나면 맨뒤로 복사하고
						temp[temp.length-1] = line[i];
						i++;
						// i를 1올리고 남은 아이템을 한칸씩 밀려서 추가. 맨뒤로 하나를 추가한 뒤므로 length-1번까지 수행한다.
						for(;i<line.length;i++) temp[i-1] = line[i];
						// i가 충분히 올라갔겠지만 명시적으로 break.
						break;
					}
				}

//				System.out.print(line.length+"LINE: ");
//				for(String s:line) System.out.print(s+",");
//				System.out.println("");
//				System.out.print(temp.length+"TEMP: ");
//				for(String s:temp) System.out.print(s+",");
//				System.out.println("");
				table.set(table.indexOf(line), temp);	// 해당 line을 순서가 바뀐 temp로 교체
			}
			
		}

	//테이블을 na_IDs 인놈들과 아닌놈들로 나눕니다. ArrayList<String[]> trains_num과 ArrayList<String[]> tests_num.
	private void seperateTables(ArrayList<String[]> table, ArrayList<Integer> na_IDs) {
		for(String[] line:table){
			if(na_IDs.contains(Integer.valueOf(line[0]))) tests_num.add(line);
			else trains_num.add(line);
		}
		
	}

	//whoIsNA를 초기화합니다. 
	//whoIsNA는 HashMap<FieldName:String, IDs:ArrayList<ID:Integer>> 의 형식입니다. 
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

	//NA Field가 어떤것들이 있는지 초기화합니다.
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


	//뉴메릭컬한 SalePrice를 카테고리컬하게 변환하기 전에 미리 빼돌려놓습니다.
	@SuppressWarnings("unused")
	private ArrayList<Double> saveSalePrices(){
		salePrices = trains.parallelStream()
				   		   .map(train->Double.parseDouble(train[train.length-1]))
				   		   .collect(Collectors.toCollection(ArrayList::new));
		return salePrices;
	}
	
	//범위별로 SalePrice 그룹을 나누고 각 그룹별 평균을 구합니다.
	private HashMap<String,Double> groupByAvg(String fieldName, ArrayList<Double> values){
		ArrayList<Predicate<Double>> ranges = DomainConvertor.getInstance().getDefinition(fieldName).ranges;
		avgBySection = new HashMap<>();
		ranges.parallelStream()
			  .map(range->{
				  //System.out.println("DEBUG:"+values);
				  return values.parallelStream()
								.filter(range)
								.mapToDouble(s->(double)s)
								.average()
								.getAsDouble();
			  }
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

