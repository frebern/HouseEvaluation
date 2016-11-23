import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;


/*
 * �� Ŭ������ �÷��� DomainDefinition���� ���� �����մϴ�.
 * �������, PoolArea, ParkingLot, ���...
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
	
	//A~Z ���� ī�װ��� �˷��ݴϴ�. �ٸ� ������ ī�װ��� 26���̻����� �������� ���ô�.
	public String getCategory(String field, String value){
		
		try{
			double num = Double.parseDouble(value);
			//Range�� ��� ������ Ŀ���Ѵٰ� �����մϴ�.
			ArrayList<Boolean> tmp = dictionary.get(field).ranges.parallelStream()
					 				   					  		 .map(range->range.test(num))
					 				   					  		 .collect(Collectors.toCollection(ArrayList::new));
			
			int i=0;
			for(i=0;i<tmp.size();i++)
				if(tmp.get(i)) break;
			
			return ""+(char)('A'+i);
			
		}catch(NumberFormatException nfe){
			//NA���� ���ڰ� �ƴ� ���� ����ü�� �ϳ��� ī�װ��ν� �����մϴ�.
			return value;
		}
		
	}

	public static DomainConvertor getInstance() {
		if (instance == null)
			instance = new DomainConvertor();
		return instance;
	}

}
