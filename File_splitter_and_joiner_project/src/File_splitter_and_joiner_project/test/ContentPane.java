package File_splitter_and_joiner_project.test;

import java.awt.*;
import javax.swing.*;

public class ContentPane extends JPanel {

	private GridBagLayout gbLayout;
	private GridBagConstraints gbConstraints;
	
	public int anchor=GridBagConstraints.NORTHWEST;
	public int fill=GridBagConstraints.HORIZONTAL;
	public Insets insets=new Insets(5,5,5,5);

	public ContentPane(){
	
		gbLayout=new GridBagLayout();
		gbConstraints=new GridBagConstraints();
		setLayout(gbLayout);	
		
	}
	
	
	public void add(JComponent c, int row, int col, int width, int height){
	
		gbConstraints.gridx=col;
		gbConstraints.gridy=row;
		gbConstraints.gridwidth=width;
		gbConstraints.gridheight=height;
		gbConstraints.anchor=anchor;
		gbConstraints.fill=fill;
		gbConstraints.insets=insets;
		
		gbLayout.setConstraints(c, gbConstraints);
		
       	add(c);

		resetConstraints();
		
	}
	
	private void resetConstraints(){
	
		fill=GridBagConstraints.HORIZONTAL;
		anchor=GridBagConstraints.NORTHWEST;
		insets=new Insets(5,5,5,5);
		
	}
	
}//end ContentPane

