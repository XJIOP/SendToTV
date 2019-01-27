import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

public class BrowseRegistryListener extends DefaultRegistryListener {

    private final CallbackListener callbackListener;

    public BrowseRegistryListener(CallbackListener listener) {
        callbackListener = listener;
    }

    @Override
    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
    	deviceAdd(device);
    }

    @Override
    public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
    	deviceRemove(device);
    }

    @Override
    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
    	deviceAdd(device);
    }

    @Override
    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
    	deviceRemove(device);
    }

    @Override
    public void localDeviceAdded(Registry registry, LocalDevice device) {
    	deviceAdd(device);
    }

    @Override
    public void localDeviceRemoved(Registry registry, LocalDevice device) {
    	deviceRemove(device);
    }

    public void deviceAdd(final Device device) {
    	callbackListener.addItem(device);
    }

    private void deviceRemove(final Device device) {
    	callbackListener.removeItem(device);
    }
    
    public interface CallbackListener {
        void addItem(Device device);
        void removeItem(Device device);
    }
}
