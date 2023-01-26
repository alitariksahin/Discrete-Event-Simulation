import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


public class AirTransportNetwork {
	int globalTime = 0;
	ACC mainACC;
	PriorityQueue<Flight> eventOrder;
	PriorityQueue<Flight> flightOrder;
	PriorityQueue<Flight> rq1EntrancePriority;
	Map<String, ATC> ATCs;	
	Map<String, Flight> flights;
	ArrayList<String> airportCodes;
	
	
	public AirTransportNetwork() {}
	public AirTransportNetwork(String accName, ArrayList<String> atcNames, ArrayList<Flight> flightOrder ) {
		ATCMap map = new ATCMap(atcNames);
		airportCodes = map.findAllCodes();
		this.flightOrder = new PriorityQueue<Flight>(new sortFlights());
		for (Flight f: flightOrder) {
			this.flightOrder.add(f);
		}
		mainACC = new ACC(accName);
		ATCs = new HashMap<>();
		for (String name : atcNames) {
			ATCs.put(name, new ATC(name));
		}
		flights = new HashMap<>();
		for (Flight f: flightOrder) {
			flights.put(f.name, f);
		}
		eventOrder = new PriorityQueue<Flight>(new FlightComparator());
		rq1EntrancePriority = new PriorityQueue<Flight>(new nameComparator());
	}
	
	
	class sortFlights implements Comparator<Flight> {
		public int compare(Flight f1, Flight f2) {
			if (f1.admission == f2.admission) {
				if (f1.name.compareTo(f2.name) > 0) {
					return 1;
				}
				else  {
					return -1;
				}
			}
			else {
				if (f1.admission > f2.admission) {
					return 1;
				}
				else {
					return -1;
				}
			}
		}
	}
	
	class nameComparator implements Comparator<Flight> {
		public int compare(Flight f1, Flight f2) {
			if (f1.name.compareTo(f2.name) > 0) {
				return 1;
			}
			else {
				return -1;
			}
		}
		
	}
	
	class FlightComparator implements Comparator<Flight> {
		public int compare(Flight f1, Flight f2) {
			int f1Value = (f1.newComer)? f1.admission - globalTime: f1.timeTaken[f1.state];
			int f2Value = (f2.newComer)? f2.admission - globalTime: f2.timeTaken[f2.state];
			if (f1Value > f2Value) {
				return 1;
			}
			else if (f1Value < f2Value) {
				return -1;
			}
			else {
				if (f1.name.compareTo(f2.name) > 0) {
					return 1;
				}
				else {
					return -1;
				}
				
			}
		}
	}
	
	public ArrayList<String> getAirportCodes() {
		return airportCodes;
	}
	
