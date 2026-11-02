
public class Dustbin {

    private final String binId;
    private final String location;
    private int fillLevel;

    public Dustbin(String binId, String location, int fillLevel) {
        this.binId = binId;
        this.location = location;
        setFillLevel(fillLevel);
    }

    public String getBinId() {
        return binId;
    }

    public String getLocation() {
        return location;
    }

    public int getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(int fillLevel) {
        if (fillLevel < 0 || fillLevel > 100) {
            throw new IllegalArgumentException(
                    "Fill level must be between 0 and 100.");
        }

        this.fillLevel = fillLevel;
    }

    public boolean isOverflowing() {
        return fillLevel >= 90;
    }

    public String getPriority() {
        if (fillLevel >= 90) {
            return "HIGH";
        }

        if (fillLevel >= 70) {
            return "MEDIUM";
        }

        return "LOW";
    }

    @Override
    public String toString() {
        return binId + " | " + location
                + " | Fill: " + fillLevel + "%"
                + " | Priority: " + getPriority();
    }
}