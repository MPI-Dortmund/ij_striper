package de.mpi_dortmund.ij.mpitools.RidgeDetectionOptimizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import de.mpi_dortmund.ij.mpitools.helicalPicker.Helical_Picker_;
import de.mpi_dortmund.ij.mpitools.helicalPicker.FilamentDetector.DetectionThresholdRange;
import de.mpi_dortmund.ij.mpitools.helicalPicker.gui.HelicalPickerGUI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.PolygonRoi;

public class RidgeDetectionOptimizerAssistant {
	
	private JFrame guiFrame;
	JPanel pane_step1;
	ImagePlus target_image;
	int measured_length = 1;
	public RidgeDetectionOptimizerAssistant(ImagePlus target_image) {
		this.target_image = target_image;
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
		
		
		guiFrame.setLayout(new GridBagLayout());
		pane_step1 = createPaneStep1();
		updatePanel(pane_step1);
		
		//Display the window.
		guiFrame.pack();
		//guiFrame.setLocationRelativeTo(imp.getWindow());
		guiFrame.setSize(new Dimension(400, 400));
		guiFrame.setVisible(true);

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
		IJ.setTool("line");
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
			
				
			}
		});
		
		JButton btNext = new JButton("Next");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
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
		IJ.setTool("polyline");
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
		
		
		JButton btAdd = new JButton("Add filament");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = paneRow;
		pane_step1.add(btAdd,c);
		paneRow+=1;
		
		btAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				IJ.run(target_image, "Add Selection...", "");
				IJ.run(target_image, "Select None", "");

				
			}
		});
		
		JButton btNext = new JButton("Next");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
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
		
		JButton btStart = new JButton("Start");
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,5,0,5);      //make this component tall
		c.weightx = 0.0;
		c.weighty = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 2;
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
						int mask_width = Helical_Picker_.getGUI().getFilamentEnhancerContext().getMaskWidth();
						pparallelOptimizer = new Parallel_Ridge_Optimizer();
						int global_runs = 20;
						int local_runs = 20;
						best_range = pparallelOptimizer.optimize(target_image, measured_length, mask_width, global_runs, local_runs);
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
