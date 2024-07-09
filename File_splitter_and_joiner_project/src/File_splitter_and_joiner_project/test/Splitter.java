package File_splitter_and_joiner_project.test;

import java.io.*;
import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;
import com.sun.java.swing.plaf.windows.*;

public class Splitter extends JFrame {
	private JButton browse, split, stop, exit, help, about, targetButton;
	private JTextField sourceFile, fragmentSize, target;
	private JComboBox metric;
	private JProgressBar monitor;
	private JTextField sourceSize;

	private Worker w;
	private SplashScreen splash;

	public Splitter() {
		super("FileSplitter ver 1.0");

		launchSplashScreen();
		sleep();

		setContentPane(createBaseContainer());

		splash.setComment("Setting mnemonics ...", 6);
		setMnemonics();
		splash.setComment("Setting tooltips ...", 7);
		setToolTips();

		registerActionListeners();
		splash.setComment("Registering WindowListeners ...", 10);
		registerWindowListener();
		splash.setComment("Registering KeyListeners ...", 11);
		splash.setComment("Building container ...", 12);

		setIconImage(new ImageIcon("images/titlebar.png").getImage());
		relocate();
	}

    public Insets getInsets() {
        return new Insets(40,15,10,15);
    }

    private void sleep(){
		try{
			Thread.sleep(2000);
		} catch(InterruptedException x) {
		}
	}

	private void launchSplashScreen(){
		Runnable r=new Runnable(){
			public void run(){
				splash = new SplashScreen();
			}
		};

		try{
			SwingUtilities.invokeAndWait(r);
		} catch(Exception x) {
			System.out.println(x.toString());
		}
	}

	public void setSelectedFolder(final File f){
		if(f != null){
			Runnable r=new Runnable(){
				public void run(){
					target.setText(f.toString());
				}
			};

			try {
				SwingUtilities.invokeLater(r);
			} catch(Exception x) {
			}
		}//if
	}

	public File getSourceFile() throws NoFileSpecifiedException {
		String file=sourceFile.getText();
		if(file.equals("")) {
			throw new NoFileSpecifiedException("Please specify a source file to continue.");
		}
		return new File(file);

	}

	public File getTargetFile() throws NoFileSpecifiedException {
		String file=target.getText();
		if(file.equals("")){
			throw new NoFileSpecifiedException("Please choose a folder for the fragments.");
		}
		return new File(file);
	}

	public void	resetMonitor(){
		Runnable r=new Runnable(){
			public void run(){
				monitor.setValue(0);
			}
		};

		try{
			SwingUtilities.invokeLater(r);
		} catch(Exception x) {
			System.out.println(x.toString());
		}
	}

	public void activateControls(){
		Runnable r=new Runnable(){
			public void run(){
				split.setEnabled(true);
			}
		};

		try{
			SwingUtilities.invokeLater(r);
		} catch(Exception x) {
		}
	}

	public void reset(){
		Runnable r=new Runnable(){
			public void run(){
				sourceSize.setText("");
				fragmentSize.setText("1.4");
				metric.setSelectedIndex(0);
				sourceFile.setText("");
				target.setText("");
				monitor.setValue(0);
			}
		};

		try{
			SwingUtilities.invokeLater(r);
		} catch(Exception x) {
		}
	}

	public void prepareMonitor(int numFragments){
		monitor.setMinimum(0);
		monitor.setMaximum(numFragments * 2);
	}


	public void updateMonitor(final int value){
		Runnable r=new Runnable(){
			public void run(){
				monitor.setValue(value);
			}
		};

		try{
			SwingUtilities.invokeLater(r);
		} catch(Exception x) {
			System.out.println(x.toString());
		}
	}

	public int getFragmentSize() throws InvalidInputException {
		int returnValue=0;

		try{

			double unitSize=Double.parseDouble(fragmentSize.getText());
			returnValue = (int)unitSize;
			switch(metric.getSelectedIndex()){
				case 0:
    				returnValue=(int) (unitSize * 1000 * 1000);
					break;
				case 1:
	    			returnValue=(int) (unitSize * 1000);
					break;
				case 2:
					returnValue=(int) unitSize;
			}
		} catch(NumberFormatException nfe) {
			String m = "The value you entered for \'Fragment size\' is invalid.";
				   m += "\nPlease specify a valid value and try again.";

			throw new InvalidInputException(m);
		}
		return returnValue;
	}

