package de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;

public class RidgeDetectionOptimizerAssistant {
	
	private JFrame guiFrame;
	JPanel pane_step1;
	ImagePlus target_image;
	int measured_length = -1;
	ArrayList<Roi> selected_filaments;
	int number_global_runs = 20;
	int number_local_runs = 20;
	public RidgeDetectionOptimizerAssistant(ImagePlus target_image) {
		this.target_image = target_image;
		selected_filaments = new ArrayList<Roi>();
	}
	
	public void showGUI(){
		/*
		 * Step 1:
		 * 1. Select line tool (automatically)
		 * "This assistant will guide you through the process of finding best detection parameter set.
		 * As a first step, you have to measure your filament width. Therefore click on a abitary side of your filament, 
		 * keep the mouse pressed and move it to the other side. Then press the button "Measure". If the measured length make sense,
		 * press the button "Next".
		 * 
		 * Step 2:
		 * "Now select in 2-3 images all filaments. The images should represent the range of contrasts. 
		 * At least choose one image with the highest contrast and one with lowest contrast. Lets start with the first image: 
		 * You have to select all filaments in that image. This is done by repeatedly clicking with the mouse. Each click will define a new line segment. 
		 * Double-click when finished, or click in the small box at the starting point. Click the add button if the filament is complete. Repeat this procedure
		 * with the next filament. If all filaments are selected, go to the next image and repeat the whole procedure"
		 * 
		 * Step 3:
		 * "The assistant will now try to find the optimal set of parameters. This can take some time. If it is finished this dialog will be closed and the parameters
		 * will be transfered to main dialog. Press the button "Start" to start the optimization.
		 */
		
		guiFrame= new JFrame("Filament detection assistant");
		guiFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//guiFrame.setMinimumSize(new Dimension(320, 320));
		//guiFrame.setMaximumSize(new Dimension(320, 320));
		
		if(getNumberFilaments(target_image.getOverlay())>0){
			importFilamentAndSettings(target_image.getOverlay());
		}else{
			target_image.setOverlay(null);
		}
		
		guiFrame.setLayout(new GridBagLayout());
		pane_step1 = createPaneStep1();
		updatePanel(pane_step1);
		
		//Display the window.
		guiFrame.pack();
		//guiFrame.setLocationRelativeTo(imp.getWindow());
		guiFrame.setSize(new Dimension(400, 400));
		guiFrame.setVisible(true);
		
		

	}
	
	private int getNumberFilaments(Overlay ov){
		if(ov!=null){
			int n = 0;
			for(int i = 0; i < ov.size(); i++){
				 if (ov.get(i) instanceof PolygonRoi) {
					 n++;
					
				}
			}
			return n;
		}
		return 0;
	}
	
	public void importFilamentAndSettings(Overlay ov){
		
		for(int i = 0; i < ov.size(); i++){
			 
			 if (ov.get(i) instanceof PolygonRoi) {
				 selected_filaments.add(ov.get(i));
				 measured_length = (int) ((PolygonRoi)ov.get(i)).getStrokeWidth();
				 Helical_Picker_.getGUI().updateFilamentWidth(measured_length);
				
			}
		}
	}
	
	public void updatePanel(JPanel panel){
		
		for(int i = 0; i<guiFrame.getComponentCount(); i++){
			if(guiFrame.getComponent(i) instanceof JPanel){
				
				guiFrame.remove(i);
				IJ.log("Remove");
				i--;
			}
		}
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
	//	guiFrame.add(panel,c);
		guiFrame.setContentPane(panel);
		guiFrame.revalidate();
		
	}
	
