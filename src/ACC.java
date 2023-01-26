import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ACC extends AirTransportNetwork {
	String name;
	Queue<Flight> readyQueue1;
	Map<String, Integer> waitingLine1;
	int limit = 30;
	public ACC(String accName) {
		this.name = accName;
		readyQueue1 = new LinkedList<Flight>();
		waitingLine1 = new HashMap<>();
	}


}
	
