package com.thebombzen.mods.thebombzenapi.installer;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipError;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.thebombzen.mods.thebombzenapi.Constants;

public class APIInstallerFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		Files.copy(sourceFile.toPath(), destFile.toPath());
	}

	public static String getMinecraftClientDirectory() {
		String name = System.getProperty("os.name");
		if (name.toLowerCase().contains("windows")){
			return new File(System.getenv("appdata") + "\\.minecraft").getAbsolutePath();
		} else if (name.toLowerCase().contains("mac") || name.toLowerCase().contains("osx") || name.toLowerCase().contains("os x")){
			return new File(System.getProperty("user.home") + "/Library/Application Support/minecraft").getAbsolutePath();
		} else {
			return new File(System.getProperty("user.home") + "/.minecraft").getAbsolutePath();
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length > 0 && ("-n".equals(args[0]) || "--no-gui".equals(args[0]))) {
			installNoGui(getMinecraftClientDirectory());
		} else {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					new APIInstallerFrame().setVisible(true);
				}
			});
		}
	}

	private JTextField textField;
	private JRadioButton installClient;
	private JRadioButton installServer;

	private JButton install;

	private String clientDirectory = getMinecraftClientDirectory();

	private String serverDirectory = "";

	public APIInstallerFrame() {
		final APIInstallerFrame frame = this;
		Box superBox = Box.createHorizontalBox();
		superBox.add(Box.createHorizontalStrut(10));
		
		Box content = Box.createVerticalBox();
		
		content.add(Box.createVerticalStrut(10));
		
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
		content.add(selectBox);
		
		content.add(Box.createVerticalStrut(10));
		
		JLabel label = new JLabel("Select minecraft folder:");
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(label);
		labelBox.add(Box.createHorizontalGlue());
		content.add(labelBox);
		
		Box textBox = Box.createHorizontalBox();
		textField = new JTextField();
		textField.setText(clientDirectory);
		textBox.add(textField);
		textBox.add(Box.createHorizontalStrut(10));
		JButton browseButton = new JButton("Browse");
		textBox.add(browseButton);
		content.add(textBox);
		
		content.add(Box.createVerticalStrut(10));
		
		JLabel forgeLabel = new JLabel("Remember to also install Minecraft Forge.");
		Box forgeLabelBox = Box.createHorizontalBox();
		forgeLabelBox.add(forgeLabel);
		forgeLabelBox.add(Box.createHorizontalGlue());
		content.add(forgeLabelBox);
		
		Box forgeLinkBox = Box.createHorizontalBox();
		JLabel forgeLinkLabel = new JLabel("<html><a href=\"http://files.minecraftforge.net/\">Download Minecraft Forge Here</a></html>");
		forgeLinkBox.add(forgeLinkLabel);
		forgeLinkBox.add(Box.createHorizontalGlue());
		content.add(forgeLinkBox);
		
		content.add(Box.createVerticalStrut(10));
		
		install = new JButton("Install ThebombzenAPI");
		Box installBox = Box.createHorizontalBox();
		installBox.add(Box.createHorizontalGlue());
		installBox.add(install);
		installBox.add(Box.createHorizontalGlue());
		content.add(installBox);
		
		content.add(Box.createVerticalStrut(10));
		superBox.add(content);
		superBox.add(Box.createHorizontalStrut(10));
		
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
		
		browseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				JFileChooser jfc = new JFileChooser();
				jfc.setMultiSelectionEnabled(false);
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = jfc.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION){
					textField.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		forgeLinkLabel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent me){
				try {
					Desktop.getDesktop().browse(new URI("http://files.minecraftforge.net/"));
				} catch (IOException e) {
					
				} catch (URISyntaxException e) {
					
				}
			}
		});
		
		this.add(superBox);
		
		this.setTitle("Install ThebombzenAPI");
		this.pack();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);
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

	private static void installNoGui(String directory, String mcVersion) throws IOException {
		File modsFolder = new File(directory, "mods");
		modsFolder.mkdirs();
		File versionFolder = new File(modsFolder, mcVersion);
		versionFolder.mkdirs();
		File file = null;
		try {
			file = new File(APIInstallerFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException uri) {
			throw new Error();
		}
		JarFile jarFile = new JarFile(file);
		if (jarFile.getEntry("com/thebombzen/mods/thebombzenapi/installer/APIInstallerFrame.class") == null){
			jarFile.close();
			throw new ZipError("Could not find self!");
		}
		jarFile.close();
		List<File> testFiles = new ArrayList<File>();
		testFiles.addAll(Arrays.asList(modsFolder.listFiles()));
		testFiles.addAll(Arrays.asList(versionFolder.listFiles()));
		for (File testMod : testFiles) {
			if (testMod
					.getName()
					.matches(
							"^ThebombzenAPI-v\\d+\\.\\d+(\\.\\d+)?-mc\\d+\\.\\d+(\\.\\d+)?\\.(jar|zip)$")) {
				testMod.delete();
			}
		}
		copyFile(file, new File(versionFolder, file.getName()));
	}

	private static void installNoGui(String directory) throws IOException {
		for (String mcVersion : Constants.INSTALL_MC_VERSIONS){
			installNoGui(directory, mcVersion);
		}
	}

	private void install(String directory) throws IOException {
		if (!new File(directory).isDirectory()){
			JOptionPane.showMessageDialog(this, "Something's wrong with the given folder. Check spelling and try again.", "Hmmm...", JOptionPane.ERROR_MESSAGE);
			return;
		}
		installNoGui(directory);
		JOptionPane.showMessageDialog(this, "Successfully installed ThebombzenAPI!", "Success!", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}

}
