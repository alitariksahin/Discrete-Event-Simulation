import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Project3 {
	
	public static void main(String[] args) {
		int numberOfACCs = 0;
		int numberOfFlights = 0;
		ArrayList<String> accs = new ArrayList<>();
		Map<String, ArrayList<String>> atcs = new HashMap<>();
		Map<String, ArrayList<Flight>> flights = new HashMap<>();
		
		try {
		      File file = new File(args[0]);
		      Scanner scanner = new Scanner(file);
		      
		      int line = 0;
		      while (scanner.hasNextLine()) {
		        String fullLine = scanner.nextLine();
		        String data[] = fullLine.split(" ");
		        if (line == 0) {
		        	numberOfACCs =  Integer.parseInt(data[0]);
		        	numberOfFlights = Integer.parseInt(data[1]);
		        }
		        else if (line > 0 && line <= numberOfACCs) {
		        	String acc = data[0];
		        	ArrayList<String> airports = new ArrayList<String>();
		        	for (int i = 1; i < data.length; i++) {
		        		airports.add(data[i]);
		        	}
		        	atcs.put(acc, airports);	
		        	accs.add(acc);
		        	flights.putIfAbsent(acc, new ArrayList<Flight>());
		        }
		        else if (line > numberOfACCs && line <= numberOfFlights + numberOfACCs) {

		        	String accName = data[2];
		        	int timeTaken[] = new int[21];
		        	int n = 0;
		        	for (int j = 5; j < data.length; j++) {
		        		timeTaken[n] = Integer.parseInt(data[j]);
		        		n++;
		        	}
		        	Flight flight = new Flight(data[1], data[2], data[3], data[4], timeTaken,Integer.parseInt(data[0]));
		        	
		        	flights.get(accName).add(flight);

		        	
		        }
		        line++;
		      }
		      scanner.close();
		    } catch (Exception e) {

		      e.printStackTrace();
		    }
		ArrayList<AirTransportNetwork> airTraffic = new ArrayList<AirTransportNetwork>();
		
		for (String acc: accs) {
			airTraffic.add(new AirTransportNetwork(acc, atcs.get(acc), flights.get(acc)));
		}
		
		try {
		      
		      
			File file = new File(args[1]);
			FileWriter myWriter = new FileWriter(file);
			for (AirTransportNetwork atn: airTraffic) {
			      myWriter.write(atn.mainACC.name + " ");
			      myWriter.write(Integer.toString(atn.processEvent()) + " ");
			      for (String s: atn.getAirportCodes()) {
			    	  myWriter.write(s + " ");
			      }
			      myWriter.write("\n");
			      
			    }
			myWriter.close(); 
			}catch (Exception e) {

			      e.printStackTrace();
			    }
			
		}
		
	}