	private void registerWindowListener(){
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
            public void windowOpened(WindowEvent we) {
                try {
                	UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
                    SwingUtilities.updateComponentTreeUI(Splitter.this);
                } catch(Exception e) {
                }
            }
			public void windowClosing(WindowEvent we) {
				String m="This will end your session. Continue ?";
				int option=JOptionPane.showConfirmDialog(Splitter.this, m, "FileSplitter", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(option == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}
		});

	}

	private JPanel createBaseContainer(){
		ContentPane cp = new ContentPane();

		splash.setComment("Building UI ...", 1);
		cp.add(createSourcePanel(), 0, 0, 1, 1);

		splash.setComment("Building UI ...", 2);
		cp.add(createTargetPanel(), 1, 0, 1, 1);

		splash.setComment("Building UI ...", 3);
		cp.add(createMetricPanel(), 2, 0, 2, 1);

		splash.setComment("Building controls ...", 4);
		cp.add(createControlsPanel(), 0, 1, 1, 3);

		splash.setComment("Building controls ...", 5);
		cp.add(createMonitorPanel(), 3, 0, 2, 1);

		return cp;
	}

	private JPanel createTargetPanel(){
		ContentPane cp=new ContentPane();
		cp.add(new JLabel("Save fragments in :", JLabel.LEFT), 0, 0, 1, 1);
		cp.add(target=new JTextField(15), 0, 1, 1, 1);
		cp.fill=GridBagConstraints.NONE;
		cp.anchor=GridBagConstraints.NORTHEAST;
		cp.add(targetButton=new JButton("Select", new ImageIcon("images/select.gif")), 1, 1, 1, 1);

		cp.setBorder(new CompoundBorder(new EmptyBorder(new Insets(2,2,2,2)), new TitledBorder(new EtchedBorder(), "Target")));
		return cp;
	}

	private JPanel createMonitorPanel(){
		monitor=new JProgressBar();
		monitor.setStringPainted(true);

		JPanel cp=new JPanel(new GridLayout(1,1));
		cp.add(monitor);
		cp.setBorder(new CompoundBorder( new TitledBorder(new EtchedBorder(), "Progress"), new EmptyBorder(new Insets(3,3,3,3))));

		return cp;
	}

	private JPanel createControlsPanel(){
		split = new JButton("Split", new ImageIcon("images/split.gif"));
		stop = new JButton("Stop", new ImageIcon("images/stop.gif"));
		exit = new JButton("Exit", new ImageIcon("images/exit.png"));
		help = new JButton("Help", new ImageIcon("images/help.gif"));
		about = new JButton("About", new ImageIcon("images/about.gif"));

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(5, 1, 6, 14));

		p.add(split);
		p.add(stop);
		p.add(exit);
		p.add(help);
		p.add(about);

