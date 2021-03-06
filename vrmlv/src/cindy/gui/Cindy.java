package cindy.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import cindy.core.GLDisplay;
import cindy.core.IParentListener;
import cindy.core.LoggerHelper;
import cindy.core.NativesHelper;
import cindy.core.VRMLRenderer;
import cindy.drawable.DisplayOptions;
import cindy.drawable.IDrawable;
import cindy.drawable.NodeSettings;
import cindy.drawable.VRMLDrawableModel;
import cindy.drawable.nodes.VRDNodeFactory;
import cindy.parser.VRMLModel;
import cindy.parser.VRMLParserException;
import cindy.parser.VRNode;
import cindy.parser.nodes.VRWorldInfo;

public class Cindy extends JFrame implements IParentListener{
	
	private static final Logger _LOG = Logger.getLogger(Cindy.class);
	
	static public String appName = "'Cindy' VRML Browser 1.1";
	private GLDisplay renderingWindow;
	private VRMLRenderer renderer;
	
	private boolean exited = false;
	
	private boolean showBoundingBox = true;
	private JCheckBox showBBoxcb = new JCheckBox("Show bounding box", showBoundingBox);
	

	// need to be called when app exits
	public synchronized void shutdown() {
		if (exited){
			return;
		}
		exited = true;
		_LOG.info("exiting...");
		renderer.shutdown();
		new Thread(new Runnable() {
			public void run() {
				while (!renderingWindow.isThreadFinished()) {
					try {
						Thread.sleep(250);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					_LOG.info("waiting for shutdown");
				}
				_LOG.info("VrmlViewer closed");
				_LOG.info("------------------------------------------------------------");
			}
		}, "closing thread").start();
	}
	
	private void readInFile(final String fileChosen){
		_LOG.info("reading file: " + fileChosen);
		final VRMLModel model = new VRMLDrawableModel();
		model.setFileName(fileChosen);
		try{
			is = new ProgressMonitorInputStream(Cindy.this, "Reading file " + fileChosen, new FileInputStream(new File(fileChosen)));
			is.getProgressMonitor().setMillisToDecideToPopup(0);
			is.getProgressMonitor().setMillisToPopup(0);
			is.getProgressMonitor().setProgress(1);					
			
			model.readModel(is, new VRDNodeFactory());
			renderer.setModel(model);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Cindy.this.setTitle(appName + "  /" + fileChosen);
					tree.setModel(new JTreeModelFromVrmlModel(model.getMainGroup()));
				}
			});
		
		} catch (IOException e1) {
			_LOG.warn("IO error while reading data");
			e1.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error in file: " +fileChosen+"\n" + e1+ "\nAborting...");
			is.getProgressMonitor().close();
			
		} catch (VRMLParserException vpe){
			_LOG.error("error while reading data: "+vpe);
			vpe.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error in file: " +fileChosen+"\n" + vpe+ "\nAborting...");
			is.getProgressMonitor().close();
		}
	}
	
		
	private JTree tree = new JTree(new DefaultTreeModel(null));
	private ProgressMonitorInputStream is;	
	
	private JComboBox renderingModeChanger;
	
	private boolean guiBeingUpdate = false;
	
	public void collapse() {		
		int i=1;
		while( i < tree.getRowCount() ) {
		      tree.collapseRow( i );
		      i++;
		 }
	}
	
	void setBBoxVisibilityInNodes(){
		for (IDrawable drawable : renderer.getSelectedNodes().selectedNodes){
			NodeSettings ns=drawable.getNodeSettings();
			if (ns!=null){
				ns.drawBBox = showBoundingBox;
			}
		}
	}
	
	void setAppName(){
		StringBuffer sb=new StringBuffer();
		Iterator iter=renderer.getSelectedNodes().selectedNodes.iterator();
		while(iter.hasNext()){	
			VRNode obj=(VRNode)iter.next();		
			if (obj.name==null)
				sb.append(obj.getNodeInternalName()+"; ");
			else
				sb.append("" + obj.name+"; ");
		}
		setTitle(appName + " / "+sb.toString());
		_LOG.info(sb.toString());
	}
	
	public void objectClicked(DisplayOptions.SelectingOptions selected) {
		_LOG.debug("[clicked]");
		guiBeingUpdate = true;
		tree.clearSelection();
		collapse();		
				
		setAppName();
		Iterator iter=renderer.getSelectedNodes().selectedNodes.iterator();
		while(iter.hasNext()){	
			VRNode obj=(VRNode)iter.next();
			TreePath tp=new TreePath(((JTreeModelFromVrmlModel)tree.getModel()).getPathToRoot(obj));
			
			tree.addSelectionPath(tp);
			tree.makeVisible(tp);
		}
		
		switch(getSelectedRenderingMode()){
			case -2:
				renderingModeChanger.setSelectedItem("NONE");
				break;
			case -1:
				renderingModeChanger.setSelectedIndex(-1);
				break;
			case GL.GL_FILL:
				renderingModeChanger.setSelectedItem("GL_FILL");
				break;
			case GL.GL_LINE:
				renderingModeChanger.setSelectedItem("GL_LINE");
				break;
			case GL.GL_POINT:
				renderingModeChanger.setSelectedItem("GL_POINT");
				break;
			default:
				_LOG.warn("wrong rendering type");									
		}		
		setBBoxVisibilityInNodes();
		guiBeingUpdate = false;
	}
	
	private JButton resetPos = new JButton("Reset position");
	
	int getSelectedRenderingMode(){
		int flag = -2;//NONE
		if (renderer.getSelectedNodes().selectedNodes.isEmpty()){
			return flag;
		}		
    	for (IDrawable node : renderer.getSelectedNodes().selectedNodes){
    		Iterator iter = ((VRNode)node).iterator();
    		while(iter.hasNext()){
    			VRNode nd = (VRNode)iter.next();
    			if (nd instanceof IDrawable) {
    				//_LOG.debug("changind node settings for: " + nd);
    				NodeSettings ds = ((IDrawable)nd).getNodeSettings();		    				
    				if (ds!=null){
    					if (flag==-2){
    						flag = ds.rendMode; 
    					}else{
    						if (flag!=ds.rendMode){
    							return -1;//NONE SELECTED
    						}
    					}
    				}		    				
				}
    		}
    	}
    	return flag;
	}
	
	public Cindy(final String inputWRL){
		super(appName);
		
		showBBoxcb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				showBoundingBox = ((JCheckBox)e.getSource()).isSelected();
				_LOG.info("showBoundingBox = "+showBoundingBox);
				setBBoxVisibilityInNodes();
			}			
		});
		
		
		
		LoggerHelper.initializeLoggingFacility();
		Object ob[] = NativesHelper.checkNativeFiles();
		if (((Boolean)ob[0]== false)){
			System.out.println(ob[1]);
			return ;
		}
		renderer = new VRMLRenderer(this);
		renderingWindow = new GLDisplay(renderer);		
		
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});
		
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		
		resetPos.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				_LOG.debug("[reseting position]");
				renderer.resetPos();
			}
		});
		
		
		tree.setCellRenderer(new VRMLObjectsTreeCellRenderer());		 
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		tree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				
				guiBeingUpdate = true;
				TreePath[] paths = ((JTree)e.getSource()).getSelectionPaths();
				if (paths!=null){
					for (int i=0; i!=paths.length; i++){
						Object obj = paths[i].getLastPathComponent();						
						IDrawable node = (IDrawable)obj;
						if (node instanceof VRWorldInfo ){
							_LOG.info("[World info:");
							
							_LOG.info("title: " +  ((VRWorldInfo)node).title);
						
							for (String s: ((VRWorldInfo)node).info){
								_LOG.info("info: " + s);
							}
							_LOG.info("]");
						}
						
						if (e.isControlDown()){
							renderer.getSelectedNodes().addAnotherNode(node);							
						}else{							
							renderer.getSelectedNodes().selectSingleNode(node);
						}
					}
				}
				
				switch(getSelectedRenderingMode()){
					case -2:
						renderingModeChanger.setSelectedItem("NONE");
					case -1:
						renderingModeChanger.setSelectedIndex(-1);
						break;
					case GL.GL_FILL:
						renderingModeChanger.setSelectedItem("GL_FILL");
						break;
					case GL.GL_LINE:
						renderingModeChanger.setSelectedItem("GL_LINE");
						break;
					case GL.GL_POINT:
						renderingModeChanger.setSelectedItem("GL_POINT");
						break;
					default:
						_LOG.warn("wrong rendering type");									
				}
				
				setAppName();
				setBBoxVisibilityInNodes();
				
				guiBeingUpdate = false;
			}});
		//tree.setRootVisible(false);
	    JScrollPane objectsChangePanel = new JScrollPane(tree);
		
		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				JFileChooser fileDialog = new JFileChooser();
				fileDialog.setFileFilter(new WRLFileFilter());
				fileDialog.setDialogType(JFileChooser.OPEN_DIALOG);
				fileDialog.setDialogTitle("Select .wrl file to open");
				int fd = fileDialog.showOpenDialog(Cindy.this);
				if (fd != JFileChooser.APPROVE_OPTION){
					return ;
				}
				String fileChosen = fileDialog.getCurrentDirectory()
						.getAbsolutePath()
						+ File.separator
						+ fileDialog.getSelectedFile().getName();
				readInFile(fileChosen);				
			}
		});
		fileMenu.add(openMenuItem);
		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				shutdown();
				System.exit(0);
			}
			
		});
		fileMenu.add(exitMenuItem);
		
		menubar.add(fileMenu);
		setJMenuBar(menubar);
		
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel centerPanel = new JPanel(new BorderLayout());				
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);		    		   
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(170);
		centerPanel.add(renderingWindow.getDrawable());
		
		
		JPanel nodeSettingsPanel = new JPanel();
		nodeSettingsPanel.setLayout(new BoxLayout(nodeSettingsPanel,BoxLayout.Y_AXIS));
		nodeSettingsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Node settings"));
		
		
		renderingModeChanger = new JComboBox(new String[]{"NONE", "GL_FILL","GL_LINE","GL_POINT"});
		renderingModeChanger.setSelectedIndex(-1);
		renderingModeChanger.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if (guiBeingUpdate){
					return ;
				}
				JComboBox src = (JComboBox)e.getSource();
				if (src.getSelectedIndex()<0){
		    		return;
		    	}
		    	_LOG.info("updating renderig mode");		    	
		    	String str = (String)src.getSelectedItem();
		    	int flag = 0;
		    	if (str.equals("NONE")) flag = -1;
		    	if (str.equals("GL_FILL")) flag = GL.GL_FILL;
		    	if (str.equals("GL_LINE")) flag = GL.GL_LINE;
		    	if (str.equals("GL_POINT")) flag = GL.GL_POINT;
		    	
		    	for (IDrawable node : renderer.getSelectedNodes().selectedNodes){
		    		Iterator iter = ((VRNode)node).iterator();
		    		while(iter.hasNext()){
		    			VRNode nd = (VRNode)iter.next();
		    			if (nd instanceof IDrawable) {
		    				//_LOG.debug("changind node settings for: " + nd);
		    				NodeSettings ds = ((IDrawable)nd).getNodeSettings();		    				
		    				if (ds!=null){
		    					ds.rendMode = flag;
		    				}		    				
						}
		    		}
		    	}
			}
			
		});
		JPanel renderingMode = new JPanel(new BorderLayout());
		renderingMode.setBorder(BorderFactory.createTitledBorder("rendering mode"));		
		renderingMode.add(renderingModeChanger);	
		
		
		//JCheckBox showBoundingBoxes = new JCheckBox("Show bounding box", true);
		
		nodeSettingsPanel.add(renderingMode);
		JPanel tmp1 = new JPanel(new BorderLayout());
		tmp1.add(showBBoxcb);
		nodeSettingsPanel.add(tmp1);
		JPanel tmpP = new JPanel(new BorderLayout());
		tmpP.add(nodeSettingsPanel, BorderLayout.CENTER);
		tmpP.add(resetPos, BorderLayout.NORTH);		
		leftPanel.add(tmpP, BorderLayout.NORTH);
		
		leftPanel.add(objectsChangePanel, BorderLayout.CENTER);
		add(splitPane);
		
		setSize(800+170,600);
		setVisible(true);
		renderingWindow.start();
		
		//TODO: 
		new Thread(){
			public void run(){
				//String inputWRL = "c:\\__vrml\\2006_01_16\\problem1\\problem1.wrl";
				String s = inputWRL;
				 //s = "C:\\__vrml\\test.wrl";
				//s = "C:/__vrml/IT_orig.wrl";
				s = "C:/__vrml/7.wrl";
				//s = "C:/__vrml/default.wrl";
				//s = "C:/new/colors.wrl";
				//s = "C:\\__vrml\\3.wrl";
				//outputWRL = "C:\\__vrml\\2006_01_16\\CT_res_2.wrl";
				if (s != null) {
					//_LOG.info("input file: " + inputWRL);
					readInFile(s);
				} else {
					_LOG.info("no input file specified");
				}
			}
		}.start();
	}
	
	static public void main(String args[]){
		try {
    		//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
    	} catch (Exception e) {
    		e.printStackTrace();            		
    	} 

		String inputFile = null;
		if (args.length > 0) inputFile = args[0];
		new Cindy(inputFile);
	}

}

class WRLFileFilter extends FileFilter {

	@Override
	public boolean accept(File arg0) {
		String name = arg0.getName().toLowerCase();
		return (arg0.isDirectory() || name.endsWith(".wrl"));
	}

	@Override
	public String getDescription() {
		return "WRL files";
	}
	
}