	public JPanel createPaneStep1(){
		if(IJ.getInstance()!=null){
			IJ.setTool("line");
		}
		GridBagConstraints c = new GridBagConstraints();
		JPanel pane_step1 = new JPanel();
		
		pane_step1.setLayout(new GridBagLayout());
		
		JPanel informationPane = new JPanel();
		informationPane.setBorder(BorderFactory.createTitledBorder("Instructions"));
		informationPane.setLayout(new BorderLayout());
		
		JTextPane textpInformation = new JTextPane();
		textpInformation.setEditable(false);
		textpInformation.setContentType("text/html");
		textpInformation.setText("<html>This assistant will guide you through the process of finding best detection parameter set:<br><br>"
				+ "<b>Measure filament width</b><br><br>"
				+ "1. Measure your filament width. Therefore click on an abitary side of your filament, keep the mouse pressed and move it to the other side. <br><br>"
				+ "2. Then press the button \"Measure\". <br><br>"
				+ "3. If the measured length makes sense, press the button \"Next\"</html>");
		textpInformation.setBackground(guiFrame.getBackground());
		JScrollPane mainJScrollPane = new JScrollPane(textpInformation);
		
		int paneRow = 1;
		//c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		informationPane.add(mainJScrollPane);
		pane_step1.add(informationPane, c);
		paneRow+=1;
		
		String lblTxt = "  Measured length:";
		if(measured_length!=-1){
			lblTxt += (" " + measured_length);
		}
		final JLabel labelMeasuredLength = new JLabel(lblTxt);
		Font font = labelMeasuredLength.getFont();
		labelMeasuredLength.setFont(font.deriveFont(font.getStyle() | Font.BOLD));
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(labelMeasuredLength,c);
		
		paneRow+=1;
		JButton btMeasure = new JButton("Measure");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(btMeasure,c);
		paneRow+=1;
		
		btMeasure.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Line roi =  (Line)target_image.getRoi();
				measured_length = (int)Math.round(roi.getLength());
				Helical_Picker_.getGUI().updateFilamentWidth(measured_length);
				labelMeasuredLength.setText("  Measured length: " + measured_length) ;
				
			}
		});
		
		JButton btCancel = new JButton("Cancel");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(btCancel,c);
		
		btCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				guiFrame.dispose();
				
			}
		});
		
		JButton btNext = new JButton("Next");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = paneRow;
		pane_step1.add(btNext,c);
		paneRow+=1;
		
		btNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel panel2 = createPaneStep2();
				updatePanel(panel2);				
			}
		});
		return pane_step1;
	}
	
	public JPanel createPaneStep2(){
		if(IJ.getInstance() != null){
			IJ.setTool("polyline");
		}
		Line.setWidth(measured_length);
		GridBagConstraints c = new GridBagConstraints();
		JPanel pane_step1 = new JPanel();
		
		pane_step1.setLayout(new GridBagLayout());
		
		JPanel informationPane = new JPanel();
		informationPane.setBorder(BorderFactory.createTitledBorder("Instructions"));
		informationPane.setLayout(new BorderLayout());
		
		JTextPane textpInformation = new JTextPane();
		textpInformation.setEditable(false);
		textpInformation.setContentType("text/html");
		textpInformation.setText("<html><b>Filament selection</b><br><br>"
				+ "Now select in 2-3 images all filaments. The images should represent the range of contrasts. "
				+ "Choose at least one image with the highest contrast and one with lowest contrast. For each selected image do the following steps:<br><br> "
				+ "1. Select all filaments in that image. This is done by repeatedly clicking with the mouse. Each click will define a new filament segment. "
				+ "Double-click when finished, or click in the small box at the starting point.<br><br>"
				+ "2. Click the add button if the filament is complete.<br><br>"
				+ "3. Repeat this procedure with the next filament.<br><br>"
				+ "If all filaments are selected, go to the next image and repeat the whole procedure. After all filaments in each image are selected, "
				+ "press the button \"Next\"</html>");
		textpInformation.setBackground(guiFrame.getBackground());
		JScrollPane mainJScrollPane = new JScrollPane(textpInformation);
		
		int paneRow = 1;
		//c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		informationPane.add(mainJScrollPane);
		pane_step1.add(informationPane, c);
		paneRow+=1;
		
		
		JButton btDelete = new JButton("Delete last filament");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(btDelete,c);
		
		btDelete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(selected_filaments.size()>0){
					//Find last filament with current position
					int to_remove=-1;
					for(int i = 0; i < selected_filaments.size(); i++){
						if(selected_filaments.get(i).getPosition()==target_image.getCurrentSlice()){
							to_remove = i;
						}
					}
					
					if(to_remove!=-1){
						selected_filaments.remove(to_remove);
					}
					Overlay ov = new Overlay();
					for (Roi roi : selected_filaments) {
						ov.add(roi);
					}
					target_image.setOverlay(ov);
				}

				
			}
		});
		
		
		JButton btAdd = new JButton("Add filament");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = paneRow;
		pane_step1.add(btAdd,c);
		paneRow+=1;
		
		btAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Overlay ov = target_image.getOverlay();
				if(ov==null){
					ov = new Overlay();
					target_image.setOverlay(ov);
				}
				Roi r = target_image.getRoi();
				r.setPosition(target_image.getCurrentSlice());
				selected_filaments.add(r);
				ov.add(target_image.getRoi());
				IJ.run(target_image, "Select None", "");

				
			}
		});
		
		JButton btPrevious = new JButton("Previous");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(btPrevious,c);
		
		btPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				target_image.setOverlay(null);
				JPanel panel1 = createPaneStep1();
				updatePanel(panel1);
			}
		});
		
		JButton btNext = new JButton("Next");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = paneRow;
		pane_step1.add(btNext,c);
		paneRow+=1;
		
		btNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Line.setWidth(1);
				JPanel panel3 = createPaneStep3();
				updatePanel(panel3);				
			}
		});

		return pane_step1;
	}	
	
	
