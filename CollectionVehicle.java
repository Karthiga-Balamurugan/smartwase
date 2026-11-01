
public class CollectionVehicle {

    private final String vehicleId;
    private final String driverName;
    private final int capacity;
    private boolean available;

    public CollectionVehicle(
            String vehicleId,
            String driverName,
            int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero.");
        }

        this.vehicleId = vehicleId;
        this.driverName = driverName;
        this.capacity = capacity;
        this.available = true;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getDriverName() {
        return driverName;
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return vehicleId
                + " | Driver: " + driverName
                + " | Capacity: " + capacity
                + " | Status: "
                + (available ? "Available" : "Busy");
    }
}