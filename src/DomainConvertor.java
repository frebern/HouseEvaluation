import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;


/*
 * 이 클래스는 컬럼별 DomainDefinition들을 전부 관리합니다.
 * 예를들면, PoolArea, ParkingLot, 등등...
 */
public class DomainConvertor {

	
	private HashMap<String, DomainDefinition> dictionary = new HashMap<>();
	
	private static DomainConvertor instance;

	private DomainConvertor() {

		ArrayList<String[]> configs = Reader.getInstance().getConfigs();
		//Print
//		configs.forEach(config->{
//			for(String word:config)
//				System.out.print(word+" ");
//			System.out.println();
//		});
		configs.forEach(config->{
			DomainDefinition dd = new DomainDefinition(config);
			dictionary.put(dd.field, dd);
		});
		
	}
	
	public DomainDefinition getDefinition(String field){
		return dictionary.get(field);
	}
	
	public void convert(String[] fields, ArrayList<String[]> data) {
		
		data.parallelStream()
			.forEach(record->{
				for(int i=0;i<record.length;i++){
					String field = fields[i];
					//If it is Already Categorical Field -> Skip
					if(!dictionary.keySet().contains(field)) continue;
					//Else(Numerical Field)
					record[i] = getCategory(field, record[i]);
				}
			});
		
	}
	
	//A~Z 까지 카테고리를 알려줍니다. 다만 가급적 카테고리를 26개이상으로 나누지는 맙시다.
	public String getCategory(String field, String value){
		
		try{
			double num = Double.parseDouble(value);
			//Range가 모든 범위를 커버한다고 가정합니다.
			ArrayList<Boolean> tmp = dictionary.get(field).ranges.parallelStream()
					 				   					  		 .map(range->range.test(num))
					 				   					  		 .collect(Collectors.toCollection(ArrayList::new));
			
			int i=0;
			for(i=0;i<tmp.size();i++)
				if(tmp.get(i)) break;
			
			return ""+(char)('A'+i);
			
		}catch(NumberFormatException nfe){
			//NA같은 숫자가 아닌 값은 그자체로 하나의 카테고리로써 생각합니다.
			return value;
		}
		
	}

	public static DomainConvertor getInstance() {
		if (instance == null)
			instance = new DomainConvertor();
		return instance;
	}

}
