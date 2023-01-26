import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ATC extends AirTransportNetwork {
	String name;
	Queue<Flight> readyQueue2;
	Map<String, Integer> waitingLine2;
	public ATC(String name) {
		this.name = name;
		readyQueue2 = new LinkedList<Flight>();
		waitingLine2 = new HashMap<>();
	}

	
}
