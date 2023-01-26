import java.util.ArrayList;
import java.util.Arrays;

public class ATCMap {
	
	String table[];
	ArrayList<String> airports;
	public ATCMap(ArrayList<String> airports) {
		table = new String[1000];
		Arrays.fill(table, "");
		this.airports = airports; 
	}
	
	public int hashFunction(String airportCode) {
		int sum = 0;
		for (int i = 0; i < airportCode.length(); i++) {
			int value = airportCode.charAt(i)* ((int) Math.pow(31, i));
			sum += value;
		}
		int result = sum % 1000;
		int k = 1;
		while (!(table[result].equals(""))) {
			result = (sum + k) % 1000;
			k++;
		}
		table[result] = airportCode;
		return result;
		
	}
	
	public String findCode(String airportCode) {
		int val = hashFunction(airportCode);

		String Code;
		if (val < 10) {
			Code = airportCode + "00" + val;
		}
		else if (val < 100) {
			Code = airportCode + "0" + val;
		}
		else {
			Code = airportCode + val;
		}

		return Code;
	}
	
	public ArrayList<String> findAllCodes() {
		ArrayList<String> allCodes = new ArrayList<String>();
		for (String rawCode: airports) {
			allCodes.add(findCode(rawCode));
			
		}
		
		return allCodes;
	}
	
}
