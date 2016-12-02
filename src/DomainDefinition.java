import java.util.ArrayList;
import java.util.function.Predicate;



/*
 * �� Ŭ������ �� �÷��� ������ ������ ����ϴ�.
 * �������, PoolArea��� �÷� �ϳ��� ������ ������(~100, 100~200, 200~300, 300~)�� ��� �ֽ��ϴ�.
 */
public class DomainDefinition {

	private final static double MIN = -9999;
	private final static double MAX = Double.MAX_VALUE; 
	
	public String field;//�ʵ��
	private double below;//����
	private double upper;//�ʰ�
	private ArrayList<Double> rangeUpper = new ArrayList<>();
	private ArrayList<Double> rangeBelow = new ArrayList<>();
	public ArrayList<Predicate<Double>> ranges = new ArrayList<>(); 

	public DomainDefinition(String[] words) {
		interpret(words);
		createRanges();
	}
	
	public ArrayList<Double> getBelows(){
		return rangeBelow;
	}
	
	private void createRanges() {
		
//		System.out.println("RangeUpper:"+rangeUpper);
//		System.out.println("RangeBelow:"+rangeBelow);
		
		int size = rangeUpper.size();
		for(int i=0;i<size;i++){
			final int index = i;
			Predicate<Double> range = new Predicate<Double>() {
				@Override
				public boolean test(Double num) {
					double upperThan = rangeUpper.get(index);
					double belowThan = rangeBelow.get(index);
					if(upperThan == belowThan)
						return num==upperThan;
					return num>upperThan && num<=belowThan;
				}
			};
			ranges.add(range);
		}
		
	}


	//�÷��� config �ؼ�
	private void interpret(String[] words){
		
		//Field Name
		field = words[0].trim();
		
		//�̸� ����
		below = Double.parseDouble(words[1].trim());
		rangeUpper.add(MIN);
		rangeBelow.add(below);
		
		//�߰� ������
		for(int i=2;i<words.length-1;i++){
			String word = words[i].trim();
			if(word.contains("~")){
				String[] range = word.split("~");
				rangeUpper.add(Double.parseDouble(range[0].trim()));
				rangeBelow.add(Double.parseDouble(range[1].trim()));
			}else{
				rangeUpper.add(Double.parseDouble(word));
				rangeBelow.add(Double.parseDouble(word));
			}
		}

		//�̻� ����
		upper = Double.parseDouble(words[words.length-1].trim());
		rangeUpper.add(upper);
		rangeBelow.add(MAX);
		
	}
	
}
