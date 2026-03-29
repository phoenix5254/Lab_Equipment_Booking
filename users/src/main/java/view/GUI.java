package view;
import model.users.User;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class GUI {
	private GridBagConstraints gbc;
	private Font customFont;
	private User user;
	private JDesktopPane desktopPane;
	public JFrame parentFrame;
	private JFrame registerFrame;
	private JInternalFrame studentFrame;
	private JInternalFrame adminFrame;
	private JInternalFrame technicianFrame;
	private JPanel panel1;
	private JLabel headerLbl;
	private JLabel firstNameLbl;
	private JTextField firstNameText;
	private JLabel lastNameLbl;
	private JTextField lastNameText;
	private JLabel userIDLbl;
	private JTextField userIDText;
	private JLabel passwordLbl;
	private JPasswordField passwordText;
	private JLabel registerLbl;
	private JLabel roleLbl;
	private JComboBox<String> roleCombo;
	private final String[] roles = {"Admin", "Student", "Lecturer"};
	private JButton registerBtn;
	private JButton submitBtn;
	
	public GUI() {
		initializeComponents();
	}
	
	public void initializeComponents() {
		gbc = new GridBagConstraints();
		//DesktopPane Configuration
		desktopPane = new JDesktopPane();
		customFont = new Font("Arial", Font.BOLD, 20);
		
		//PARENT FRAME CONFIG
		//*********************
		parentFrame = new JFrame("CLEB HOME");
		parentFrame.setSize(300, 300);
		parentFrame.setLayout(new GridBagLayout());
		parentFrame.setLocationRelativeTo(null);
		parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gbc.insets = new Insets (10, 10, 10, 10);
		parentFrame.setVisible(true);
		
		//Login 
		headerLbl = new JLabel("Login");
		headerLbl.setFont(customFont);
		gbc.gridx=0;
		gbc.gridy=0;
//		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		parentFrame.add(headerLbl, gbc);
		
		userIDLbl = new JLabel("User ID");
		gbc.gridx=0;
		gbc.gridy=1;//row
		gbc.anchor = GridBagConstraints.WEST;
		parentFrame.add(userIDLbl, gbc);
		
		userIDText = new JTextField(20);
		gbc.gridx=1;
		gbc.gridy=1;
		gbc.anchor = GridBagConstraints.EAST;
		parentFrame.add(userIDText, gbc);
		
		passwordLbl = new JLabel("Password");
		gbc.gridx=0;
		gbc.gridy=2;
		gbc.anchor = GridBagConstraints.WEST;
		parentFrame.add(passwordLbl, gbc);
		
		passwordText = new JPasswordField(20);
		gbc.gridx=1;
		gbc.gridy=2;
		gbc.anchor = GridBagConstraints.EAST;
		parentFrame.add(passwordText, gbc);
		
		submitBtn = new JButton("Submit");
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		parentFrame.add(submitBtn, gbc);
		
		//REGISTER ***********************
		registerLbl = new JLabel("Don't have an account? Register below.");
		gbc.gridx=0;
		gbc.gridy=6;
		gbc.anchor = GridBagConstraints.WEST;
		parentFrame.add(registerLbl, gbc);
		
		registerBtn = new JButton("Register");
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		parentFrame.add(registerBtn, gbc);
		
//		parentFrame.add(desktopPane); //creates a container where the internalframes can be shown
		
		submitBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				user.setUserID(userIDText.getText());
//				Password pword = passwordText
			}
		});
		
		registerBtn.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        registerFrame();
		    }
		});
	}
	
	public void registerFrame() {
		registerFrame = new JFrame();
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(10,10,10,10);
		registerFrame.setLayout(new GridBagLayout());
        registerFrame.setSize(300, 300);
        registerFrame.setLocationRelativeTo(null);
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerFrame.setVisible(true);
        
//        gbc.gridx = 0;
//		gbc.gridy = 8;
//		gbc.gridwidth = 2;

//		parentFrame.add(registerFrame, gbc);
//		parentFrame.revalidate();
//		parentFrame.repaint();
		
        headerLbl = new JLabel("Welcome to CLEB");
		headerLbl.setFont(customFont);
		gbc.gridx=0;
		gbc.gridy=0;
//		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		registerFrame.add(headerLbl, gbc);
		
        userIDLbl = new JLabel("User ID");
		gbc.gridx=0;
		gbc.gridy=1;//row
		gbc.anchor = GridBagConstraints.WEST;
		registerFrame.add(userIDLbl, gbc); 
		
		userIDText = new JTextField(20);
		gbc.gridx=1;
		gbc.gridy=1;
		gbc.anchor = GridBagConstraints.EAST;
		registerFrame.add(userIDText, gbc);
		
        firstNameLbl = new JLabel("First Name");
		gbc.gridx=0;
		gbc.gridy=2;//row
		gbc.anchor = GridBagConstraints.WEST;
		registerFrame.add(firstNameLbl, gbc);
		
		firstNameText = new JTextField(20);
		gbc.gridx=1;
		gbc.gridy=2;
		gbc.anchor = GridBagConstraints.EAST;
		registerFrame.add(firstNameText, gbc);
		
		lastNameLbl = new JLabel("Last Name");
		gbc.gridx=0;
		gbc.gridy=3;//row
		gbc.anchor = GridBagConstraints.WEST;
		registerFrame.add(lastNameLbl, gbc);
		
		lastNameText = new JTextField(20);
		gbc.gridx=1;
		gbc.gridy=3;
		gbc.anchor = GridBagConstraints.EAST;
		registerFrame.add(lastNameText, gbc);
	
		passwordLbl = new JLabel("Password");
		gbc.gridx=0;
		gbc.gridy=4;
		gbc.anchor = GridBagConstraints.WEST;
		registerFrame.add(passwordLbl, gbc);
		
		passwordText = new JPasswordField(20);
		gbc.gridx=1;
		gbc.gridy=4;
		gbc.anchor = GridBagConstraints.EAST;
		registerFrame.add(passwordText, gbc);
		
		roleLbl = new JLabel("Role");
		gbc.gridx=0;
		gbc.gridy=5;//row
		gbc.anchor = GridBagConstraints.WEST;
		registerFrame.add(roleLbl, gbc);
		
		roleCombo = new JComboBox<String>(roles);
		roleCombo.setSize(50, 50);
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.HORIZONTAL;
//		gbc.anchor = GridBagConstraints.WEST;
		registerFrame.add(roleCombo, gbc);
		
		registerBtn = new JButton("Register");
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		registerFrame.add(registerBtn, gbc);
		
//		registerFrame.setBorder(BorderFactory.createTitledBorder(
//                BorderFactory.createEtchedBorder(), "Login Panel"));
		
	}
	
