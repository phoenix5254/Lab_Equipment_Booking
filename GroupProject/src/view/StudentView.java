package view;
import model.User;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class StudentView extends JFrame{
	private GridBagConstraints gbc;
	private Font customFont;
	private User user;
	private JDesktopPane desktopPane;
	public JFrame parentFrame;
	private JFrame registerFrame;
	private JInternalFrame studentFrame;
	private JInternalFrame adminFrame;
	private JInternalFrame technicianFrame;
	private JPanel panel1, panel2;
	private JLabel headerLbl;
	private JLabel seatsLbl;
	private JLabel	locationLbl;
	private JTextField firstNameText;
	private JLabel lastNameLbl;
	private JTextField equipID;
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
	
	public StudentView (){
	    this.setTitle("STUDENT BOOKING");
	    this.setSize(400, 300);
	    this.setLayout(new GridBagLayout());
	    this.setLocationRelativeTo(null);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	    // Frame constraints
	    GridBagConstraints frameGbc = new GridBagConstraints();
	    frameGbc.insets = new Insets(10,10,10,10);
	    frameGbc.gridx = 0;
	    frameGbc.gridy = 0;
	    frameGbc.weightx = 1.0;
	    frameGbc.weighty = 1.0;
	    frameGbc.fill = GridBagConstraints.BOTH;

	    // Panel
	    panel1 = new JPanel();
	    panel1.setLayout(new GridBagLayout());
	    panel1.setBorder(BorderFactory.createTitledBorder(
	        BorderFactory.createEtchedBorder(), 
	        "SOE Industrial & Mechanical Engineering Lab"
	    ));

	    // Panel constraints
	    GridBagConstraints panelGbc = new GridBagConstraints();
	    panelGbc.insets = new Insets(10,10,10,10);

	    // Seats
	    seatsLbl = new JLabel("24 Seats");
	    panelGbc.gridx = 0;
	    panelGbc.gridy = 0;
	    panel1.add(seatsLbl, panelGbc);

	    // Location
	    locationLbl = new JLabel("Location: Papine");
	    panelGbc.gridx = 1;
	    panelGbc.gridy = 0;
	    panel1.add(locationLbl, panelGbc);

	    // Header
	    headerLbl = new JLabel("Equipments");
	    panelGbc.gridx = 0;
	    panelGbc.gridy = 1;
	    panelGbc.gridwidth = 2;
	    panel1.add(headerLbl, panelGbc);

	    // Equipment ID
	    JTextField equipID = new JTextField("e34664", 10);
	    equipID.setEditable(false);
	    panelGbc.gridx = 0;
	    panelGbc.gridy = 2;
	    panelGbc.gridwidth = 2;
	    panel1.add(equipID, panelGbc);

	    // Add panel to frame
	    this.add(panel1, frameGbc);
	    
	    
	    
	    panel2 = new JPanel();
	    panel2.setLayout(new GridBagLayout());
	    panel2.setBorder(BorderFactory.createTitledBorder(
	        BorderFactory.createEtchedBorder(), 
	        "SOE Industrial & Mechanical Engineering Lab"
	    ));
	    
//	    GridBagConstraints panelGbc = new GridBagConstraints();
//	    panelGbc.insets = new Insets(10,10,10,10);

	    // Seats
	    seatsLbl = new JLabel("24 Seats");
	    panelGbc.gridx = 0;
	    panelGbc.gridy = 0;
	    panel2.add(seatsLbl, panelGbc);

	    // Location
	    locationLbl = new JLabel("Location: Papine");
	    panelGbc.gridx = 1;
	    panelGbc.gridy = 0;
	    panel2.add(locationLbl, panelGbc);

	    // Header
	    headerLbl = new JLabel("Equipments");
	    panelGbc.gridx = 0;
	    panelGbc.gridy = 1;
	    panelGbc.gridwidth = 2;
	    panel2.add(headerLbl, panelGbc);

//	    // Equipment ID
//	    JTextField equipID = new JTextField("e34664", 10);
//	    equipID.setEditable(false);
//	    panelGbc.gridx = 0;
//	    panelGbc.gridy = 2;
//	    panelGbc.gridwidth = 2;
//	    panel1.add(equipID, panelGbc);
//	    panel2.add(equipID, panelGbc);
		
	    this.add(panel2, frameGbc);
	    this.setVisible(true);
	    
//		gbc = new GridBagConstraints();
//		this.setTitle("STUDENT BOOKING");
//		this.setSize(300, 300);
//		this.setLayout(new GridBagLayout());
//		this.setLocationRelativeTo(null);
//		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		gbc.insets = new Insets (10, 10, 10, 10);
//		this.setVisible(true);
//		
//		panel1 = new JPanel();
//		
//		//Adding Components 
//		seatsLbl = new JLabel("24 Seats");
//		gbc.gridx=0;
//		gbc.gridy=1;//row
//		panel1.add(seatsLbl, gbc); 
//		gbc.anchor = GridBagConstraints.WEST;
//		
//		locationLbl = new JLabel("Location: Papine");
//		gbc.gridx=1;
//		gbc.gridy=1;//row
//		gbc.anchor = GridBagConstraints.EAST;
//		panel1.add(locationLbl, gbc);
//		
//		headerLbl = new JLabel("Equipments");
//		gbc.gridx=0;
//		gbc.gridy=3;//row
//		gbc.anchor = GridBagConstraints.WEST;
//		panel1.add(locationLbl, gbc);
//		
//		equipID = new JTextField("e34664");
//		equipID.setEditable(false);
//		gbc.gridx=0;
//		gbc.gridy=4;//row
//		gbc.anchor = GridBagConstraints.EAST;
//		panel1.add(equipID, gbc);
//		
//		//PANEL 1 -- SOE Industrial & Mechanical Engineering Lab	
//		
//		//---Config
//		panel1.setLayout(new GridBagLayout());
//
//		GridBagConstraints panelGbc = new GridBagConstraints();
//		panelGbc.insets = new Insets(10,10,10,10);
//		panelGbc.gridx = 0;
//		panelGbc.gridy = 0;
//		panelGbc.anchor = GridBagConstraints.WEST;
//
////		panel1.add(seatsLbl, panelGbc);
//		panel1.setLayout(new GridBagLayout());
//		gbc.gridx = 0;
//		gbc.gridy = 0;
//		gbc.weightx = 1.0;
//		gbc.weighty = 1.0;
//		gbc.fill = GridBagConstraints.BOTH;
//
//		this.add(panel1, panelGbc);
//		this.revalidate();
//		this.repaint();
//		panel1.setBorder(BorderFactory.createTitledBorder(
//      BorderFactory.createEtchedBorder(), "SOE Industrial & Mechanical Engineering Lab"));

		}
	
	public static void main(String args[]) {
		new StudentView();
	}
}
