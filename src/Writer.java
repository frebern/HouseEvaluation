import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;


public class Writer {
	private static Writer instance;

	private Writer() {

	}

	public static Writer getInstance() {
		if (instance == null)
			instance = new Writer();
		return instance;
	}
	
	public boolean write(HashMap<Integer, Double> result, String filename){
		final String LINE_SEPARATOR = System.getProperty("line.separator");
		try{
			FileWriter fw = new FileWriter(new File(filename), false);
			fw.write("Id,SalePrice");
			fw.write(LINE_SEPARATOR);
			ArrayList<Integer> keys = new ArrayList<>();
			keys.addAll(result.keySet());
			keys.sort(Integer::compareTo);
			
			keys.forEach(key->{
				try {
					fw.write(key+","+result.get(key));
					fw.write(LINE_SEPARATOR);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	class Pair{
		
		String k;
		Double v;
		public Pair(String k, Double v){
			this.k = k;
			this.v = v;
		}
		
	}
	
	/* 필드 나눌 때 사용했음. 더이상 사용하지 않습니다. */
	public boolean devide(ArrayList<String> attrValues, ArrayList<Double> classValues, String field){
		String filename = "./fields/"+field+".csv";
		final String LINE_SEPARATOR = System.getProperty("line.separator");
		try{
			FileWriter fw = new FileWriter(new File(filename), false);
			fw.write(field+",SalePrice");
			fw.write(LINE_SEPARATOR);
			ArrayList<Pair> pairs = new ArrayList<>();
			for(int i=0;i<attrValues.size();i++)
				pairs.add(new Pair(attrValues.get(i), classValues.get(i)));
			
			pairs.sort((p1,p2)->p1.v.compareTo(p2.v)*-1);
			
			pairs.forEach(pair->{
				try {
					fw.write(pair.k+","+pair.v);
					fw.write(LINE_SEPARATOR);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