//	
//	public void adminFrame() {
//		adminFrame = new JInternalFrame("Internal Frame", true, true,
//		true, true);
//		adminFrame.setTitle("Admin - CLEB");
//		
//		submitBtn = new JButton("Click me");
//		internalFrameLbl = new JLabel("This is a JInternal Frame ");
//		panel1 = new JPanel();
//		panel1.add(internalFrameLbl);
//		panel1.add(panel1);
//		adminFrame.setVisible(true);
//		adminFrame.add(panel1);
//		adminFrame.add(studentFrame);
//		adminFrame.setSize(300, 300);
//		adminFrame.setVisible(true);
//		}
	
	
	
	// Java Program to demonstrate
	// Simple JDesktopPane
//	import javax.swing.*;
//	import java.awt.*;
//	import java.awt.event.ActionEvent;
//	import java.awt.event.ActionListener;
//
//	// Driver Class
//	public class JDesktopPaneExample {
//	    // main function
//	    public static void main(String[] args) {
//	        // Create and show the main application frame
//	        SwingUtilities.invokeLater(() -> {
//	          
//	            JFrame parentframe = new JFrame("JDesktopPane Example");
//	            parentframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	            parentframe.setSize(800, 600);
//	            
//	              // Create a JDesktopPane to manage internal frames
//	            JDesktopPane desktopPane = new JDesktopPane();
//	            parentframe.add(desktopPane, BorderLayout.CENTER);
//	            
//	              // Create and add two internal frames
//	            createInternalFrame(desktopPane, "Child Frame 1");
//	            createInternalFrame(desktopPane, "Child Frame 2");
//	            
//	              // Display the main application frame
//	            parentframe.setVisible(true);
//	        });
//	    }
//
//	    private static void createInternalFrame(JDesktopPane desktopPane, String title) {
//	        
//	          // Create a new internal frame
//	        JInternalFrame internalFrame = new JInternalFrame(title, true, true, true, true);
//	        internalFrame.setBounds(50, 50, 300, 200);
//	        
//	          // Add a text area with the frame's title as content
//	        JTextArea textArea = new JTextArea(title);
//	        internalFrame.add(textArea);
//
//	        // Create a "Close" button to dispose of the internal frame
//	        JButton closeButton = new JButton("Close");
//	        closeButton.addActionListener(new ActionListener() {
//	            @Override
//	            public void actionPerformed(ActionEvent e) {
//	                
//	                  // Dispose of the internal frame when the "Close" button is clicked
//	                internalFrame.dispose();
//	            }
//	        });
//
//	        // Create a panel for the "Close" button and add it to the internal frame
//	        JPanel buttonPanel = new JPanel();
//	        buttonPanel.add(closeButton);
//	        internalFrame.add(buttonPanel, BorderLayout.SOUTH);
//
//	        // Add the internal frame to the JDesktopPane and make it visible
//	        desktopPane.add(internalFrame);
//	        internalFrame.setVisible(true);
//	    }
//	}
	//REGISTER FRAME
//	 gbc = new GridBagConstraints();
//
//	    registerFrame = new JInternalFrame("CLEB REGISTRATION", true, true, true, true);
//	    registerFrame.setLayout(new GridBagLayout());
//	    registerFrame.setSize(400, 200);
//
//	    gbc.insets = new Insets(10,10,10,10);
//
//	    JLabel headerLbl = new JLabel("Register");
//	    headerLbl.setFont(customFont);
//	    gbc.gridx = 0;
//	    gbc.gridy = 0;
//	    gbc.gridwidth = 2;
//	    registerFrame.add(headerLbl, gbc);
//
//	    JLabel userIDLbl = new JLabel("User ID");
//	    gbc.gridx = 0;
//	    gbc.gridy = 1;
//	    gbc.gridwidth = 1;
//	    registerFrame.add(userIDLbl, gbc);
//
//	    JTextField userIDText = new JTextField(15);
//	    gbc.gridx = 1;
//	    gbc.gridy = 1;
//	    registerFrame.add(userIDText, gbc);
//
//	    // Add to desktop pane (IMPORTANT)
//	    desktopPane.add(registerFrame);
//
//	    registerFrame.setVisible(true);
	public static void main (String[] args) {
		new GUI();
	}
}