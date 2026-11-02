
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class WasteManager {

    private final HashMap<String, Dustbin> dustbins;
    private final ArrayList<CollectionVehicle> vehicles;

    public WasteManager() {
        dustbins = new HashMap<>();
        vehicles = new ArrayList<>();
    }

    public void addDustbin(Dustbin dustbin) {
        dustbins.put(dustbin.getBinId(), dustbin);
    }

    public Dustbin getDustbin(String binId) {
        return dustbins.get(binId);
    }

    public boolean removeDustbin(String binId) {
        return dustbins.remove(binId) != null;
    }

    public List<Dustbin> getAllDustbins() {
        return new ArrayList<>(dustbins.values());
    }

    public void addVehicle(CollectionVehicle vehicle) {
        vehicles.add(vehicle);
    }

    public List<CollectionVehicle> getAllVehicles() {
        return new ArrayList<>(vehicles);
    }

    public List<Dustbin> getPriorityDustbins() {
        PriorityQueue<Dustbin> queue =
                new PriorityQueue<>((a, b) -> {
                    int priorityA = getPriorityValue(a);
                    int priorityB = getPriorityValue(b);

                    if (priorityA != priorityB) {
                        return Integer.compare(priorityA, priorityB);
                    }

                    return Integer.compare(
                            b.getFillLevel(), a.getFillLevel());
                });

        queue.addAll(dustbins.values());

        ArrayList<Dustbin> sortedDustbins = new ArrayList<>();

        while (!queue.isEmpty()) {
            sortedDustbins.add(queue.poll());
        }

        return sortedDustbins;
    }

    private int getPriorityValue(Dustbin dustbin) {
        if (dustbin.getFillLevel() >= 90) {
            return 1;
        }

        if (dustbin.getFillLevel() >= 70) {
            return 2;
        }

        return 3;
    }

    public int getDustbinCount() {
        return dustbins.size();
    }

    public int getVehicleCount() {
        return vehicles.size();
    }

    public int getOverflowCount() {
        int count = 0;

        for (Dustbin dustbin : dustbins.values()) {
            if (dustbin.isOverflowing()) {
                count++;
            }
        }

        return count;
    }
}