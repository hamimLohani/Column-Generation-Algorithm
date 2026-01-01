package util;

import model.Flight;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputParser {

    public static List<Flight> parseFlights(String filePath) throws IOException {
        List<Flight> flights = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("---") || line.startsWith("inputId")) {
                    continue; // Skip empty lines, separators, or headers if not standard
                }
                // Specifically handling the header from the prompt example
                if (firstLine && line.toLowerCase().startsWith("flightid")) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 10)
                    continue;

                // FlightID,From,To,DepTime,ArrTime,Duration,Aircraft,Base,FlightCost,Night
                String id = parts[0].trim();
                String from = parts[1].trim();
                String to = parts[2].trim();
                String dep = parts[3].trim();
                String arr = parts[4].trim();
                double duration = Double.parseDouble(parts[5].trim());
                String aircraft = parts[6].trim();
                String base = parts[7].trim();
                double cost = Double.parseDouble(parts[8].trim());
                int night = Integer.parseInt(parts[9].trim());

                flights.add(new Flight(id, from, to, dep, arr, duration, aircraft, base, cost, night));
            }
        }
        return flights;
    }
}
