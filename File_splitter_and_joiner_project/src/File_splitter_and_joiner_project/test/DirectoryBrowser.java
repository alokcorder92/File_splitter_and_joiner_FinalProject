package File_splitter_and_joiner_project.test;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

public class DirectoryBrowser extends JDialog implements TreeSelectionListener, ActionListener {

    private JTree tree;
    private DefaultTreeModel treeModel;

    private Container container;
    private JFrame parent;
    private JButton accept, dispose;

    private File selectedFolder;

    public DirectoryBrowser(JFrame parent, String title, boolean modal){

        super(parent, title, modal);
        this.parent=parent;

        DefaultMutableTreeNode top=new DefaultMutableTreeNode("My Computer");
        treeModel=new DefaultTreeModel(top);

        File[] roots=File.listRoots();

        for(int i=0; i<roots.length; i++){

            ArrayList subFoldersList=getSubFolders(roots[i]);
            top.add(prepareTreeBranch(roots[i], subFoldersList));

        }
        tree=new JTree(treeModel);
        tree.setShowsRootHandles(true);

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(this);
        tree.putClientProperty("JTree.lineStyle", "Angled");

        setRenderer();

        createBasePanel();

        registerListeners();
        relocate();

    }

    public void relocate() {

        setSize( new Dimension(parent.getPreferredSize().width-50, parent.getPreferredSize().height-50) );
        setLocation(parent.getLocation().x+50, parent.getLocation().y+50);
        setVisible(true);
    }

    private void setRenderer(){

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getDefaultClosedIcon());
        renderer.setClosedIcon(renderer.getDefaultClosedIcon());
        renderer.setOpenIcon(renderer.getDefaultOpenIcon() );
        tree.setCellRenderer(renderer);

    }

    public void valueChanged(TreeSelectionEvent tse){

        TreePath treepath=tree.getSelectionPath();

        if(treepath != null){
            Object obj[]=treepath.getPath();
            String filePath="";

            for(int i=0; i<obj.length; i++){

                if(i == 0){
                    //Ignore
                }
                else if ( i == 1){
                    filePath +=((DefaultMutableTreeNode)obj[i]).toString()+ "\\";
                }
                else{
                    filePath +=((DefaultMutableTreeNode)obj[i]).toString()+"\\"+"\\";
                }

            }

            selectedFolder=new File(filePath);
            DefaultMutableTreeNode parent=(DefaultMutableTreeNode)treepath.getLastPathComponent();

            if (parent.getChildCount() == 0) {
                ArrayList subFolders=getSubFolders(new File(filePath));

                for(int i=0; i< subFolders.size(); i++){
                    String child=(String)subFolders.get(i);
                    addObject(parent, child, false);
                }
            }
        }//if

    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {

            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

            treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

            if (shouldBeVisible) {
                tree.scrollPathToVisible(new TreePath(childNode.getPath()));
            }
            return childNode;

    }

    private DefaultMutableTreeNode prepareTreeBranch(File root, ArrayList children){

        DefaultMutableTreeNode parent=new DefaultMutableTreeNode(root);
        for(int i=0; i<children.size(); i++){
            parent.add(new DefaultMutableTreeNode((String)children.get(i)));
        }

        return parent;
    }

    public ArrayList getSubFolders(File folder){
        ArrayList<String> subFolderList = new ArrayList<String>();
        File[] subFolders = folder.listFiles();

        if(subFolders != null) {
            for(int i=0; i<subFolders.length; i++){
                File file = subFolders[i];
                if(file.isDirectory()) {
                    subFolderList.add(file.getName());
                }

            }//for
        }//if
        return subFolderList;
    }


    private void registerListeners(){
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent we){
                DirectoryBrowser.this.dispose();
                System.gc();
            }
        });
    }

    private void createBasePanel(){

        JPanel p=new JPanel();
        p.setLayout(new BorderLayout());
        setContentPane(p);

        p.add(createTreePanel(), BorderLayout.CENTER);
        p.add(createControlsPanel(), BorderLayout.EAST);

    }

    private JPanel createTreePanel(){

        JPanel p=new JPanel();
        p.setLayout(new BorderLayout());
        JScrollPane sp=new JScrollPane(tree);
        sp.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);

        sp.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
        p.add(sp, BorderLayout.CENTER);

        p.setBorder( new EmptyBorder(new Insets(5,5,5,5)) );
        return p;

    }

    private JPanel createControlsPanel(){
        accept=new JButton("OK");
        accept.setToolTipText("Click to accept the folder selected ...");
        accept.setMnemonic('O');

        dispose=new JButton("Cancel");
        dispose.setToolTipText("Click to dispose the browser ...");
        dispose.setMnemonic('C');

        accept.addActionListener(this);
        dispose.addActionListener(this);

        JPanel p=new JPanel(new GridLayout(2,1,5,5));
        p.add(accept);
        p.add(dispose);

        JPanel tmp=new JPanel();
        tmp.setLayout(new BorderLayout());
        tmp.add(p, BorderLayout.NORTH);
        tmp.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
        return tmp;
    }

    public void actionPerformed(ActionEvent ae){
        if(ae.getSource() == accept){

            ((Splitter)parent).setSelectedFolder(selectedFolder);
            this.dispose();

        }
        if(ae.getSource() == dispose){
            this.dispose();
        }
    }
}

