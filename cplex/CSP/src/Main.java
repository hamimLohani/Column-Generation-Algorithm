import cg.ColumnGenerationSolver;
import ilog.concert.IloException;
import model.Flight;
import pricing.PricingProblem;
import util.InputParser;
import util.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        utils.header("Crew Scheduling Problem");
        
        try{
            // load file
            System.out.println("\n===  Step 1: Load Flight File  ===");
            System.out.println("Enter path to flight schedule file (default path: data/flights.csv)");
            System.out.print(":: ");
            String filePath = scanner.nextLine().trim();

            if (filePath.isEmpty()) {
                filePath = "data/flights.csv";
            }

            if (!new File(filePath).exists()) {
                System.err.println("Error: File not found at " + filePath);
                // try relative to project root if running from bin
                if (new File("../" + filePath).exists()) {
                    filePath = "../" + filePath;
                    System.out.println("Found at " + filePath);
                } else {
                    System.out.println(filePath + " doesn't exist. Please fix the file path.");
                    return;
                }
            }

            List<Flight> flights = InputParser.parseFlights(filePath);
            System.out.println("Loaded " + flights.size() + " flights.");

            // taking operational constraints
            System.out.println("\n===  STEP 2: OPERATIONAL CONSTRAINTS  ===");

            String defaultBase = flights.get(0).getBase();
            System.out.println("Enter home base airport (default: " + defaultBase + ")");
            System.out.print(":: ");
            String base = scanner.nextLine().trim();
            if (base.isEmpty()) {
                if (flights.isEmpty()) {
                    System.err.println("Error: No flights loaded, cannot set default base.");
                    return;
                }
                base = defaultBase;
            }

            System.out.println("Maximum duty time (hours, default: 12)");
            System.out.print(":: ");
            String dutyStr = scanner.nextLine().trim();
            double maxDuty = dutyStr.isEmpty() ? 12.0 : Double.parseDouble(dutyStr);

            System.out.println("Maximum flying time (hours, default: 8)");
            System.out.print(":: ");
            String flyStr = scanner.nextLine().trim();
            double maxFly = flyStr.isEmpty() ? 8.0 : Double.parseDouble(flyStr);

            System.out.println("Minimum turnaround time (minutes, default: 40)");
            System.out.print(":: ");
            String turnStr = scanner.nextLine().trim();
            long minTurn = turnStr.isEmpty() ? 40 : Long.parseLong(turnStr);

            System.out.println("Allow overnight duties? (yes/no, default: no)");
            System.out.print(":: ");
            String nightStr = scanner.nextLine().trim().toLowerCase();
            boolean allowOvernight = nightStr.equals("yes") || nightStr.equals("y");

            // taking cost parameters
            System.out.println("\n===  STEP 3: COST PARAMETERS  ===");

            System.out.println("Fixed cost per duty (default: 200)");
            System.out.print(":: ");
            String fixedStr = scanner.nextLine().trim();
            double fixedCost = fixedStr.isEmpty() ? 200.0 : Double.parseDouble(fixedStr);

            System.out.println("Cost per flying hour (default: 100)");
            System.out.print(":: ");
            String hourlyStr = scanner.nextLine().trim();
            double hourlyCost = hourlyStr.isEmpty() ? 100.0 : Double.parseDouble(hourlyStr);

            System.out.println("Night flight penalty (default: 150)");
            System.out.print(":: ");
            String nightPenStr = scanner.nextLine().trim();
            double nightPenalty = nightPenStr.isEmpty() ? 150.0 : Double.parseDouble(nightPenStr);

            System.out.println("Overtime penalty per hour (default: 120)");
            System.out.print(":: ");
            String overStr = scanner.nextLine().trim();
            double overPenalty = overStr.isEmpty() ? 120.0 : Double.parseDouble(overStr);

            System.out.println(flights.get(0));

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
