import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args){
        String filename = "Metrolink_times_linecolour(in) (1).csv";
        Set<String> stationSet = new HashSet<> () ; //For storing station names
        List<String> connections = new ArrayList<>(); //For storing connections
        Map<String, List<Connection>> graph = new HashMap<>(); //For storing the graph tructure

        //Scanner for user input
        Scanner scanner = new Scanner(System.in);

        //User welcome interface
        System.out.println("Welcome to the Metrolink Journey Planner");

        try{
            List<String> lines = Files.readAllLines(Paths.get(filename));
            String currentLine= ""; //Variable to remember which Metroline is currently being read

            for (String line :lines){
                line = line.trim(); //Metroline name and removing extra space

                if (line.isEmpty()) continue; //Skipping empty lines
                
                String[] parts = line.split(","); //Splitting each line into parts

                if (parts[0].trim().equals("From")&& parts[1].trim().equals("To")){
                    continue; //Skipping the header line
                }

                String firstPart = parts[0].trim();//Getting the Metroline name
                String secondPart = (parts.length>1)? parts[1].trim() : ""; //Checking for a part after the line/station name

                boolean isLineNameRow = (secondPart.isEmpty() && !firstPart.equals("From")); //Checking if the row is a line name

                if (isLineNameRow){
                    currentLine = firstPart;
                    System.out.println("Now reading line " + currentLine);
                    continue;
                }

                //Connection Row
                if (parts.length >= 3){
                    //Setting up connection data from the parts array
                    String fromStation = parts[0].trim();
                    String toStation = parts[1].trim();
                    String travelTimeStr = parts[2].trim();

                    double travelTime =Double.parseDouble(travelTimeStr);

                    //Adding station names to the set
                    stationSet.add(fromStation);
                    stationSet.add(toStation);

                    //Storing the connections as a readable string
                    connections.add(fromStation + "->" + toStation + " {" + travelTime +"mins) on" + currentLine);

                    //Adding a connection in both directions
                    graph.computeIfAbsent(fromStation, k -> new ArrayList<>()).add(new Connection(toStation, travelTime,currentLine));
                    graph.computeIfAbsent(toStation, k -> new ArrayList<>()).add(new Connection(fromStation,travelTime, currentLine));
                }
            }

            //User interface Menu
            System.out.println("\nData loaded successfully!");
            System.out.println("Found " + stationSet.size() + " stations in the network. \n");

            while(true){
                System.out.println("\n ~MainMenu~");
                System.out.println("1. List all stations");
                System.out.println("2. Plan a journey");
                System.out.println("3. Exit");
                System.out.println("Enter you choice (1-3): ");

                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1){
                    //Showing all stations
                    System.out.println("\n~Station~");
                    List<String> sortedStations = new ArrayList<>(stationSet);
                    Collections.sort(sortedStations);
                    for (String station : sortedStations){
                        System.out.println("-" + station);
                    }
                    System.out.println("Total: " + sortedStations.size() +" stations");

                } else if(choice == 2 ){
                    //Planning a journey
                    System.out.println("~Plan a Journey~");

                    //Checking if the start station exists and returns them to picking station stage
                    String startStation ="";
                    while (!stationSet.contains(startStation)){
                        System.out.print("Enter your starting station: ");
                        startStation =scanner.nextLine().trim();
                        if (!stationSet.contains(startStation)){
                            System.out.println("Invalid station. Try again.\n");
                        }
                    }

                    //Checking if the end station exists and returns them to picking station stage
                    String endStation = "";
                    while (!stationSet.contains(endStation)){
                        System.out.print("Enter destination station: ");
                        endStation = scanner.nextLine().trim();
                        if (!stationSet.contains(endStation)) {
                            System.out.println("Invalid station Try again.\n ");
                        }
                    }

                    //Checking if the start and end are the same
                    if(startStation.equals(endStation)) {
                        System.out.println("You are already at your destination!");
                    } else{
                        findJourney(graph, startStation, endStation, stationSet);
                    }

                } else if(choice == 3){
                    //Exiting the service/program
                    System.out.println("Thank you for using the Metrolink Planner. Goodbye!");
                    break;

                }else {
                    //Invalid input validation check
                    System.out.println("Invalid choice. Please pick between (1-3)");
                    try{
                        Thread.sleep(3000); //Time for the user to actually see the message
                    } catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                    }

                }
            }

            scanner.close();
            
        } catch (IOException e){
            System.out.println("Error reading file" + e.getMessage());
        }
    }

    public static void findJourney(Map<String, List<Connection>> graph, String start, String end, Set<String>stationSet){
        
        System.out.println("Looking for route from '" + start + "' to '" + end + "' ...");

        //BFS to find the shortest path
        Queue<PathNode> queue = new LinkedList<>();
        Map<String,Double> bestTime = new HashMap<>();

        //Creating the inital path starting with the user's starting station
        PathNode startPath = new PathNode(start, "");

        queue.add(startPath);
        bestTime.put(start, 0.0);

        PathNode shortestPath = null; //Stores the final route

        //Keep exploring paths until the queue is empty
        while(!queue.isEmpty()){
            PathNode currentPath = queue.poll();
            String currentStation = currentPath.stations.get(currentPath.stations.size()-1);
            double currentTime = currentPath.totalTime;

            //If destination reached
            if (currentStation.equals(end)){
                shortestPath = currentPath;
                break;
            }

            //Saving the path if the destination has been reached
            if(graph.containsKey(currentStation)) {
                for (Connection conn : graph.get(currentStation)) {
                    String nextStation = conn.station;
                    double travelTime = conn.time;
                    String nextLine = conn.line;

                    //Calculating total time + 2 min penalty if line changes
                    double newTime = currentTime + travelTime;
                    String currentLine = currentPath.lines.get(currentPath.lines.size()-1);

                    //Starting station will have the next line
                    boolean isFirstMove = currentLine.isEmpty();
                    if (isFirstMove){
                        currentLine = nextLine;
                    }

                    //Adding 2-minute penalty
                    if (!currentLine.equals(nextLine)){
                        newTime += 2;
                    }

                    //Only explore if its a better time to reach the next station
                    if(!bestTime.containsKey(nextStation) || newTime < bestTime.get(nextStation)){
                        bestTime.put(nextStation, newTime);

                        //Creating new path from the current one
                        PathNode newPath = new PathNode(currentPath);
                        newPath.stations.add(nextStation);
                        newPath.lines.add(nextLine);
                        newPath.totalTime = newTime;

                        //Fix the start station's line if this the first move
                        if (isFirstMove){
                            newPath.lines.set(0, nextLine);
                        }

                        queue.add(newPath);
                    }

                }
            }
        }

        //Displaying results
        if (shortestPath == null) {
            //Telling the user BFS failed to find the route
            System.out.println("Sorry, no route found between '" + start + "' and '" + end +"'.");
        } else{

            //Print each station followed by the which line its on
            System.out.println();
            for(int i = 0; i < shortestPath.stations.size(); i++){
                String station = shortestPath.stations.get(i);
                String line = shortestPath.lines.get(i);
                System.out.println(station + " on " + line + " line");
            }

            //Print Summary
            System.out.println("\nOverall Journey time: " + shortestPath.totalTime);

        }

    }        
    
}

//Storing the information about a connection between two stations together
class Connection{
    String station;
    Double time;
    String line;

    Connection(String station, double time, String line){
        this.station = station;
        this.time = time;
        this.line = line;
    }
}

//Storing a path with station names and which line each station is on.
class PathNode{
List<String> stations;
List<String> lines;
double totalTime; //Total travel time including penalties

//Creating a new path starting with one station
PathNode(String startStation, String startLine){
    stations = new ArrayList<>();
    lines = new ArrayList<>();
    stations.add(startStation);
    lines.add(startLine);
    totalTime = 0;
}

//Creating a new path by copying an exisiting
PathNode(PathNode other){
    this.stations = new ArrayList<>(other.stations);
    this.lines = new ArrayList<>(other.lines);
    this.totalTime = other.totalTime;
}

}