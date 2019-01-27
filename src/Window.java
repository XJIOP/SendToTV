import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.ListSelectionModel;

import org.fourthline.cling.model.meta.Device;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import java.awt.Color;
import javax.swing.JTextArea;

public class Window {
	
	private DefaultListModel<DeviceDisplay> adapter = new DefaultListModel<>();
	private Dlna dlna;
	
	private JFrame frame;
	private JTextField textField;	
	private JTextArea textAreaMeta;
	private JTextArea textAreaLogs;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					window.frame.setVisible(true);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Window() {		
		
		initialize();
		
		new Thread(new Runnable() {
		    public void run() {
		    	dlna = new Dlna();
		    	dlna.init(adapter, textAreaLogs);
		    }
		}).start();		
	}

	private void initialize() {
		
		frame = new JFrame();
		frame.setTitle("Send video URL to TV (DLNA)");
		frame.setResizable(false);
		frame.setBounds(100, 100, 450, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setLocationRelativeTo(null); 
		
		// input
		textField = new JTextField();
		textField.setToolTipText("");
		textField.setBounds(10, 11, 325, 27);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		// list	
		JList<DeviceDisplay> list = new JList<>(adapter);
		list.setBorder(new LineBorder(Color.LIGHT_GRAY));
		list.setBounds(10, 63, 424, 110);
		frame.getContentPane().add(list);		
		
		// metadata
		textAreaMeta = new JTextArea();
		textAreaMeta.setLineWrap(true);

		JScrollPane textAreaScroll = new JScrollPane(textAreaMeta);
		textAreaScroll.setBorder(new LineBorder(Color.LIGHT_GRAY));
		textAreaScroll.setBounds(10, 196, 424, 110);		
		frame.getContentPane().add(textAreaScroll);
		
        String meta = "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:sec=\"http://www.sec.co.kr/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">\n" +
                "<item id=\"1\" parentID=\"0\" restricted=\"1\">\n" +
                "<upnp:class>object.item.videoItem</upnp:class>\n" +
                "<dc:title>Title</dc:title>\n" +
                "<res protocolInfo=\"http-get:*:video/mp4:*\"></res>\n" +
                "</item>\n" +
                "</DIDL-Lite>";		
		
        textAreaMeta.setText(meta);	
        textAreaMeta.setCaretPosition(0);
		
		// button
		JButton btnNewButton = new JButton("Send");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				String url = textField.getText().trim();
				
				if(url.length() == 0)
					return;
				
				if(!url.startsWith("http://") && !url.startsWith("https://"))
					return;
				
				if(list.isSelectionEmpty())
					return;
			
				DeviceDisplay value = (DeviceDisplay) list.getSelectedValue();
				replaceUrl();
				
				textAreaLogs.append("Send to "+value+": "+url+"\n");
				
				dlna.SendTo(value, url, textAreaMeta.getText());
			}
		});
		btnNewButton.setBounds(345, 10, 89, 28);
		frame.getContentPane().add(btnNewButton);
        
		// logs
		textAreaLogs = new JTextArea();
		textAreaLogs.setEditable(false);
		textAreaLogs.setLineWrap(true);
						
		JScrollPane textAreaLogsScroll = new JScrollPane(textAreaLogs);
		textAreaLogsScroll.setBorder(new LineBorder(Color.LIGHT_GRAY));
		textAreaLogsScroll.setBounds(10, 330, 424, 110);		
		frame.getContentPane().add(textAreaLogsScroll);		
		
		// labels
		JLabel lblNewLabel = new JLabel("Devices");
		lblNewLabel.setBounds(10, 45, 46, 14);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Metadata");
		lblNewLabel_1.setBounds(10, 179, 57, 14);
		frame.getContentPane().add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Logs");
		lblNewLabel_2.setBounds(10, 313, 46, 14);
		frame.getContentPane().add(lblNewLabel_2);
		
		// url key listener
		textField.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				replaceUrl();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});		
		
		// logs listener
		textAreaLogs.getDocument().addDocumentListener(new DocumentListener() {

	        @Override
	        public void removeUpdate(DocumentEvent e) {}

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	        	textAreaLogs.setCaretPosition(textAreaLogs.getText().length());
	        }

	        @Override
	        public void changedUpdate(DocumentEvent arg0) {}
	    });	
	}
	
	private void replaceUrl() {
		String text = textAreaMeta.getText();
		text = text.replaceAll("(?<=>)(.*?)(?=<\\/res>)", textField.getText());
		textAreaMeta.setText(text);		
	}
}