		p.setBorder(new EmptyBorder(new Insets(8,7,7,7)));
		return p;
	}

	private JPanel createMetricPanel(){
		fragmentSize=new JTextField("1.4", 5);

		metric=new JComboBox();
		metric.addItem("MB");
		metric.addItem("KB");
		metric.addItem("Bytes");
		metric.setPreferredSize(fragmentSize.getPreferredSize());

		ContentPane cp=new ContentPane();

		cp.add(new JLabel("Create fragments of size :", JLabel.LEFT), 0, 0, 1, 1);
		cp.add(fragmentSize, 0, 1, 1, 1);
		cp.add(metric, 0, 2, 1, 1);

		cp.setBorder(new TitledBorder( new EtchedBorder(), "Fragments" ));
		return cp;
	}

	private JPanel createSourcePanel(){
		sourceFile=new JTextField(15);
		sourceFile.addKeyListener(new KeyManager());

		sourceSize=new JTextField(10);
		sourceSize.setEditable(false);
		sourceSize.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
		sourceSize.setBackground(new Color(204, 204, 204));
		JLabel temp=new JLabel();
		sourceSize.setForeground(temp.getForeground());
		sourceSize.setFont(temp.getFont());

		browse=new JButton("Browse", new ImageIcon("images/browse.gif"));
		browse.setMnemonic('B');

		ContentPane cp=new ContentPane();

		cp.add(new JLabel("File :", JLabel.LEFT), 0, 0, 1, 1);
		cp.add(sourceFile, 0, 1, 2, 1);
		cp.add(sourceSize, 1, 0, 2, 1);
		cp.anchor=GridBagConstraints.EAST;
		cp.fill=GridBagConstraints.NONE;
       	cp.add(browse, 1, 2, 1, 1);

		cp.setBorder(new TitledBorder( new EtchedBorder(), "Source" ));
		return cp;
	}

	private void registerActionListeners(){
		splash.setComment("Registering ActionListeners ...", 8);
		ButtonHandler handler=new ButtonHandler();

		browse.addActionListener(handler);
		split.addActionListener(handler);
		stop.addActionListener(handler);
		exit.addActionListener(handler);
		help.addActionListener(handler);
		about.addActionListener(handler);
		targetButton.addActionListener(handler);

		splash.setComment("Registering FocusListeners ....", 9);
		FocusManager fm=new FocusManager();
		fragmentSize.addFocusListener(fm);
		sourceFile.addFocusListener(fm);
		target.addFocusListener(fm);
	}

	private void setMnemonics(){
		split.setMnemonic('S');
		stop.setMnemonic('t');
		exit.setMnemonic('x');
		help.setMnemonic('H');
		about.setMnemonic('A');
		browse.setMnemonic('B');
		targetButton.setMnemonic('e');
	}

	private void setToolTips(){
		browse.setToolTipText("Click to browse for a source file ...");
		split.setToolTipText("Click to start splitting the file selected ...");
		stop.setToolTipText("Click to stop the current process ...");
		exit.setToolTipText("Click to exit FileSplitter...");
		help.setToolTipText("Click to view help information ...");
		about.setToolTipText("Click to view about FileSplitter...");
		targetButton.setToolTipText("Click to choose a target folder ...");
	}

	private void relocate(){
        /*
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = getWidth();
		int h = getHeight();
		setLocation( (screenSize.width - w)/2, (screenSize.height - h)/2 - 25 );
        */
        setBounds(150,100,500,400);
        setResizable(false);

		splash.dispose();
		setVisible(true);
		System.gc();
	}

	public static void main(String[] args){
		new Splitter();
	}


	class ButtonHandler implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == targetButton){
				new DirectoryBrowser(Splitter.this, "FileSplitter - Choose target folder ...", true);
			}

			if(e.getSource() == stop){
				if( w!=null ){
					w.interrupt();
				}
			}

			if(e.getSource() == exit){
				String m="This will end your session. Continue ?";
				int option=JOptionPane.showConfirmDialog(Splitter.this, m, "FileSplitter", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if(option == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}

			if(e.getSource() == split){
				split.setEnabled(false);
				w=new Worker(Splitter.this);
				w.start();
			}

			if(e.getSource() == browse){
				JFileChooser fc=new JFileChooser();
				fc.setDialogTitle("FileSplitter - Choose source file");
				fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
				fc.setPreferredSize( new Dimension(Splitter.this.getSize().width - 8, Splitter.this.getSize().height-100) );

				int option=fc.showDialog(Splitter.this, "Select");

				if(option == JFileChooser.APPROVE_OPTION){
					File fileSelected=fc.getSelectedFile();
					sourceFile.setText(fileSelected.toString());
					sourceSize.setText("Size: " + getFormattedSize(fileSelected));
				}
			}

			if(e.getSource() == about){
				String m = "FileSplitter ver 1.0";
					   m += "\nDeveloped by:\nPrangyasmita, Shridevi & Suchismita";
                       m += "\nIndira Gandhi National Open University\n";
				JOptionPane.showMessageDialog(Splitter.this, m, "About FileSplitter", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("images/smileygold.gif"));
			}

			if(e.getSource() == help){
				String m = "Help\nThis program splits large file\ninto smaller size files\nso that it is convenient to transport\nusing floppy disks\n";
				JOptionPane.showMessageDialog(Splitter.this, m, "FileSplitter Help", JOptionPane.WARNING_MESSAGE, new ImageIcon("images/smileygold.gif"));
			}
		}

		private String getFormattedSize(File file) {

			DecimalFormat formatter=new DecimalFormat("#.##");
			String result="";
			long size=file.length();
			if( (size >= 1024) && (size < 1024*1024) ) {
				result += ( formatter.format( (double)size/1024) )+ " KB";
			} else if( (size >= 1024*1024) && (size < 1024*1024*1024) ) {
				result += ( formatter.format( (double)size / (1024*1024)) )+ " MB";
			} else {
				result += size + " bytes";
			}
			return result;
		}

	}//end ButtonHandler

	class FocusManager extends FocusAdapter {
		public void focusGained(FocusEvent fe){
			((JTextField)fe.getSource()).selectAll();
		}
	}

	class KeyManager extends KeyAdapter {
		private short count=0;

		public void keyPressed(KeyEvent ke){
			if(ke.getKeyCode() == KeyEvent.VK_DELETE){
				sourceSize.setText("");
			}
		}
	}
}// end Splitter

