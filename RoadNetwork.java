
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoadNetwork {

    private final HashMap<String, List<Road>> graph;

    public RoadNetwork() {
        graph = new HashMap<>();
    }

    public void addLocation(String location) {
        graph.putIfAbsent(location, new ArrayList<>());
    }

    public void addRoad(
            String source, String destination, int distance) {

        if (distance <= 0) {
            throw new IllegalArgumentException(
                    "Distance must be greater than zero.");
        }

        addLocation(source);
        addLocation(destination);

        graph.get(source).add(new Road(destination, distance));
        graph.get(destination).add(new Road(source, distance));
    }

    public List<Road> getNeighbours(String location) {
        return new ArrayList<>(
                graph.getOrDefault(location, new ArrayList<>()));
    }

    public List<String> getAllLocations() {
        return new ArrayList<>(graph.keySet());
    }

    public boolean containsLocation(String location) {
        return graph.containsKey(location);
    }

    public static class Road {

        private final String destination;
        private final int distance;

        public Road(String destination, int distance) {
            this.destination = destination;
            this.distance = distance;
        }

        public String getDestination() {
            return destination;
        }

        public int getDistance() {
            return distance;
        }
    }

    public Map<String, List<Road>> getGraph() {
        HashMap<String, List<Road>> copy = new HashMap<>();

        for (Map.Entry<String, List<Road>> entry
                : graph.entrySet()) {
            copy.put(entry.getKey(),
                    new ArrayList<>(entry.getValue()));
        }

        return copy;
    }
}