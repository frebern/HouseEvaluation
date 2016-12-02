import java.util.ArrayList;
import java.util.function.Predicate;



/*
 * 이 클래스는 한 컬럼의 도메인 정보를 담습니다.
 * 예를들면, PoolArea라는 컬럼 하나의 도메인 범위들(~100, 100~200, 200~300, 300~)을 담고 있습니다.
 */
public class DomainDefinition {

	private final static double MIN = -9999;
	private final static double MAX = Double.MAX_VALUE; 
	
	public String field;//필드명
	private double below;//이하
	private double upper;//초과
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


	//컬럼별 config 해석
	private void interpret(String[] words){
		
		//Field Name
		field = words[0].trim();
		
		//미만 범위
		below = Double.parseDouble(words[1].trim());
		rangeUpper.add(MIN);
		rangeBelow.add(below);
		
		//중간 범위들
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

		//이상 범위
		upper = Double.parseDouble(words[words.length-1].trim());
		rangeUpper.add(upper);
		rangeBelow.add(MAX);
		
	}
	
}
