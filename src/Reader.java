import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Reader{
	
	private ArrayList<String[]> trains = new ArrayList<>();
	private ArrayList<String[]> tests = new ArrayList<>();
	private ArrayList<String[]> configs = new ArrayList<>();
	private String[] fields;
	
	private static Reader instance = null;
	
	public static Reader getInstance(){
		if(instance==null)
			instance = new Reader();
		return instance;
	}
	
	private Reader(){
		try {
			//Training Data
			Scanner sc = new Scanner(new File("test/train.csv"));
			fields = sc.nextLine().trim().split(",");
			while(sc.hasNext())
				trains.add(sc.nextLine().trim().split(","));
			sc.close();
			
			//Test Sample Data
			sc = new Scanner(new File("test/test.csv"));
			sc.nextLine();
			while(sc.hasNext())
				tests.add(sc.nextLine().trim().split(","));
			sc.close();
			
			//Numeric to Category Domain Configure File
			sc = new Scanner(new File("test/category.conf"));
			sc.nextLine();
			while(sc.hasNext())
				configs.add(sc.nextLine().trim().split(","));
			sc.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String[] getFields(){
		return fields;
	}
	public ArrayList<String[]> getTrains(){
		return trains;
	}
	public ArrayList<String[]> getTests(){
		return tests;
	}
	public ArrayList<String[]> getConfigs(){
		return configs;
	}
}