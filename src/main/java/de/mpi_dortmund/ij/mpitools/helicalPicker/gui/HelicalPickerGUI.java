package de.mpi_dortmund.ij.mpitools.helicalPicker.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeListener;

import com.sun.jdi.connect.Connector.StringArgument;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker2_;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.GIF_Reader;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class HelicalPickerGUI implements Runnable {
	
	
	private JFrame guiFrame;
	
	private JPanel basicPane;
	private JPanel detectionPane;
	private JPanel filteringPane;
	private JPanel generalPane;
	private JPanel informationPane;
	JPanel reponseFilterPanel;
	JPanel straightnessFilterPanel;
	
	JCheckBox checkboxShowAdvancedSettings;
	
	JButton buttonApply;
	JButton buttonCancel;
	JButton buttonShowPreview;
	
	JSeparator seperator_upper;
	JSeparator seperator_lower;
	
	JTextArea textpaneInformation;
	
	
	/*
	 *  Detection pane elements
	 */
	JLabel labelFilamentWidth;
	JLabel labelMaskWidth;
	JLabel labelLowerThreshold;
	JLabel labelUpperThreshold;
	
	JTextField textfieldFilamentWidth;
	JTextField textfieldMaskWidth;
	JTextField textfieldLowerThreshold;
	JTextField textfieldUpperThreshold;
	JTextField textfieldBoxSize;
	JTextField textfieldBoxDistance;
	
	/*
	 * Filtering panel elements
	 */
	
	JLabel labelMinNumberBoxes;
	JLabel labelSigmaMinResponse;
	JLabel labelSigmaMaxResponse;
	JLabel labelSensitivity;
	JLabel labelMinStraightness;
	JLabel labelWindowSize;
	JLabel labelOverlappingFactor;
	JLabel labelCustomMask;
	JLabel labelBoxSize;
	JLabel labelBoxDistance;
	JLabel labelPreviewOptions;
	
	JSpinner spinnerSigmaMinResponse;
	JSpinner spinnerSigmaMaxResponse;
	JSpinner spinnerSensitvity;
	JSpinner spinnerMinStraightness;
	JSpinner spinnerOverlappingFactor;
	
	JTextField textfieldMinNumberBoxes;
	JTextField textfieldWindowSize;
	
	JComboBox comboboxCustomMask;
	JComboBox comboboxPreviewOptions;
	
	PreviewActionListener listenerPreview;
	
	public void createAndShowGUI(){
		String version = getClass().getPackage().getImplementationVersion();
		if(version==null){
			version = "";
		}
		guiFrame = new JFrame("STRIPPER V"+version);
		guiFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		guiFrame.setJMenuBar(createMenuBar());
		guiFrame.setMinimumSize(new Dimension(320, 700));
		guiFrame.setMaximumSize(new Dimension(320, 700));
		guiFrame.setLayout(new GridBagLayout());
		/*
		 * Set up the basic pane.
		 */
		initComponents();
		
		addComponentsToPane(basicPane);
		setupListener();
		
		
		
		//guiFrame.setContentPane(basicPane);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		guiFrame.add(basicPane,c);
		//Display the window.
		guiFrame.pack();
		//guiFrame.setLocationRelativeTo(imp.getWindow());
		guiFrame.setVisible(true);
		
	}
	
	public void toggleAdvanced(){
		labelMaskWidth.setVisible(!labelMaskWidth.isVisible());
		textfieldMaskWidth.setVisible(!textfieldMaskWidth.isVisible());

		straightnessFilterPanel.setVisible(!straightnessFilterPanel.isVisible());
		labelOverlappingFactor.setVisible(!labelOverlappingFactor.isVisible());
		spinnerOverlappingFactor.setVisible(!spinnerOverlappingFactor.isVisible());
		labelCustomMask.setVisible(!labelCustomMask.isVisible());
		comboboxCustomMask.setVisible(!comboboxCustomMask.isVisible());
		labelSensitivity.setVisible(!labelSensitivity.isVisible());
		spinnerSensitvity.setVisible(!spinnerSensitvity.isVisible());
	}
	
	public void initComponents(){
		
		basicPane = new JPanel();
		detectionPane = new JPanel();
		filteringPane = new JPanel();
		generalPane = new JPanel();
		
		/*
		 * Basic pane
		 */
		
		seperator_upper = new JSeparator(SwingConstants.HORIZONTAL);
		seperator_lower = new JSeparator(SwingConstants.HORIZONTAL);
		
		informationPane = new JPanel();
		informationPane.setBorder(BorderFactory.createTitledBorder("Manual"));
		informationPane.setLayout(new BorderLayout());

		textpaneInformation = new JTextArea();
		textpaneInformation.setBackground(guiFrame.getBackground());
		textpaneInformation.setLineWrap(true);
		textpaneInformation.setWrapStyleWord(true);
		textpaneInformation.setText("Here are important information");
		
		checkboxShowAdvancedSettings = new JCheckBox("Show advanced settings");
		
		buttonApply = new JButton("Apply");
		buttonCancel = new JButton("Cancel");
		buttonShowPreview = new JButton("Show preview");
		
		/*
		 * Detection pane
		 */
		labelFilamentWidth = new JLabel("Filament width:");
		labelLowerThreshold = new JLabel("Lower threshold:");
		labelUpperThreshold = new JLabel("Upper threshold:");
		labelMaskWidth = new JLabel("Mask width:");
		labelPreviewOptions = new JLabel("Preview options:");
		textfieldFilamentWidth = new JTextField("64", 3);
		textfieldLowerThreshold = new JTextField("0.6", 4);
		textfieldUpperThreshold = new JTextField("1.2", 4);
		textfieldMaskWidth = new JTextField("100",3);
		
		/*
		 * Filtering pane
		 */
		 labelMinNumberBoxes = new JLabel("Min. number of boxes:");
		 labelSigmaMinResponse = new JLabel("Sigma min. response");
		 labelSigmaMaxResponse = new JLabel("Sigma max. response");
		 labelSensitivity = new JLabel("Sensitivity:");
		 labelMinStraightness = new JLabel("Min. straightness:");
		 labelWindowSize = new JLabel("Window size:");
		 labelOverlappingFactor = new JLabel("Allowed box overlapping:");
		 labelCustomMask = new JLabel("Custom mask:");
		 
		
		 spinnerSigmaMinResponse = new JSpinner(new SpinnerNumberModel(0, 0, 4, 0.1));
		 ((JSpinner.DefaultEditor)spinnerSigmaMinResponse.getEditor()).getTextField().setColumns(4);
		 spinnerSigmaMaxResponse = new JSpinner(new SpinnerNumberModel(0, 0, 4, 0.1));
		 ((JSpinner.DefaultEditor)spinnerSigmaMaxResponse.getEditor()).getTextField().setColumns(4);
		 spinnerSensitvity = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.1));
		 ((JSpinner.DefaultEditor)spinnerSensitvity.getEditor()).getTextField().setColumns(4);
		 spinnerMinStraightness = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.1));
		 ((JSpinner.DefaultEditor)spinnerMinStraightness.getEditor()).getTextField().setColumns(4);
		 spinnerOverlappingFactor = new JSpinner(new SpinnerNumberModel(0.5, 0, 0.5, 0.1));
		 ((JSpinner.DefaultEditor)spinnerOverlappingFactor.getEditor()).getTextField().setColumns(4);
		
		 textfieldMinNumberBoxes = new JTextField("7",4);
		 textfieldWindowSize = new JTextField("100",4);
		 
		 Vector<String> items = new Vector<String>();
		 items.add("None");
		 comboboxCustomMask = new JComboBox(items);
		 
		 items = new Vector<String>();
		 items.add("Boxes");
		 items.add("Points");
		 items.add("Response map + Detected lines");
		 comboboxPreviewOptions = new JComboBox(items);
		 
		 /*
		  * General pane
		  */
		 textfieldBoxSize = new JTextField("64");
		 textfieldBoxDistance = new JTextField("10");
		 
		 labelBoxSize = new JLabel("Box size:");
		 labelBoxDistance = new JLabel("Box distance:");
		
	}
	
	public JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem close = new JMenuItem("Close");
		menu.add(close);
		menuBar.add(menu);
		
		menu = new JMenu("Help");
		menuBar.add(menu);
		return menuBar;
	}
	
	public void setupListener(){
		UpdateInformationListener updateInformationListener = new UpdateInformationListener(textpaneInformation);
		updateInformationListener.addDescription(checkboxShowAdvancedSettings, "Shows advanced settings. In most cases, you don't need to change them.");
		updateInformationListener.addDescription(buttonShowPreview, "Shows you the result for the current image."
				+ " Be careful: If you change the filament or mask width the update may be long time.");
		updateInformationListener.addDescription(textfieldFilamentWidth, "The width of your filaments in pixel.");
		buttonShowPreview.addMouseListener(updateInformationListener);
		comboboxPreviewOptions.addMouseListener(updateInformationListener);
		textfieldFilamentWidth.addMouseListener(updateInformationListener);
		
		checkboxShowAdvancedSettings.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleAdvanced();
				
			}
		});
		
		
		buttonCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				guiFrame.dispose();
				
			}
		});
		
		buttonApply.addActionListener(new ApplyActionListener());
		listenerPreview = new PreviewActionListener();
		
		buttonShowPreview.addActionListener(listenerPreview);

	}
	
	public void addComponentsToPane(JPanel basicPane) {
		GridBagConstraints c = new GridBagConstraints();
		JTabbedPane tabs = new JTabbedPane();
		/*
		 * ===
		 * Basic pane
		 * ===
		 */
		basicPane.setLayout(new GridBagLayout());
		int basicPaneRow = 1;
		//c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = basicPaneRow;
		basicPane.add(tabs,c);
		basicPaneRow++;
		
		
		/*
		 * Upper seperator
		 */
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = basicPaneRow;
		basicPane.add(seperator_upper, c);
		basicPaneRow++;
		
		/*
		 * Information pane
		 */
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = basicPaneRow;
		informationPane.add(textpaneInformation);
		basicPane.add(informationPane, c);
		basicPaneRow+=1;
		c.weighty = 0;
		
		/*
		 * Lower seperator
		 */
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = basicPaneRow;
		basicPane.add(seperator_lower, c);
		basicPaneRow+=2;
		
		
		/*
		 * Show advanced settings checkbox
		 */
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = basicPaneRow;
		basicPane.add(checkboxShowAdvancedSettings, c);
		basicPaneRow++;
		
		/*
		 * Show preview
		 */
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = basicPaneRow;
		basicPane.add(labelPreviewOptions, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;

		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = basicPaneRow;
		basicPane.add(comboboxPreviewOptions, c);
		basicPaneRow++;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;

		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = basicPaneRow;
		basicPane.add(buttonShowPreview, c);
		basicPaneRow++;
		
		
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = basicPaneRow;
		c.weightx = 0.5;
		basicPane.add(buttonCancel,c);
		c.gridx = 2;
		basicPane.add(buttonApply,c);
		basicPaneRow++;
		
		
		
		/* 
		 * ============================================
		 * Detection pane
		 * ============================================
		 */
		
		int rowNumber = 0;
		detectionPane.setLayout(new GridBagLayout());
		
		c.fill = GridBagConstraints.HORIZONTAL;
		

		/*
		 * Filament width
		 */
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.7;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		detectionPane.add(labelFilamentWidth, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.3;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = rowNumber;
		detectionPane.add(textfieldFilamentWidth, c);
		rowNumber++;
		
		/*
		 * Mask width
		 */
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		detectionPane.add(labelMaskWidth, c);
		labelMaskWidth.setVisible(false);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = rowNumber;
		detectionPane.add(textfieldMaskWidth, c);
	    textfieldMaskWidth.setVisible(false);
		rowNumber++;
		
		
		/*
		 * Lower threshold
		 */
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		detectionPane.add(labelLowerThreshold, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = rowNumber;
		detectionPane.add(textfieldLowerThreshold, c);
		rowNumber++;
		
		/*
		 * Upper threshold
		 */
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		detectionPane.add(labelUpperThreshold, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = rowNumber;
		detectionPane.add(textfieldUpperThreshold, c);
		rowNumber++;
				
		
		/* 
		 * ============================================
		 * Filtering pane
		 * ============================================
		 */
		
		rowNumber = 1;
		filteringPane.setLayout(new GridBagLayout());
		/*
		 * Min. number of boxes
		 */
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.7;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		filteringPane.add(labelMinNumberBoxes, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.3;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = rowNumber;
		filteringPane.add(textfieldMinNumberBoxes, c);
		rowNumber++;
		
		/*
		 * Response filter
		 */
		reponseFilterPanel = new JPanel();
		reponseFilterPanel.setBorder(BorderFactory.createTitledBorder("Response filter"));
		reponseFilterPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints reponseConstr = new GridBagConstraints();
		reponseConstr.fill = GridBagConstraints.HORIZONTAL;
		reponseConstr.insets = new Insets(0,5,0,5);      //make this component tall
		reponseConstr.weightx = 0;
		reponseConstr.gridwidth = 1;
		reponseConstr.gridx = 0;
		reponseConstr.gridy = 0;
		reponseFilterPanel.add(labelSigmaMinResponse, reponseConstr);
		
		reponseConstr.weightx = 1;
		reponseConstr.gridwidth = 2;
		reponseConstr.gridx = 1;
		reponseConstr.gridy = 0;
		reponseFilterPanel.add(spinnerSigmaMinResponse, reponseConstr);
		
		reponseConstr.gridwidth = 1;
		reponseConstr.gridx = 0;
		reponseConstr.gridy = 1;
		reponseFilterPanel.add(labelSigmaMaxResponse, reponseConstr);
		
		reponseConstr.gridwidth = 2;
		reponseConstr.gridx = 1;
		reponseConstr.gridy = 1;
		reponseFilterPanel.add(spinnerSigmaMaxResponse, reponseConstr);
		
		reponseConstr.gridwidth = 1;
		reponseConstr.gridx = 0;
		reponseConstr.gridy = 2;
		reponseFilterPanel.add(labelSensitivity, reponseConstr);
		labelSensitivity.setVisible(false);
		
		reponseConstr.gridwidth = 2;
		reponseConstr.gridx = 1;
		reponseConstr.gridy = 2;
		reponseFilterPanel.add(spinnerSensitvity, reponseConstr);
		spinnerSensitvity.setVisible(false);

		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		filteringPane.add(reponseFilterPanel, c);
		rowNumber++;
		
		straightnessFilterPanel = new JPanel();
		straightnessFilterPanel.setLayout(new GridBagLayout());
		straightnessFilterPanel.setBorder(BorderFactory.createTitledBorder("Straightness filter"));
		GridBagConstraints straightnessConstr = new GridBagConstraints();
		straightnessConstr.fill = GridBagConstraints.HORIZONTAL;
		straightnessConstr.insets = new Insets(0,5,0,5);      //make this component tall
		straightnessConstr.weightx = 0.7;
		straightnessConstr.gridwidth = 1;
		straightnessConstr.gridx = 0;
		straightnessConstr.gridy = 0;
		straightnessFilterPanel.add(labelMinStraightness, straightnessConstr);
		
		straightnessConstr.weightx = 0.3;
		straightnessConstr.gridwidth = 2;
		straightnessConstr.gridx = 1;
		straightnessConstr.gridy = 0;
		straightnessFilterPanel.add(spinnerMinStraightness, straightnessConstr);
		straightnessFilterPanel.setVisible(false);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		filteringPane.add(straightnessFilterPanel, c);
		rowNumber+=1;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		filteringPane.add(labelOverlappingFactor, c);
		labelOverlappingFactor.setVisible(false);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = rowNumber;
		filteringPane.add(spinnerOverlappingFactor, c);
		spinnerOverlappingFactor.setVisible(false);
		rowNumber+=1;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		filteringPane.add(labelCustomMask, c);
		labelCustomMask.setVisible(false);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = rowNumber;
		filteringPane.add(comboboxCustomMask, c);
		comboboxCustomMask.setVisible(false);
		rowNumber+=1;
		
		
		/* 
		 * ============================================
		 * General pane
		 * ============================================
		 */
		generalPane.setLayout(new GridBagLayout());
		rowNumber = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.7;
		c.weighty = 0.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		generalPane.add(labelBoxSize, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.3;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = rowNumber;
		generalPane.add(textfieldBoxSize, c);
		rowNumber+=1;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = rowNumber;
		generalPane.add(labelBoxDistance, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.gridwidth = 2;
		c.gridx = 1;
		c.weighty =1;
		c.gridy = rowNumber;
		generalPane.add(textfieldBoxDistance, c);
		rowNumber++;
		
	//	filteringPane.setMinimumSize(new Dimension(250, 300));
		tabs.addTab("Detection", detectionPane);
		tabs.addTab("Filtering", filteringPane);
		tabs.addTab("General", generalPane);
	
	}
	
	
	@Override
	public void run() {
		
		
	}
	
	public static void main(String[] args) {

		final HelicalPickerGUI bgui = new HelicalPickerGUI();
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ImageProcessor ip = new ByteProcessor(100, 100);
				bgui.createAndShowGUI();
			}
		});
	}

}