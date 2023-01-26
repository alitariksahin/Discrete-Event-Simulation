
public class Flight {
	int admission;
	boolean newComer = true;

	String name;
	String accName;
	String departure;
	String arrival;
	int state;
	int timeTaken[];
	public Flight(String name, String accName, String departure, String arrival, int timeTaken[], int admission) {
		this.admission = admission;
		this.name = name;
		this.accName = accName;
		this.departure = departure;
		this.arrival = arrival;
		this.timeTaken = timeTaken;
		this.state = 0;
	}	

}
