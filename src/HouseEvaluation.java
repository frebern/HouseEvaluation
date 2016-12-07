import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class HouseEvaluation {
	
	/* trains�� tests�� csv������ record ���� ������ ���������� �����ֽ��ϴ�.
	����, �÷����� �ٲٱ� ���ؼ��� ������ ��ȯ�۾��� �ʿ��մϴ�. */
	private ArrayList<String[]> trains;
	private String[] fields;
	private ArrayList<String[]> tests;
	
	public HouseEvaluation(){
		
		//1. Ʈ���̴�/�׽�Ʈ/ī�װ� �����͸� �а� �����մϴ�. SalePrice�� ���� �������ϴ�.
		readData();
		
		//2. Numeric�� Category�� �ٲ��ݴϴ�.
		convertData();
		
		//3. � �˰����̵� ������Բ� �������̽� ���.
		HashMap<Integer, Double> results = new HashMap<Integer, Double>();
		int size = tests.size();
		for(int i=0;i<100;i++){
			String[] test = tests.get(i);
			long start = System.currentTimeMillis();
			System.out.println("Test #"+i+" Start!");
			
			int id = Integer.parseInt(test[0]);
			double salePrice = recursion(trains,test,1.0);
			results.put(id, salePrice);
			
			System.out.println("Test #"+i+" Done!("+(System.currentTimeMillis()-start)/1000.0+"sec)");
		}
		
		//4. ����� ���Ϸ� ���ϴ�. output.svc�� �����ϴ�.
		writeResult(results);
		
	}
	int cnt=0;
	// train�� ���ݾ� ������ ������ ���� Ȯ���� ���ϰ� recursive�ϰ� �����Ѵ�.
	private double recursion(ArrayList<String[]> trains_num, String[] test, double beforeProb){
//		System.out.println("Call #"+cnt++);
		ArrayList<String[]> trains_upper = new ArrayList<>();
		ArrayList<String[]> trains_lower = new ArrayList<>();
		ArrayList<String[]> trains_cat = new ArrayList<>();
		double upperProb, lowerProb;
		divide(trains_num, trains_cat, trains_upper, trains_lower);
		
		System.out.println("AFTER DIVIDE:"+trains_upper.size()+","+trains_lower.size()+","+trains_cat.size());
		for(String[] train:trains_cat){
			System.out.print(train[train.length-1]+",");
		}
		System.out.println();
		
		Algorithm algorithm = NaiveBayesian.getInstance();
		algorithm.readData(trains_cat, test);
		algorithm.runAlgorithm();
		
		HashMap<String,Double> probabilities = algorithm.getProbabilities();
		if(probabilities.keySet().size()<=1)
			return average(trains_num)*beforeProb;
		
		upperProb = probabilities.get("upper");
		lowerProb = probabilities.get("lower");
		
		System.out.println("upperProb:"+upperProb+",lowerProb:"+lowerProb);
		
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
//		if(Math.max(upperProb, lowerProb)>0.7) return true;
//		if(Math.min(trains_upper.size(), trains_lower.size()) <= 1) return true;
//		else return false;
		
		return Math.max(trains_upper.size(), trains_lower.size()) <= 2;
	}
	
	// trains_num�� salesPrice�� sorting�ϰ� ���ݾ� trains_upper, trains_lower�� add�Ѵ�.
	private void divide(ArrayList<String[]> trains_num, ArrayList<String[]> trains_cat, 
			ArrayList<String[]> trains_upper, ArrayList<String[]> trains_lower){
		
//		trains_cat.clear();
//		trains_upper.clear();
//		trains_lower.clear();
//		
////		System.out.println("trains_num Size:"+trains_num.size());
//		
//		final int index = trains_num.get(0).length-1;
//		
//		trains_num.sort((t1,t2)->{
//			double sp1 = Double.parseDouble(t1[index]);
//			double sp2 = Double.parseDouble(t2[index]);
//			return Double.compare(sp1,sp2);
//		});
//		int midIndex = trains_num.size()/2;
//		double pivot = Double.parseDouble(trains_num.get(midIndex)[index]);
//		//trains_lower
//		trains_num.stream()
//				  .filter(train->Double.parseDouble(train[index])<=pivot)
//				  .forEach(train->trains_lower.add(train));
//		
//		//trains_upper
//		trains_num.stream()
//				  .filter(train->Double.parseDouble(train[index])>pivot)
//				  .forEach(train->trains_upper.add(train));
//		
//		copyTo(trains_num,trains_cat);
//		
//		trains_cat.stream().forEach(train->{
//			double value = Double.parseDouble(train[index]);
//			train[index] = value<=pivot?"lower":"upper";
//		});
//		
////		System.out.println("Pivot:"+pivot+",upperSize:"+trains_upper.size()+",lowerSize:"+trains_lower.size());
//		
		
		trains_cat.clear();
		trains_upper.clear();
		trains_lower.clear();
		
		trains_num.sort((t1,t2)->{
			double sp1 = Double.parseDouble(t1[t1.length-1]);
			double sp2 = Double.parseDouble(t2[t2.length-1]);
			return Double.compare(sp1, sp2);
		});
		copyTo(trains_num,trains_cat);
		
		int catSize = trains_cat.size();
		//Convert To Category Class
		for(int i=0;i<catSize;i++){
//			System.out.println(('A'+i/((catSize/20)-1))+"");
			trains_cat.get(i)[trains_cat.get(i).length-1] = (char)('A'+i/(catSize/20))+"";
		}
		
		
		//���� Ʈ���̴� �����Ϳ� ���ؼ� Outlook, Windy, Humidity, Temperature�� IG�� ���� ���մϴ�.
		//�̸� ���� ���� Ʈ���̴� �������� ������ �÷� ������ �����մϴ�.
		ArrayList<Double> informationGains = new ArrayList<>();
		ArrayList<ArrayList<String>> attrs = new ArrayList<>();
		
		int numOfAttrs = trains_cat.get(0).length;
		for(int i=0;i<numOfAttrs;i++)
			attrs.add(new ArrayList<>());
		
		for(int i=0;i<trains_cat.size();i++){
			String[] train = trains_cat.get(i);
			for(int j=0;j<numOfAttrs;j++)
				attrs.get(j).add(train[j]);
		}
		
		attrs.remove(0);//Remove ID
		numOfAttrs--;
		ArrayList<String> y = attrs.remove(--numOfAttrs);//Class Pop
		System.out.println(y);
		
		//�� �ʵ忡 ���ؼ� IG�� ����ϰ�, �� ������ ���մϴ�.
		ArrayList<String> criterias = new ArrayList<>();
		for(int i=0;i<attrs.size();i++){
			int criteriaIndex;
			double ig;
			criteriaIndex = 0;
			ig = calcCategoricalIG(attrs.get(i), y);
			
			criterias.add(attrs.get(i).get(criteriaIndex));
			informationGains.add(ig);
		}
		
		System.out.println("IGs:"+informationGains);

//		�� �ʵ庰 IG�� Print
//		System.out.println("Information Gains:"+informationGains);
		
		//max IG�� ã���ϴ�.
		int index=0;
		//trains�� ù �����͸� �������� ����� �Ǵܱ����� ���մϴ�.
		//������� ù ������ {Sunny, Hot, High, Weak}�̰� Outlook�� ���� IG�� ���ٸ�, �� ����� �Ǵܱ�����  isSunny?�� �˴ϴ�.

		//�ִ� IG���� ��� �ʵ����� ã���ϴ�.
		index = informationGains.indexOf(informationGains.stream().max(Double::compareTo).get());
		
		//�� �ʵ��� �з������� ã���ϴ�.
		String criteria = criterias.get(index);
		
		//�� �ʵ尡 ī�װ����ϸ� �״�� �����մϴ�.
		final int in = index+1;
		//�ش� ī�װ����� �ƴ����� ���� �����ϴ�.
		for(int i=0;i<trains_cat.size();i++){
			String[] train_cat = trains_cat.get(i);
			String[] train_num = trains_num.get(i);
			boolean isEqual = train_cat[in].equals(criteria);
			train_cat[train_cat.length-1] = isEqual?"lower":"upper";
			ArrayList<String[]> ref = isEqual?trains_lower:trains_upper;
			ref.add(train_num);
		}
		
		System.out.println("lowerSize:"+trains_lower.size());
		System.out.println("upperSize:"+trains_upper.size());
		
	}
	
	//1. ���� trains�� ���ؼ� Outlook, Windy, Humidity, Temperature�� IG�� ���� ���Ѵ�.
	//IG = H(Y|Outlook)
	//   = - (P(X=S)H(Y|X=S) + P(X=O)H(Y|X=O) + P(X=R)H(Y|X=R))
	//H(Y|X=S)
	//= -(P(Y=yes|X=S)log2(P(Y=yes|X=S)) 
	//     + P(Y=no|X=S)log2(P(Y=no|X=S)))
	
	private double calcCategoricalIG(ArrayList<String> attrValues, ArrayList<String> y) {
		
		//Yes No
		TreeSet<String> classKeys = new TreeSet<>();
		y.forEach(c->classKeys.add(c));
		
		//Sunny Rain Overcast
		TreeSet<String> attrValueSet = new TreeSet<>();
		attrValues.forEach(attr->attrValueSet.add(attr));
		
		//Foreach xi, Calculate P(X=xi)H(Y|X=xi)
		DoubleStream entropys = attrValueSet.stream().mapToDouble(attrValue->{
			
			//H(Y|X=Math)
			//attrValue = Sunny
			//Yes: 3, No: 2
			HashMap<String,Integer> classCounts = new HashMap<>();
			for(int i=0;i<attrValues.size();i++){
				if(attrValues.get(i).equals(attrValue)){
					String c = y.get(i);
					classCounts.put(c, classCounts.getOrDefault(c, 0)+1);
				}
			}
			
//			System.out.println("CC"+classCounts);
			
			Supplier<IntStream> classCountsStream = ()->classCounts.values().stream().mapToInt(a->a);
			int size = classCountsStream.get().sum();
			double entropy = classCountsStream.get().mapToDouble(classCount-> {
				double p = (1.0 * classCount)/size;
//				System.out.println("P:"+classCount+"/"+size);
				return -1.0 * p * (Math.log(p) / Math.log(2)); // -P(Y|X=xi)log2(P(Y|X=xi))
			}).sum();
			
			
			double pX = (attrValues.stream()
								   .filter(attr->attr.equals(attrValue))
								   .count()*1.0)
								   /attrValues.size();

			return entropy * pX;
		});

		
		double h = classKeys.stream().mapToDouble(key->{
			double cnt = (double)(y.stream().filter(c->c.equals(key)).count());
			return cnt/y.size();
		}).sum();
		
		double sum = entropys.sum();
//		System.out.println("H:"+h);
//		System.out.println("Entropy: "+sum);
		
		double result = h-sum;

		return result;
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
		System.out.println("Now Running...");
		new HouseEvaluation();
		System.out.println("Done!");
		System.out.println("Time Elapsed: "+(System.currentTimeMillis()-start)/1000.0+"sec");
	}
	
}
