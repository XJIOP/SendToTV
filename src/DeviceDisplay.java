import org.fourthline.cling.model.meta.Device;

public class DeviceDisplay {

    private final Device device;

    public DeviceDisplay(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDisplay that = (DeviceDisplay) o;
        return device.equals(that.device);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public String toString() {
        String name = getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null
                        ? getDevice().getDetails().getFriendlyName()
                        : getDevice().getDisplayString();
        // Display a little star while the device is being loaded (see performance optimization earlier)
        return device.isFullyHydrated() ? name : name + " *";
    }
}
