
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class RoutePlanner {

    private final RoadNetwork roadNetwork;

    public RoutePlanner(RoadNetwork roadNetwork) {
        this.roadNetwork = roadNetwork;
    }

    public List<String> findShortestRoute(
            String source, String destination) {

        if (!roadNetwork.containsLocation(source)
                || !roadNetwork.containsLocation(destination)) {
            return new ArrayList<>();
        }

        HashMap<String, Integer> distances = new HashMap<>();
        HashMap<String, String> previous = new HashMap<>();

        PriorityQueue<LocationDistance> queue =
                new PriorityQueue<>(
                        Comparator.comparingInt(
                                LocationDistance::getDistance));

        for (String location : roadNetwork.getAllLocations()) {
            distances.put(location, Integer.MAX_VALUE);
        }

        distances.put(source, 0);
        queue.add(new LocationDistance(source, 0));

        while (!queue.isEmpty()) {
            LocationDistance current = queue.poll();

            String currentLocation = current.getLocation();
            int currentDistance = current.getDistance();

            if (currentDistance
                    > distances.get(currentLocation)) {
                continue;
            }

            if (currentLocation.equals(destination)) {
                break;
            }

            for (RoadNetwork.Road road :
                    roadNetwork.getNeighbours(currentLocation)) {

                String nextLocation = road.getDestination();

                int newDistance =
                        currentDistance + road.getDistance();

                if (newDistance < distances.get(nextLocation)) {
                    distances.put(nextLocation, newDistance);
                    previous.put(nextLocation, currentLocation);

                    queue.add(new LocationDistance(
                            nextLocation, newDistance));
                }
            }
        }

        if (distances.get(destination) == Integer.MAX_VALUE) {
            return new ArrayList<>();
        }

        ArrayList<String> route = new ArrayList<>();
        String current = destination;

        while (current != null) {
            route.add(current);
            current = previous.get(current);
        }

        Collections.reverse(route);
        return route;
    }

    public int getRouteDistance(
            String source, String destination) {

        List<String> route =
                findShortestRoute(source, destination);

        if (route.isEmpty()) {
            return -1;
        }

        int totalDistance = 0;

        for (int i = 0; i < route.size() - 1; i++) {
            String current = route.get(i);
            String next = route.get(i + 1);

            int distance = Integer.MAX_VALUE;

            for (RoadNetwork.Road road :
                    roadNetwork.getNeighbours(current)) {

                if (road.getDestination().equals(next)) {
                    distance = Math.min(
                            distance, road.getDistance());
                }
            }

            if (distance == Integer.MAX_VALUE) {
                return -1;
            }

            totalDistance += distance;
        }

        return totalDistance;
    }

    private static class LocationDistance {

        private final String location;
        private final int distance;

        public LocationDistance(
                String location, int distance) {
            this.location = location;
            this.distance = distance;
        }

        public String getLocation() {
            return location;
        }

        public int getDistance() {
            return distance;
        }
    }
}