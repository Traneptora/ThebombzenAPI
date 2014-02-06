package thebombzen.mods.thebombzenapi.installer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.google.common.io.Files;

public class InstallerFrame extends JFrame {
	
	private static final long serialVersionUID = 316486537931642186L;
	private JTextField textField;
	private JRadioButton installClient;
	private JRadioButton installServer;
	private JButton install;
	private String clientDirectory = getMinecraftClientDirectory();
	private String serverDirectory = "";
	
	public static String getMinecraftClientDirectory() throws IOException {
		String name = System.getProperty("os.name");
		if (name.contains("win")){
			return new File(System.getenv("appdata") + "\\.minecraft").getCanonicalPath();
		} else if (name.contains("mac")){
			return new File(System.getProperty("user.home") + "/Library/Application Support/minecraft").getCanonicalPath();
		} else {
			return new File(System.getProperty("user.home") + "/.minecraft").getCanonicalPath();
		}
	}
	
	private void clickedInstallClient(){
		serverDirectory = textField.getText();
		textField.setText(clientDirectory);
	}
	
	private void clickedInstallServer(){
		clientDirectory = textField.getText();
		textField.setText(serverDirectory);
	}
	
	private void install(){
		try {
			install(textField.getText());
		} catch (Exception e){
			JOptionPane.showMessageDialog(this, "Error installing. Install manually.", "Error Installing", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void install(String directory) throws Exception {
		File dir = new File(directory);
		if (!dir.isDirectory()){
			JOptionPane.showMessageDialog(this, "Something's wrong with the given folder. Check spelling and try again.", "Hmmm...", JOptionPane.ERROR_MESSAGE);
			return;
		}
		File modsFolder = new File(directory, "mods");
		modsFolder.mkdir();
		File file = new File(InstallerFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		JarFile jarFile = new JarFile(file);
		if (jarFile.getEntry("thebombzen/mods/thebombzenapi/installer/InstallerFrame.class") == null){
			jarFile.close();
			throw new Exception();
		}
		jarFile.close();
		Files.copy(file, new File(modsFolder, file.getName()));
		JOptionPane.showMessageDialog(this, "Successfully installed ThebombzenAPI!", "Success!", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException {
		new InstallerFrame().setVisible(true);
	}

	public InstallerFrame() throws IOException {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		installClient = new JRadioButton("Install Client");
		installClient.setSelected(true);
		installServer = new JRadioButton("Install Server");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(installClient);
		buttonGroup.add(installServer);
		Box selectBox = Box.createHorizontalBox();
		selectBox.add(installClient);
		selectBox.add(Box.createHorizontalStrut(10));
		selectBox.add(installServer);
		this.add(selectBox);
		
		this.add(Box.createVerticalStrut(10));
		
		JLabel label = new JLabel("Select install folder:");
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(label);
		labelBox.add(Box.createHorizontalGlue());
		this.add(labelBox);
		
		textField = new JTextField();
		textField.setText(clientDirectory);
		this.add(textField);
		
		this.add(Box.createVerticalStrut(10));
		
		install = new JButton("Install");
		Box installBox = Box.createHorizontalBox();
		installBox.add(Box.createHorizontalGlue());
		installBox.add(install);
		installBox.add(Box.createHorizontalGlue());
		this.add(installBox);
		
		installClient.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				clickedInstallClient();
			}
			
		});
		
		installServer.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				clickedInstallServer();
			}
		});
		
		install.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				install();
			}
		});
		
		this.setTitle("Install");
		this.pack();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);
	}
	
}
