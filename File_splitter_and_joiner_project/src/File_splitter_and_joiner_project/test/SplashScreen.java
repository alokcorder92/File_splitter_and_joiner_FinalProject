package File_splitter_and_joiner_project.test;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class SplashScreen extends JWindow {

	private JTextField comment;
	private JProgressBar monitor;

	public SplashScreen(){

		addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				SplashScreen.this.dispose();
			}
		});

		setContentPane(createGUI());
		relocate();
	}

	private JPanel createGUI(){

		ContentPane cp=new ContentPane();

		JLabel label=new JLabel("Loading FileSplitter. Please wait...", JLabel.LEFT);
		label.setFont(new Font("Serif", Font.BOLD, 16));
		label.setForeground(Color.black);

		comment=new JTextField(30);
		comment.setBorder(new EmptyBorder(0,0,0,0));
		comment.setBackground(new Color(204,204,204));
		monitor=new JProgressBar();
		monitor.setMinimum(1);
		monitor.setMaximum(12);

		cp.add(label, 0, 0, 2, 1);
		cp.add(monitor, 2, 0, 2, 1);
		cp.add(comment, 1, 0, 2, 1);

		cp.setBorder(new EtchedBorder());
		return cp;
	}

	private void relocate(){

		setSize(300, 100);
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation( (screenSize.width-300)/2, (screenSize.height-100)/2 );
		setVisible(true);

	}

	public void setComment(final String text, final int value){

		Runnable r=new Runnable(){
			public void run(){
				comment.setText(text);
				monitor.setValue(value);
			}
		};

		try{
			SwingUtilities.invokeAndWait(r);
		}
		catch(Exception x){
			//Ignore
		}

	}

	public static void main(String args[]){
		new SplashScreen();
	}

}