public JPanel createPaneStep3(){
		
		GridBagConstraints c = new GridBagConstraints();
		JPanel pane_step1 = new JPanel();
		
		pane_step1.setLayout(new GridBagLayout());
		
		JPanel informationPane = new JPanel();
		informationPane.setBorder(BorderFactory.createTitledBorder("Instructions"));
		informationPane.setLayout(new BorderLayout());
		
		JTextPane textpInformation = new JTextPane();
		textpInformation.setEditable(false);
		textpInformation.setContentType("text/html");
		textpInformation.setText("<html><b>Optimization</b><br><br>The assistant will now try to find the optimal set of parameters. This can take some time. "
				+ "If it is finished this dialog will be closed and the parameters will be transfered to main dialog. Press the button \"Start\" "
				+ "to start the optimization.</html>");
		textpInformation.setBackground(guiFrame.getBackground());
		JScrollPane mainJScrollPane = new JScrollPane(textpInformation);
		
		int paneRow = 1;
		//c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		informationPane.add(mainJScrollPane);
		pane_step1.add(informationPane, c);
		paneRow+=1;
		
		final JProgressBar bar = new JProgressBar(0, 100);
		bar.setVisible(false);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(bar, c);
		paneRow++;
		
		JLabel labelTfNumberGlobalRuns = new JLabel("Number of global runs:");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(labelTfNumberGlobalRuns,c);
		
		final JTextField tfNumberGlobalRuns = new JTextField("20", 3);
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = paneRow;
		pane_step1.add(tfNumberGlobalRuns,c);
		
		tfNumberGlobalRuns.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = tfNumberGlobalRuns.getText();
				if(StringUtils.isNumeric(txt)){
					number_global_runs = Integer.parseInt(txt);
				}
				
			}
		});
		
		paneRow++;
		
		
		JLabel labelTfNumberLocalRuns = new JLabel("Number of local runs:");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(labelTfNumberLocalRuns,c);
		
		final JTextField tfNumberLocalRuns = new JTextField("20", 3);
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = paneRow;
		pane_step1.add(tfNumberLocalRuns,c);
		
		tfNumberLocalRuns.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = tfNumberLocalRuns.getText();
				if(StringUtils.isNumeric(txt)){
					number_local_runs = Integer.parseInt(txt);
				}
				
			}
		});
		
		paneRow++;
		
		JButton btPrevious = new JButton("Previous");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(btPrevious,c);
		
		btPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel panel2 = createPaneStep2();
				updatePanel(panel2);
			}
		});
		
		JButton btStart = new JButton("Start");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = paneRow;
		pane_step1.add(btStart,c);
		paneRow+=1;
		
		btStart.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>(){
					Parallel_Ridge_Optimizer pparallelOptimizer;
					DetectionThresholdRange best_range;
					@Override
					protected Boolean doInBackground() throws Exception {
						bar.setIndeterminate(true);
						bar.setVisible(true);
						int mask_width = Helical_Picker_.getGUI().getFilamentEnhancerContext().getMaskWidth();
						pparallelOptimizer = new Parallel_Ridge_Optimizer();

						best_range = pparallelOptimizer.optimize(target_image, measured_length, mask_width, number_global_runs, number_local_runs);
						return false;
					}

					@Override
					protected void done() {
						Helical_Picker_.getGUI().updateDetectionParameters(best_range);
						guiFrame.dispose();
						target_image.setOverlay(null);
						target_image.repaintWindow();
						super.done();
					}

				};
				worker.execute();			
			}
		});
		
		return pane_step1;
	}	
	
	
	
	public static void main(String[] args) {
		ImagePlus imp = new ImagePlus("test");
		final RidgeDetectionOptimizerAssistant bgui = new RidgeDetectionOptimizerAssistant(imp);
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				bgui.showGUI();
			}
		});
	}

}
