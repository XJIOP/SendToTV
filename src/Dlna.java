import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JTextArea;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.model.TransportInfo;

public class Dlna {

	private DefaultListModel<DeviceDisplay> mAdapter = new DefaultListModel<>();
	private JTextArea logs;
	private UpnpService upnpService;
	
	public void init(DefaultListModel<DeviceDisplay> adapter, JTextArea textArea) {
		textArea.append("Initialize DLNA...\n");
		
		// disable cling logs
		Logger.getLogger("org.fourthline.cling").setLevel(Level.OFF);
		
		mAdapter = adapter;
		logs = textArea;
		
		upnpService = new UpnpServiceImpl(registryListener);
		
	    // Send a search message to all devices and services, they should respond soon
        upnpService.getControlPoint().search(new STAllHeader());
	}

	/* TODO: DLNA CALLBACK */

    private final BrowseRegistryListener registryListener = new BrowseRegistryListener(new BrowseRegistryListener.CallbackListener() {
        @Override
        public void addItem(Device device) {
        	
        	new Thread(new Runnable() {
        	    public void run() {
        	    	
                    // device not ready
                    if(device == null || mAdapter == null || !device.isFullyHydrated())
                        return;

                    // check AVTransport
                    Service[] services = device.findServices();
                    for(Service service : services) {
                    	
                        // add device only with support AVTransport
                        if(service.getServiceType().getType().equals("AVTransport")) {
                            
                        	DeviceDisplay deviceDisplay = new DeviceDisplay(device);
                        	
                            if(!mAdapter.contains(deviceDisplay)) {
                            	
                            	logs.append("Found device: "+deviceDisplay+" ("+device.getDisplayString()+")\n");
                            	
                            	if(mAdapter != null)
                            		mAdapter.addElement(deviceDisplay);
                            }

                            break;
                        }
                    }
                }
            }).start();
        }

        @Override
        public void removeItem(Device device) {
        	
        	new Thread(new Runnable() {
    			public void run() {
                    if(mAdapter != null) {
                    	
                    	DeviceDisplay deviceDisplay = new DeviceDisplay(device);                    	
                    	mAdapter.removeElement(deviceDisplay);
                    	
                    	logs.append("Device lost: "+deviceDisplay+" ("+device.getDisplayString()+")\n");
                    }
                }
            }).start();
        }
    });		
	
    /* TODO: PLAY */    
    
    public void SendTo(DeviceDisplay deviceDisplay, String url, String meta) {

        final Service service = deviceDisplay.getDevice().findService(new UDAServiceType("AVTransport"));

        ActionCallback setAVTransportURIAction = new SetAVTransportURI(service, url, meta) {

            @Override
            public void success(ActionInvocation invocation) {
            	logs.append("SetAVTransportURI: Success\n");
                super.success(invocation);
                CheckToPlay(service);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            	logs.append("SetAVTransportURI: Failure ("+operation+")\n");
            }
        };

        if(upnpService != null)
            upnpService.getControlPoint().execute(setAVTransportURIAction);
    }

    private void CheckToPlay(final Service service) {

        GetTransportInfo transportInfo = new GetTransportInfo(service) {
            @Override
            public void received(ActionInvocation invocation, TransportInfo transportInfo) {
            	logs.append("GetTransportInfo: Success\n");
            	
                if(transportInfo.getCurrentTransportState().getValue().equals("STOPPED")) {
                    ActionPlay(service);
                    return;
                }
             }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            	logs.append("GetTransportInfo: Failure ("+operation+")\n");
            }
        };
    
        if(upnpService != null)
            upnpService.getControlPoint().execute(transportInfo);       
    }

    private void ActionPlay(Service service) {

        ActionCallback playAction = new Play(service) {

            @Override
            public void success(ActionInvocation invocation) {
            	logs.append("ActionCallback: Success\n");
                super.success(invocation);
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            	logs.append("ActionCallback: Failure ("+operation+")\n");
            }
        };
   
        if(upnpService != null)
            upnpService.getControlPoint().execute(playAction);
    }    
}
