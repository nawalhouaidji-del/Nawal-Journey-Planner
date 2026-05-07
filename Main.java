import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Main {
    public static void main(String[] args){
        String filename = "Metrolink_times_linecolour(in) (1).csv";
        Set<String> stationSet = new HashSet<> () ; //For storing station names
        List<String> connections = new ArrayList<>(); //For storing connections

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
                }

            }
            
        } catch (IOException e){
            System.out.println("Error reading file" + e.getMessage());
        }
    }
}