	//puts all flights which are close to finish their current process into priority queue and so finds the one which requires the least time.
	//Also in case 30 limit causes a problem, it stores flights overlapping with 30 second limit into another priority queue.
	public void fillEventOrder() {

		eventOrder.clear();
		rq1EntrancePriority.clear();

		// the top newcomer
		if (!flightOrder.isEmpty()) {

			eventOrder.add(flightOrder.peek());

			if (flightOrder.peek().admission - globalTime == mainACC.limit) {
				rq1EntrancePriority.add(flightOrder.peek());
			}
		}
		
		// the top readyQueue1 element
		if (!mainACC.readyQueue1.isEmpty()) {
			eventOrder.add(mainACC.readyQueue1.peek());
			if (mainACC.readyQueue1.peek().timeTaken[mainACC.readyQueue1.peek().state] == mainACC.limit) {
				rq1EntrancePriority.add(mainACC.readyQueue1.peek());
			}
		}

		//the top waitingQueue1 element
		int wq1Time = 1999999999;	
		Flight wq1Flight = null;
		for (Map.Entry<String, Integer> flight: mainACC.waitingLine1.entrySet()) {
			if (flight.getValue() == mainACC.limit) {
				rq1EntrancePriority.add(flights.get(flight.getKey()));
			}
			if (flight.getValue() < wq1Time) {
				wq1Time = flight.getValue();
				wq1Flight = flights.get(flight.getKey());
				
			}
			else if (flight.getValue() == wq1Time && wq1Flight != null && flight.getKey().compareTo(wq1Flight.name) < 0) {
				wq1Flight = flights.get(flight.getKey());
			}
		}
		if (wq1Flight != null) {
			eventOrder.add(wq1Flight);
		}
		
		//the top readyQueue2s element
		
		int rq2Time = 1999999999;
		Flight rq2Flight = null;
		for (ATC atc: ATCs.values()) {
			if (!atc.readyQueue2.isEmpty() && (atc.readyQueue2.peek().state == 9 || atc.readyQueue2.peek().state == 19) && atc.readyQueue2.peek().timeTaken[atc.readyQueue2.peek().state] == mainACC.limit) {
				rq1EntrancePriority.add(atc.readyQueue2.peek());
			}
			if (!atc.readyQueue2.isEmpty() && atc.readyQueue2.peek().timeTaken[atc.readyQueue2.peek().state] < rq2Time) {
				rq2Time = atc.readyQueue2.peek().timeTaken[atc.readyQueue2.peek().state];
				rq2Flight = atc.readyQueue2.peek();
			}
			else if (!atc.readyQueue2.isEmpty() && rq2Flight != null && atc.readyQueue2.peek().timeTaken[atc.readyQueue2.peek().state] == rq2Time && atc.readyQueue2.peek().name.compareTo(rq2Flight.name) < 0) {
				rq2Flight = atc.readyQueue2.peek();
			}
		}
		if (rq2Flight != null) {
			eventOrder.add(rq2Flight);
		}
		
		// remaining time of the top waitingQueue2s element
		int wq2Time = 1999999999;
		Flight wq2Flight = null;
		for (ATC atc: ATCs.values()) {
			for (Map.Entry<String, Integer> flight: atc.waitingLine2.entrySet()) {
				if (atc.readyQueue2.isEmpty() && flight.getValue() + flights.get(flight.getKey()).timeTaken[flights.get(flight.getKey()).state + 1] == mainACC.limit) {
					rq1EntrancePriority.add(flights.get(flight.getKey()));				
				}
				if (flight.getValue() < wq2Time) {
					wq2Time = flight.getValue();
					wq2Flight = flights.get(flight.getKey());
				}
				else if (flight.getValue() == wq2Time && wq2Flight != null && flight.getKey().compareTo(wq2Flight.name) < 0) {
					wq2Flight = flights.get(flight.getKey());
				}
			}
		}
		if (wq2Flight != null) {
			eventOrder.add(wq2Flight);
		}

		
	}
	
	
	public int processEvent() {
		fillEventOrder();

		while (!eventOrder.isEmpty()) {
			
			Flight currentFlight = eventOrder.peek();

			

			int state = currentFlight.state;
			int passedTime = currentFlight.timeTaken[currentFlight.state];
			//newcomer check
			if (currentFlight.newComer == true) {
				passedTime = currentFlight.admission - globalTime;
			}
			boolean delay = false;
			// delay incident check
			if (!mainACC.readyQueue1.isEmpty() && passedTime > mainACC.limit) {

				delay = true;
				passedTime = mainACC.limit;
				
				currentFlight = mainACC.readyQueue1.peek();
				state = currentFlight.state;
			}
			
			//the edge case control
			// this is where the priority of entrancte to ACC's ready Queue is handled			
			else if (!mainACC.readyQueue1.isEmpty() && passedTime == mainACC.limit && mainACC.readyQueue1.peek().timeTaken[mainACC.readyQueue1.peek().state] - passedTime > 0) {		
				if (rq1EntrancePriority.isEmpty()) {
					currentFlight = mainACC.readyQueue1.peek();
					delay = true;
				}
				else {
					currentFlight = rq1EntrancePriority.peek();
				}
				state = currentFlight.state;
		
			}

			
			// the chosen least time must be reduced from all other flights' operations
			skipTime(passedTime, currentFlight.name);
			
			// delay case handled here whenever the 30 second limit leads to a problem
			if (delay) {

				mainACC.limit = 30;
				currentFlight.timeTaken[currentFlight.state] -= passedTime;

				mainACC.readyQueue1.remove();
				mainACC.readyQueue1.add(currentFlight);
			}
			
			else if (currentFlight.newComer == true) {

				mainACC.readyQueue1.add(currentFlight);
				currentFlight.newComer = false;		
				flightOrder.remove();
			}
			// from now on it is about what the current flight will do according to its positions/state.
			else if (state == 0 || state == 10) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				mainACC.readyQueue1.remove();
				mainACC.limit = 30;
				mainACC.waitingLine1.put(currentFlight.name, currentFlight.timeTaken[currentFlight.state]);
			}
			else if (state == 2) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				mainACC.readyQueue1.remove();
				mainACC.limit = 30;
				ATCs.get(currentFlight.departure).readyQueue2.add(currentFlight);
			}
			else if (state == 12) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				mainACC.readyQueue1.remove();
				mainACC.limit = 30;
				ATCs.get(currentFlight.arrival).readyQueue2.add(currentFlight);
			}
			else if (state == 1 || state == 11) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				mainACC.waitingLine1.remove(currentFlight.name);
				mainACC.readyQueue1.add(currentFlight);
			}
			else if (state == 3 || state == 5 || state == 7) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				ATCs.get(currentFlight.departure).readyQueue2.remove();
				ATCs.get(currentFlight.departure).waitingLine2.put(currentFlight.name, currentFlight.timeTaken[currentFlight.state]);
				
			}
			else if (state == 13 || state == 15 || state == 17) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				ATCs.get(currentFlight.arrival).readyQueue2.remove();
				ATCs.get(currentFlight.arrival).waitingLine2.put(currentFlight.name, currentFlight.timeTaken[currentFlight.state]);
				
			}
			else if (state == 4 || state == 6 || state == 8) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				ATCs.get(currentFlight.departure).waitingLine2.remove(currentFlight.name);
				ATCs.get(currentFlight.departure).readyQueue2.add(currentFlight);
			}
			else if (state == 14 || state == 16 || state == 18) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				ATCs.get(currentFlight.arrival).waitingLine2.remove(currentFlight.name);
				ATCs.get(currentFlight.arrival).readyQueue2.add(currentFlight);
			}
			else if (state == 9) {
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				ATCs.get(currentFlight.departure).readyQueue2.remove();
				mainACC.readyQueue1.add(currentFlight);
			}
			else if (state == 19) {

				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;

				ATCs.get(currentFlight.arrival).readyQueue2.remove();
				mainACC.readyQueue1.add(currentFlight);
			}
			else if (state == 20){
				currentFlight.timeTaken[currentFlight.state] = 0;
				currentFlight.state++;
				mainACC.readyQueue1.remove();
				mainACC.limit = 30;
			}

			//update time and fill the event order again with the updated flight values.
			globalTime += passedTime;

			fillEventOrder();
			
			
		} 
		
		return globalTime;
		
	}
	private void skipTime(int passedTime, String flightInProcess) {
		
		// no need to update newcomers
		// update readyQueue1
		if (!mainACC.readyQueue1.isEmpty()) {
			Flight rq1TopFlight = mainACC.readyQueue1.peek();
			if (!(rq1TopFlight.name.equals(flightInProcess))) {			
				rq1TopFlight.timeTaken[rq1TopFlight.state] -= passedTime;

				mainACC.limit -= passedTime;

			}
		}
		
		// update readyQueue2s
		for (ATC atc: ATCs.values()) {
			if (!atc.readyQueue2.isEmpty()) {
				Flight rq2TopFlight = atc.readyQueue2.peek();
				if (!(rq2TopFlight.name.equals(flightInProcess))) {
					rq2TopFlight.timeTaken[rq2TopFlight.state] -= passedTime;
				}
			}
			
		}
		// update waitingLine1
		for (Map.Entry<String, Integer> flight: mainACC.waitingLine1.entrySet()) {
			if (!(flight.getKey().equals(flightInProcess))) {
				mainACC.waitingLine1.put(flight.getKey(), flight.getValue() - passedTime);
				flights.get(flight.getKey()).timeTaken[flights.get(flight.getKey()).state] -= passedTime;
			}
		}
		//mainACC.wl1Work(passedTime, flightInProcess);
		//update waitingLine2
		for (ATC atc: ATCs.values()) {
			for (Map.Entry<String, Integer> flight: atc.waitingLine2.entrySet()) {
				if (!(flight.getKey().equals(flightInProcess))) {
					atc.waitingLine2.put(flight.getKey(), flight.getValue() - passedTime);
					flights.get(flight.getKey()).timeTaken[flights.get(flight.getKey()).state] -= passedTime;
				}
			}
		}
		
		
	}
}
