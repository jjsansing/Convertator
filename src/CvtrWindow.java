/*
 * Copyright (C) 2009 Jim Sansing
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 */

import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The Convertator Window class builds the window and its components for the
 * converting calculator user interface.  When an action is taken that requires
 * file input or calculation, the Convertator File or Engine class is called.
 */
public class CvtrWindow extends JFrame
{
	static final long serialVersionUID = 1;
	static final boolean verbose = false;

	public String textEncoding = "UTF-8";
/** The Convertator class for handling file operations. */
	public CvtrFile cvtrFile = null;
/** The main Convertator engine class for performing conversions and calculations. */
	public CvtrEngine cvtrEngine = null;
/** The base and units of an equation result. */
	public CvtrResult[] equationResult = null;
/** The user preferences selections. */
	public CvtrPrefs userPrefs = null;
/** The result modification window class */
	private CvtrModifyResult modResult = null;
/** The customization window class */
	private CvtrCustom customData = null;
/** The data import window class */
	private CvtrImport importData = null;
/** The message display class */
	private CvtrMessages cvtrMessages = null;
	
	// Information for performing undo on the equation text
	private CvtrHistory historyAction;
	private ArrayList <String> equationHistory = new ArrayList<String>();

	private boolean autoSelect = false;
	// Use to prevent actions from being performed during initialization
	private boolean initUI = true;

	private JFrame cvtrFrame = this;
	private Container cvtrWindow = null;
	private GridBagLayout gbLayout = null; 
	private JButton bResult = null;
	private JTextField txEquation = null;
	private JTextField txResult = null;
	private JButton bUseEquation = null;
	private JComboBox cbEquation = null;
	private JButton bUseResult = null;
	private JComboBox cbResult = null;
	private JButton bOp = null;
	private JComboBox cbOperators = null;	
	private JButton bUnit = null;
	public JComboBox cbUnits = null;
	public JComboBox cbCategories = null;
	private JButton bBase = null;
	public JComboBox cbBases = null;
	private JSpinner spPrecision = null;
	private JCheckBox cbSciNotation = null;
	private JButton bBlank = null;
	private JButton bPeriod = null;
	private JButton bZero = null;
	private JButton bOne = null;
	private JButton bTwo = null;
	private JButton bThree = null;
	private JButton bFour = null;
	private JButton bFive = null;
	private JButton bSix = null;
	private JButton bSeven = null;
	private JButton bEight = null;
	private JButton bNine = null;
	private JButton bA = null;
	private JButton bB = null;
	private JButton bC = null;
	private JButton bD = null;
	private JButton bE = null;
	private JButton bF = null;
	private JButton bEqual = null;
	private JButton bLeft = null;
	private JButton bRight = null;
	private JButton bBackspace = null;
	private JButton bDelete = null;
	private JButton bClear = null;

	private JButton[] buttonList = new JButton[16];

 /**
  * The CvtrWindow constructor instantiates each of CvtrFile and CvtrEngine for
  * getting and handling data, and then creates the user interface window.
  */
	public CvtrWindow() {
		Locale loc = Locale.getDefault();
		Charset ch = Charset.defaultCharset();
		textEncoding = ch.displayName(loc);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Convertator");
		cvtrWindow = getContentPane();
		gbLayout = new GridBagLayout();
		cvtrWindow.setLayout(gbLayout);
		// The messages file handler cannot be set yet
		cvtrMessages = new CvtrMessages(this, null);

		// The Convertator file class must be created before the preference and engine classes. 
		try {
			cvtrEngine = new CvtrEngine();
			cvtrFile = cvtrEngine.cvtrFile;
			if (cvtrEngine.errorMessage != null) {
				cvtrMessages.infoDialog("WARNING", cvtrEngine.errorMessage);
			}
		} catch (Exception err) {
			cvtrMessages.errorDialog("ERROR", "Failed to create Convertator calulating engine:\n  " + err.getMessage());
			System.exit(-1);
		}
		// Set the messages file handler for help dialogs
		cvtrMessages.cvtrFile = cvtrFile;
		userPrefs = new CvtrPrefs(this);
//		userPrefs.getPreferences();

		// Build Convertator user interface
		setJMenuBar(cvtrMenu());
		initLayout();
		initUI = false;
	}

/**
 * Open the Convertator Window
 */
	public void start() {
		pack();
		setVisible(true);
		txEquation.requestFocus();
		cvtrMessages.infoDialog("Welcome", "    Welcome to Convertator\n\n" +
				"To learn how to use Convertator see\n" +
				"the Help menu.  For more information\n" +
				"and support, please visit\n\n" +
				"  https://github.com/jjsansing/Convertator\n");
	}

/**
 * Create the main menu bar.
 * 
 * @return JMenuBar	The menu bar created for the main window.
 */
	private JMenuBar cvtrMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		menuItem = new JMenuItem("Customization", KeyEvent.VK_C);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { dataCustom(); } });
		menu.add(menuItem);

		menuItem = new JMenuItem("Import from Web", KeyEvent.VK_I);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { importWeb(); } });
		menu.add(menuItem);

		menuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { System.exit(0); } });
		menu.add(menuItem);

		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menu);

		menuItem = new JMenuItem("Undo (F2)", KeyEvent.VK_U);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { historyUndo(); } });
		menu.add(menuItem);

		menuItem = new JMenuItem("Clear Previous Eqtn/Rslt", KeyEvent.VK_C);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { clearER(); } });
		menu.add(menuItem);

//		menuItem = new JMenuItem("Preferences", KeyEvent.VK_P);
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) { editPreferences(); } });
//		menu.add(menuItem);

		menu = new JMenu("Constants");
		menuBar.add(menu);
		buildTree(menu, "C");
		
		menu = new JMenu("Equations");
		menuBar.add(menu);
		buildTree(menu, "E");

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);

		menuItem = new JMenuItem("Convertator", KeyEvent.VK_C);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cvtrMessages.helpDialog("HELP", "interface/cvtr.cvi"); } });
		menu.add(menuItem);

		menuItem = new JMenuItem("Main Window", KeyEvent.VK_M);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cvtrMessages.helpDialog("HELP", "interface/main.cvi"); } });
		menu.add(menuItem);

		menuItem = new JMenuItem("Calculations", KeyEvent.VK_A);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cvtrMessages.helpDialog("HELP", "interface/calc.cvi"); } });
		menu.add(menuItem);

//		menuItem = new JMenuItem("Preferences", KeyEvent.VK_P);
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) { helpDialog(cvtrFrame, "HELP", "interface/prefs.cvi"); } });
//		menu.add(menuItem);

		menuItem = new JMenuItem("List of Units", KeyEvent.VK_U);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { getUnits(); } });
		menu.add(menuItem);

		menuItem = new JMenuItem("Errors", KeyEvent.VK_E);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cvtrMessages.helpDialog("HELP", "interface/errors.cvi"); } });
		menu.add(menuItem);

//		menuItem = new JMenuItem("Code License", KeyEvent.VK_L);
//		menuItem.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				cvtrMessages.helpDialog("LICENSE", "interface/codelic.cvi");
//			}
//		});
//		menu.add(menuItem);

		menuItem = new JMenuItem("Data License", KeyEvent.VK_D);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cvtrMessages.helpDialog("LICENSE", "interface/datalic.cvi");
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("About", KeyEvent.VK_B);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = cvtrFile.getTextFile("interface/about.cvi");
				if (s != null)
					cvtrMessages.infoDialog("ABOUT", s);
				else
					cvtrMessages.errorDialog("ERROR", cvtrFile.errorMessage);
			}
		});
		menu.add(menuItem);

		return menuBar;
	}

/**
 * Display units in groups
 */
	public void getUnits() {
		int i, j;
		String unitList = "";

		for (i=0; i < cvtrEngine.UnitCategories.size(); i++) {
			unitList += "\n" + cvtrEngine.UnitCategories.get(i).toString() + "\n";
			for (j=0; j < cvtrEngine.UnitNames.get(i).length; j++) {
				unitList += "  " + cvtrEngine.UnitNames.get(i)[j] + "\n";
			}
		}
		cvtrMessages.helpStringDialog("HELP", unitList);
	}

/**
 * Import data to the application.
 */
	public void dataCustom() {
		if (customData != null) {
			try {
				if (customData.customFrame.isVisible())
					return;
				else {
					customData.stop();
				}
			} catch (NullPointerException e) {
				customData = null;
			}
		}

		customData = new CvtrCustom(this);
		Point p = getLocation();
		customData.setLocation(p.x + 60, p.y + 40);
	}

/**
 * Get a file from the web.
 */
	private void importWeb()
	{
		if (importData != null) {
			try {
				if (importData.importFrame.isVisible())
					return;
				else {
					importData.stop();
				}
			} catch (NullPointerException e) {
				importData = null;
			}
		}
		importData = new CvtrImport(this, userPrefs);
		Point p = cvtrFrame.getLocation();
		importData.setLocation(p.x + 30, p.y + 80);
	}

/**
 * Open a dialog window and when it is closed, update the Convertator Preferences.
 */
	private void editPreferences() {

		userPrefs.setPreferences();
		userPrefs.prefFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				updatePreferences();
			} });
	}

/**
 * Update preferences when user clicks on OK;
 */
	private void updatePreferences() {
if (verbose)
System.out.println("Update Prefs");
	}

/**
 * Clear Equation/Result combobox.
 */
	private void clearER() {

		if (cvtrMessages.yesnoDialog("CLEAR", "Do you want to remove all previous Equations and Results?") == JOptionPane.YES_OPTION) {
			cbEquation.removeAllItems();
			cbResult.removeAllItems();
		}
	}

/**
 * Initialize the main Convertator window.
 */
	private void initLayout() {
		JLabel lBlank = new JLabel("", JLabel.CENTER);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
	// First row:  o [ Equation ] o [ Result ]
		c.insets = new Insets(2,3,2,3);
		txEquation = new JTextField(40);
		txEquation.setEditable(true);
		c.weightx = 0.75; c.gridwidth = 8; c.gridx = 0; c.gridy = 0;
		gbLayout.setConstraints(txEquation, c); cvtrWindow.add(txEquation);
		bResult = new JButton("ChgR");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 8; c.gridy = 0;
		gbLayout.setConstraints(bResult, c); cvtrWindow.add(bResult);
		bResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		txResult = new JTextField("0", 10);
		txResult.setHorizontalAlignment(JTextField.RIGHT);
		txResult.setEditable(false);
		c.weightx = 0.17; c.gridwidth = 2; c.gridx = 9; c.gridy = 0;
		gbLayout.setConstraints(txResult, c); cvtrWindow.add(txResult);

	// Second row:  Sel |--Previous Equations--|  Sel |--Previous Results--|
		bUseEquation = new JButton("UseE");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 0; c.gridy = 1;
		gbLayout.setConstraints(bUseEquation, c); cvtrWindow.add(bUseEquation);
		bUseEquation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		cbEquation = new JComboBox();
		cbEquation.setActionCommand("Equation");
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 0.75; c.gridwidth = 7; c.gridx = 1; c.gridy = 1;
		gbLayout.setConstraints(cbEquation, c); cvtrWindow.add(cbEquation);
		cbEquation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { prevInputEvents(e); } });
		bUseResult = new JButton("UseR");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 8; c.gridy = 1;
		gbLayout.setConstraints(bUseResult, c); cvtrWindow.add(bUseResult);
		bUseResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		cbResult = new JComboBox();
		cbResult.setActionCommand("Result");
		c.weightx = 0.25; c.gridwidth = 2; c.gridx = 9; c.gridy = 1;
		gbLayout.setConstraints(cbResult, c); cvtrWindow.add(cbResult);
		cbResult.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { prevInputEvents(e); } });

	// Third row:  Op --v  . 0 _  A B  Base --v
		c.insets = new Insets(10,3,2,3);
		bBase = new JButton("Base");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 0; c.gridy = 2;
		gbLayout.setConstraints(bBase, c); cvtrWindow.add(bBase);
		bBase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		c.fill = GridBagConstraints.HORIZONTAL;
		cbBases = new JComboBox(cvtrEngine.getBases());
		c.weightx = 0.16; c.gridwidth = 2; c.gridx = 1; c.gridy = 2;
		gbLayout.setConstraints(cbBases, c); cvtrWindow.add(cbBases);
		cbBases.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { setBase(); } });
		bPeriod = new JButton(".");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 3; c.gridy = 2;
		gbLayout.setConstraints(bPeriod, c); cvtrWindow.add(bPeriod);
		bPeriod.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		c.fill = GridBagConstraints.NONE;
		bZero = new JButton("0");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 4; c.gridy = 2;
		gbLayout.setConstraints(bZero, c); cvtrWindow.add(bZero);
		bZero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bBlank = new JButton("_");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 5; c.gridy = 2;
		gbLayout.setConstraints(bBlank, c); cvtrWindow.add(bBlank);
		bBlank.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bA = new JButton("A");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 6; c.gridy = 2;
		gbLayout.setConstraints(bA, c); cvtrWindow.add(bA);
		bA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); }});
		bB = new JButton("B");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 7; c.gridy = 2;
		gbLayout.setConstraints(bB, c); cvtrWindow.add(bB);
		bB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bOp = new JButton("Op");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 8; c.gridy = 2;
		gbLayout.setConstraints(bOp, c); cvtrWindow.add(bOp);
		bOp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		cbOperators = new JComboBox(cvtrEngine.getOperators());
		c.weightx = 0.16; c.gridwidth = 2; c.gridx = 9; c.gridy = 2;
		gbLayout.setConstraints(cbOperators, c); cvtrWindow.add(cbOperators);
		cbOperators.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { setOperator(); } });

	// Fourth row:  Unit --v  1 2 3  C D  Prec 0|
		c.insets = new Insets(2,3,2,3);
		bUnit = new JButton("Unit");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 0; c.gridy = 3;
		gbLayout.setConstraints(bUnit, c); cvtrWindow.add(bUnit);
		bUnit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		c.fill = GridBagConstraints.HORIZONTAL;
		cbUnits = new JComboBox((String[])cvtrEngine.UnitNames.get(0));
		c.weightx = 0.16; c.gridwidth = 2; c.gridx = 1; c.gridy = 3;
		gbLayout.setConstraints(cbUnits, c); cvtrWindow.add(cbUnits);
		cbUnits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { setUnit(); } });
		c.fill = GridBagConstraints.NONE;
		bOne = new JButton("1");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 3; c.gridy = 3;
		gbLayout.setConstraints(bOne, c); cvtrWindow.add(bOne);
		bOne.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bTwo = new JButton("2");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 4; c.gridy = 3;
		gbLayout.setConstraints(bTwo, c); cvtrWindow.add(bTwo);
		bTwo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bThree = new JButton("3");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 5; c.gridy = 3;
		gbLayout.setConstraints(bThree, c); cvtrWindow.add(bThree);
		bThree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bC = new JButton("C");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 6; c.gridy = 3;
		gbLayout.setConstraints(bC, c); cvtrWindow.add(bC);
		bC.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bD = new JButton("D");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 7; c.gridy = 3;
		gbLayout.setConstraints(bD, c); cvtrWindow.add(bD);
		bD.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		c.anchor = GridBagConstraints.CENTER;
		JLabel lPrec = new JLabel("Prec", JLabel.CENTER);
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 8; c.gridy = 3;
		gbLayout.setConstraints(lPrec, c); cvtrWindow.add(lPrec);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		SpinnerListModel sl = new SpinnerListModel(cvtrEngine.getPrecision());
		spPrecision = new JSpinner(sl);
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 9; c.gridy = 3;
		gbLayout.setConstraints(spPrecision, c); cvtrWindow.add(spPrecision);
		spPrecision.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int i = cvtrEngine.setPrecision(spPrecision.getValue().toString());
				spPrecision.setValue("" + i);
				Cursor ieCursor = txEquation.getCursor();
				txEquation.setCursor(ieCursor);
				txEquation.requestFocus();
			} });
		cbSciNotation = new JCheckBox("SciNot");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 10; c.gridy = 3;
		gbLayout.setConstraints(cbSciNotation, c); cvtrWindow.add(cbSciNotation);
		cbSciNotation.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				cvtrEngine.scientificNotation = cbSciNotation.isSelected();
			} });
		lBlank = new JLabel("", JLabel.LEFT);
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 10; c.gridy = 3;
		gbLayout.setConstraints(lBlank, c); cvtrWindow.add(lBlank);

	// Fifth row:  Type --v  4 5 6  E F  Clr
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel lType = new JLabel("Type", JLabel.CENTER);
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 0; c.gridy = 4;
		gbLayout.setConstraints(lType, c); cvtrWindow.add(lType);
		cbCategories = new JComboBox(cvtrEngine.UnitCategories.toArray());
		c.weightx = 0.16; c.gridwidth = 2; c.gridx = 1; c.gridy = 4;
		gbLayout.setConstraints(cbCategories, c); cvtrWindow.add(cbCategories);
		cbCategories.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { setCategory(); } });
		c.fill = GridBagConstraints.NONE;
		bFour = new JButton("4");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 3; c.gridy = 4;
		gbLayout.setConstraints(bFour, c); cvtrWindow.add(bFour);
		bFour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bFive = new JButton("5");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 4; c.gridy = 4;
		gbLayout.setConstraints(bFive, c); cvtrWindow.add(bFive);
		bFive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bSix = new JButton("6");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 5; c.gridy = 4;
		gbLayout.setConstraints(bSix, c); cvtrWindow.add(bSix);
		bSix.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bE = new JButton("E");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 6; c.gridy = 4;
		gbLayout.setConstraints(bE, c); cvtrWindow.add(bE);
		bE.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bF = new JButton("F");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 7; c.gridy = 4;
		gbLayout.setConstraints(bF, c); cvtrWindow.add(bF);
		bF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bEqual = new JButton("=");
		c.weightx = 0.16; c.gridwidth = 1; c.gridx = 9; c.gridy = 4;
		gbLayout.setConstraints(bEqual, c); cvtrWindow.add(bEqual);
		bEqual.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });

	// Sixth row:  <- 7 8 9  ->  =
		lBlank = new JLabel("", JLabel.CENTER);
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 0; c.gridy = 5;
		gbLayout.setConstraints(lBlank, c); cvtrWindow.add(lBlank);
		// Keep buttons to minimum size
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.LINE_END;
		bBackspace = new JButton("Bks");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 1; c.gridy = 5;
		gbLayout.setConstraints(bBackspace, c); cvtrWindow.add(bBackspace);
		bBackspace.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		bDelete = new JButton("Del");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 2; c.gridy = 5;
		gbLayout.setConstraints(bDelete, c); cvtrWindow.add(bDelete);
		bDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		bSeven = new JButton("7");
		c.anchor = GridBagConstraints.LINE_START;
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 3; c.gridy = 5;
		gbLayout.setConstraints(bSeven, c); cvtrWindow.add(bSeven);
		bSeven.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bEight = new JButton("8");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 4; c.gridy = 5;
		gbLayout.setConstraints(bEight, c); cvtrWindow.add(bEight);
		bEight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bNine = new JButton("9");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 5; c.gridy = 5;
		gbLayout.setConstraints(bNine, c); cvtrWindow.add(bNine);
		bNine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { valueButtonEvents(e); } });
		bLeft = new JButton("<-");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 6; c.gridy = 5;
		gbLayout.setConstraints(bLeft, c); cvtrWindow.add(bLeft);
		bLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		bRight = new JButton("->");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 7; c.gridy = 5;
		gbLayout.setConstraints(bRight, c); cvtrWindow.add(bRight);
		bRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });
		bClear = new JButton("Clr");
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 8; c.gridy = 5;
		gbLayout.setConstraints(bClear, c); cvtrWindow.add(bClear);
		bClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { actionButtonEvents(e); } });

	// Build list of digit buttons
		buttonList[0] = bZero; buttonList[1] = bOne; buttonList[2] = bTwo; buttonList[3] = bThree;
		buttonList[4] = bFour; buttonList[5] = bFive; buttonList[6] = bSix; buttonList[7] = bSeven;
		buttonList[8] = bEight; buttonList[9] = bNine; buttonList[10] = bA; buttonList[11] = bB;
		buttonList[12] = bC; buttonList[13] = bD; buttonList[14] = bE; buttonList[15] = bF;
	// Set the default base and digit buttons
		cbBases.setSelectedItem(cvtrEngine.getDefaultBase());
		setBase();

		setKeyEvents();

	}

/**
 * Set the handler for pressing keys, such as "=".
 */
	private void setKeyEvents() {
		AbstractAction eqAA, histAA, undoAA;

		// Set handler for CR
		eqAA = new AbstractAction("eqAction") {
			static final long serialVersionUID = 0;
            public void actionPerformed(ActionEvent e) { eqEvent(); }
        };
		txEquation.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "eqAction");
		txEquation.getActionMap().put("eqAction", eqAA);
		cbOperators.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "eqAction");
		cbOperators.getActionMap().put("eqAction", eqAA);
		cbUnits.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "eqAction");
		cbUnits.getActionMap().put("eqAction", eqAA);
		cbCategories.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "eqAction");
		cbCategories.getActionMap().put("eqAction", eqAA);
		cbBases.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "eqAction");
		cbBases.getActionMap().put("eqAction", eqAA);
		cbSciNotation.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "eqAction");
		cbSciNotation.getActionMap().put("eqAction", eqAA);

		// Set handler for "="
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('='), "eqAction");
		txEquation.getActionMap().put("eqAction", eqAA);
		cbOperators.getInputMap().put(KeyStroke.getKeyStroke('='), "eqAction");
		cbOperators.getActionMap().put("eqAction", eqAA);
		cbUnits.getInputMap().put(KeyStroke.getKeyStroke('='), "eqAction");
		cbUnits.getActionMap().put("eqAction", eqAA);
		cbCategories.getInputMap().put(KeyStroke.getKeyStroke('='), "eqAction");
		cbCategories.getActionMap().put("eqAction", eqAA);
		cbBases.getInputMap().put(KeyStroke.getKeyStroke('='), "eqAction");
		cbBases.getActionMap().put("eqAction", eqAA);
		cbSciNotation.getInputMap().put(KeyStroke.getKeyStroke('='), "eqAction");
		cbSciNotation.getActionMap().put("eqAction", eqAA);

		// Set handler for undo key
		undoAA = new AbstractAction("undoAction") {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent e) { historyUndo(); }
		};
		txEquation.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "undoAction");
		txEquation.getActionMap().put("undoAction", undoAA);

		// Set handler for saving history
		Action eqAct = txEquation.getKeymap().getDefaultAction();
		historyAction = new CvtrHistory(eqAct, this);
		histAA = new AbstractAction("histAction") {
			static final long serialVersionUID = 0;
			public void actionPerformed(ActionEvent e) { historyAction.actionPerformed(e); }
		};
		txEquation.getInputMap().put(KeyStroke.getKeyStroke(' '), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('('), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke(')'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('+'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('-'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('*'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('/'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('%'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('^'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('\\'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('&'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('|'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('#'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);
		txEquation.getInputMap().put(KeyStroke.getKeyStroke('!'), "histAction");
		txEquation.getActionMap().put("histAction", histAA);

	}

/**
 * Build a menu tree for Constants and Equations
 * 
 * @param menu	The menu for setting the tree.
 * @param type	The type of values to be set.
 */
	private void buildTree(JMenu menu, String type) {
		int i, j;
		JMenu submenu;
		JMenuItem mItem;
		String[] tNames, tValues;

		if (type.equals("C")) {
			for (i=0; i < cvtrEngine.ConstantNames.size(); i++) {
				submenu = new JMenu(cvtrEngine.ConstantCategories.get(i));
				tNames = cvtrEngine.ConstantNames.get(i);
				tValues = cvtrEngine.ConstantValues.get(i);
					for (j=0; j < tNames.length; j++) {
						mItem = new JMenuItem(tNames[j]);
						mItem.getAccessibleContext().setAccessibleDescription(tValues[j]);
						mItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) { insertPredefined(e, false); } });
						submenu.add(mItem);
					}
				menu.add(submenu);
			}
		} else if (type.equals("E")) {
			for (i=0; i < cvtrEngine.EquationNames.size(); i++) {
				submenu = new JMenu(cvtrEngine.EquationCategories.get(i));
				tNames = cvtrEngine.EquationNames.get(i);
				tValues = cvtrEngine.EquationValues.get(i);
					for (j=0; j < tNames.length; j++) {
						mItem = new JMenuItem(tNames[j]);
						mItem.getAccessibleContext().setAccessibleDescription(tValues[j]);
						mItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) { insertPredefined(e, true); } });
						submenu.add(mItem);
					}
				menu.add(submenu);
			}
		} else
			return;

	}

/**
 * Insert a pre-defined constant or equation in Equation field.
 * 
 * @param e	The event to be handled.
 * @param equation	Indicates whether the item to be inserted is an equation or not.
 */
	private void insertPredefined(ActionEvent e, boolean equation) {
		JMenuItem mItem = (JMenuItem) e.getSource();
		String eValue = mItem.getAccessibleContext().getAccessibleDescription();
		if (equation) {
			txEquation.setText(eValue);
			txResult.setText("0");
			// Clear the equation format list
			equationResult = null;
			modResult = null;
		} else {
			insertEquationData(eValue);
		}
	}

/**
 * Insert the specified data in the equation field.
 * 
 * @param eData	The data to be inserted.
 */
	private void insertEquationData(String eData) {
		int i = txEquation.getCaretPosition();
		String inputData = txEquation.getText();
		String id1, id2;
		Cursor ieCursor = txEquation.getCursor();

		if (i == inputData.length()) {
			inputData += eData;
		} else {
			id1 = inputData.substring(0, i);
			id2 = inputData.substring(i);
			inputData = id1 + eData + id2;
		}
		txEquation.setText(inputData);
		txEquation.setCaretPosition(i + eData.length());
		txEquation.setCursor(ieCursor);
		txEquation.requestFocus();
	}

/**
 * Maintain the history of the equation field.
 */
	public void historyUpdate() {
		int idx = equationHistory.size() - 1;

		// Enforce maximum history size
		if (idx > 31) {
			equationHistory.remove(0);
			idx--;
		}
		// Avoid duplicates
		if (idx >= 0 && equationHistory.get(idx).equals(txEquation.getText()))
			return;
		equationHistory.add(txEquation.getText());
	}

/**
 * Return the equation field to a previous value.  This may recall previous
 * equations.  There is no redo, when undo is called, the current value is removed.
 */
	private void historyUndo() {
		int idx = equationHistory.size() - 1;
		if (idx < 0)
			return;
		txEquation.setText(equationHistory.get(idx));
		equationHistory.remove(idx);
	}

/**
 * Set the selected operator in the equation field.
 */
	private void setOperator() {
		String operator = cbOperators.getSelectedItem().toString();

		insertEquationData(cvtrEngine.getOpSign(operator));
	}

/**
 * Set the selected operator in the equation field.
 */
	private void setUnit() {
		String uName;

		if (autoSelect)
			return;

		uName = cbUnits.getSelectedItem().toString();
			insertEquationData(cvtrEngine.getUnitAbbrev(uName));
	}

/**
 * Set the selected operator in the equation field.
 */
	private void setCategory() {
		int i = cbCategories.getSelectedIndex();
		String[] newUnits = cvtrEngine.UnitNames.get(i);

		autoSelect = true;
		cbUnits.removeAllItems();
		for (i=0; i < newUnits.length; i++) {
			cbUnits.addItem(newUnits[i]);
		}
		autoSelect = false;
		pack();
		repaint();
	}

/**
 * Set the selected operator in the equation field.
 */
	private void setBase() {
		int i, maxDigits;
		String base = cbBases.getSelectedItem().toString(), bValue;
		Cursor ieCursor = txEquation.getCursor();

		if (initUI)
			return;

		bValue = cvtrEngine.getBaseSign(base, true);
		insertEquationData(bValue);
		if (bValue.length() == 3) {
			i = txEquation.getCaretPosition();
			txEquation.setCaretPosition(i - 1);
			txEquation.setCursor(ieCursor);
		}

		// Enable calculator buttons
		maxDigits = cvtrEngine.getAllowedDigits(base);
		for (i=2; i < 16; i++) {
			if (i < maxDigits)
				buttonList[i].setEnabled(true);
			else
				buttonList[i].setEnabled(false);
		}

	}

/**
 * Handle value button actions.
 * 
 * @param e	The event to be handled.
 */
	private void valueButtonEvents(ActionEvent e) {
		JButton b = (JButton) e.getSource();
		String bName = b.getText();

		if (bName.equals("_"))
			bName = " ";
		insertEquationData(bName);
	}

/**
 * Handle the equal key being pressed.
 */
	private void eqEvent()
	{
		historyUpdate();
		getResult();
		txEquation.requestFocus();
	}

/**
 * Handle action button processes.  These include:
 * <ul>
 *   <li>=: Causes the input equation to be calculated and displayed in the result field.</li>
 *   <li><-: Causes the cursor to be moved left one space in the input equation.</li>
 *   <li>->: Causes the cursor to be moved right one space in the input equation.</li>
 *   <li>Op: Causes the current operator to be added to the input equation at the
 *		current cursor location.</li>
 *   <li>Clr: Causes the input equation field to be cleared.</li>
 *   <li>Unit: Causes the current unit abbreviation to be added to the input equation
 *		at the current cursor location.</li>
 *   <li>Base: Causes the current base symbol to be added to the input equation at the
 *		current cursor location.</li>
 * </ul>
 * 
 * @param e	The event to be handled.
 */
	private void actionButtonEvents(ActionEvent e)
	{
		int i;
		String inputData, id1, id2;
		Cursor ieCursor;
		JButton b = (JButton) e.getSource();
		String bName = b.getText();

		if (bName.equals("=")) {
			eqEvent();
		} else if (bName.equals("<-")) {
			if ((i = txEquation.getCaretPosition()) > 0) {
				txEquation.setCaretPosition(i - 1);
			}
		} else if (bName.equals("->")) {
			if ((i = txEquation.getCaretPosition()) < txEquation.getText().length()) {
				txEquation.setCaretPosition(i + 1);
			}
		} else if (bName.equals("Bks")) {
			if ((i = txEquation.getCaretPosition()) > 0) {
				inputData = txEquation.getText();
				id1 = inputData.substring(0, i - 1);
				id2 = inputData.substring(i);
				inputData = id1 + id2;
				ieCursor = txEquation.getCursor();
				txEquation.setText(inputData);
				txEquation.setCaretPosition(i - 1);
				txEquation.setCursor(ieCursor);
				txEquation.requestFocus();
			}
		} else if (bName.equals("Del")) {
			if ((i = txEquation.getCaretPosition()) < txEquation.getText().length()) {
				inputData = txEquation.getText();
				id1 = inputData.substring(0, i);
				id2 = inputData.substring(i + 1);
				inputData = id1 + id2;
				ieCursor = txEquation.getCursor();
				txEquation.setText(inputData);
				txEquation.setCaretPosition(i);
				txEquation.setCursor(ieCursor);
				txEquation.requestFocus();
			}
		} else if (bName.equals("Op")) {
			setOperator();
		} else if (bName.equals("Clr")) {
			historyUpdate();
			txEquation.setText("");
			txResult.setText("0");
			// Clear the equation format list
			equationResult = null;
			modResult = null;
		} else if (bName.equals("Unit")) {
			setUnit();
		} else if (bName.equals("Base")) {
			setBase();
		} else if (bName.equals("UseE")) {
			if (cbEquation.getItemCount() == 0)
				return;
			txEquation.setText(cbEquation.getSelectedItem().toString());
			txResult.setText("0");
			// Clear the equation format list
			equationResult = null;
			modResult = null;
		} else if (bName.equals("UseR")) {
			if (cbResult.getItemCount() == 0)
				return;
			insertEquationData(cbResult.getSelectedItem().toString());
			historyUpdate();
		} else if (bName.equals("ChgR")) {
			JButton bFinish = new JButton("Finish");
			bFinish.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateResult();
				}
			});
			if (equationResult == null || equationResult.length != cvtrEngine.getResultSize()) {
				equationResult = cvtrEngine.getResultUnits();
				if (equationResult == null) {
					txResult.setText("?? Result");
					return;
				}
			}
			if (modResult != null) {
				if (modResult.mrWindow.isShowing()) {
					modResult.mrWindow.setVisible(true);
					return;
				}
			}
			modResult = new CvtrModifyResult(equationResult, cvtrEngine, bFinish, spPrecision, cbSciNotation);
			Rectangle r = getBounds();
			Rectangle rus = modResult.mrWindow.getBounds();
			i = r.x + (r.width - (rus.width >> 2));
			Double d = (r.height / 7) * 6.0;
			r.y += d;
			modResult.setLocation(i, r.y);
			historyUpdate();
		}
		txEquation.requestFocus();

	}

/**
 * Get modifications to the result.
 */
	private void updateResult() {
		modResult.finishEvent();
		modResult = null;
		eqEvent();
	}

/**
 * Read equations from a test file and output them to a results file.
 * 
 * @param filename	The input filename
 */
	private void testFile(String filename) {
		int i, j;
		String equation, result, eqList, resList = "";

		if ((eqList = cvtrFile.getTextFile(filename)) == null) {
			txResult.setText("?? File");
			return;
		}
		cvtrEngine.setPrecision("5");
		i = 0;
		while (i < eqList.length()) {
			j = eqList.substring(i).indexOf("\n");
			j += i;
			while (eqList.substring(i, j).trim().length() == 0) {
				i = j + 1;
				if (i >= eqList.length()) {
					txResult.setText("File Ready");
					cvtrFile.saveTextFile(resList, filename + ".result");
					return;
				}
				j = eqList.substring(i).indexOf("\n");
				j += i;
				if (j > eqList.length()) {
					txResult.setText("??Format");
					cvtrFile.saveTextFile(resList, filename + ".result");
					return;
				}
			}
			j = eqList.substring(i).indexOf("=");
			j += i;
			if ((i < 0 || eqList.length() <= i) || (j < i || j > eqList.length())) {
				txResult.setText("??Format");
				cvtrFile.saveTextFile(resList, filename + ".result");
				return;
			}
			equation = eqList.substring(i, j);
			i = j + 1;
			if (i >= eqList.length()) {
				txResult.setText("??Format");
				cvtrFile.saveTextFile(resList, filename + ".result");
				return;
			}
			j = eqList.substring(i).indexOf("\n");
			j += i;
			if (j > eqList.length()) {
				txResult.setText("??Format");
				cvtrFile.saveTextFile(resList, filename + ".result");
				return;
			}
			result = eqList.substring(i, j);
			i = j + 1;
			if (cvtrEngine.calculate(equation, 0) < 0) {
				resList += equation + "\nERROR: " + cvtrEngine.getCalcError() +
				"\nExpected: " + result + "\n= = = = = = = \n\n";
			} else {
				resList += equation + "\nResult: " + cvtrEngine.getResult(null) +
				"\nExpected: " + result + "\n________________\n\n";
			}
		}
		
		txResult.setText("File Ready");
		cvtrFile.saveTextFile(resList, filename + ".result");
		return;
	}

/**
 * Get result and display it in the result field.  Also add the equation and result
 * to the previous input menus.
 */
	private void getResult() {
		int i, idx;
		String blank = "";

		// Read test equations from a file
		if (txEquation.getText().length() > 4 &&
				txEquation.getText().substring(0, 4).equalsIgnoreCase("FILE")) {
			testFile(txEquation.getText().substring(4).trim());
			pack();
			repaint();
			return;
		}
		if ((equationResult == null ||
				!txEquation.getText().equals(cbEquation.getItemAt(cbEquation.getSelectedIndex()))) &&
				cvtrEngine.calculate(txEquation.getText(), 0) < 0) {
if (verbose)
System.out.println("Error: " + cvtrEngine.getCalcError());
			txResult.setText(cvtrEngine.getCalcError());
			equationResult = null;
		} else {
			// Set format of result
			if (equationResult == null) {
				equationResult = new CvtrResult[cvtrEngine.EquationResult.length];
				for (idx=0; idx < cvtrEngine.EquationResult.length; idx++) {
					equationResult[idx] = new CvtrResult(cvtrEngine.EquationResult[idx].resultBase, cvtrEngine.EquationResult[idx].resultAbbrev,
						cvtrEngine.EquationResult[idx].conversionFactor, cvtrEngine.EquationResult[idx].indexType, cvtrEngine.EquationResult[idx].indexUnit);
				}
			}
			txResult.setText(cvtrEngine.getResult(equationResult));
			// Make ComboBox String unique by adding blanks at end (see JComboBox.setSelectedIndex definition)
			for (idx=0; idx < cbResult.getItemCount(); idx++) {
				if (cbResult.getItemAt(idx).toString().indexOf(txResult.getText()) == 0) {
					i = cbResult.getItemAt(idx).toString().length() - txResult.getText().length();
					while (i >= 0) {
						blank += " ";
						i--;
					}
				}
			}
			for (idx=0; idx < cbEquation.getItemCount(); idx++) {
				if (cbEquation.getItemAt(idx).toString().equals(txEquation.getText())) {
				cbResult.removeItemAt(idx);
				cbResult.insertItemAt(txResult.getText() + blank, idx);
					break;
				}
			}
			if (idx == cbEquation.getItemCount()) {
				idx = 0;
				cbEquation.insertItemAt(txEquation.getText(), 0);
				cbResult.insertItemAt(txResult.getText() + blank, 0);
			}
			cbEquation.setSelectedIndex(idx);
			cbResult.setSelectedIndex(idx);
			pack();
			repaint();
		}

	}

/**
 * Handle previous input combo box selections.
 * 
 * @param e	The event to be handled.
 */
	private void prevInputEvents(ActionEvent e) {
		JComboBox cb = (JComboBox) e.getSource();
		String cbName = cb.getActionCommand();

		if (autoSelect) {
			return;
		}
		if (cbName.equals("Equation")) {
			autoSelect = true;
			cbResult.setSelectedIndex(cbEquation.getSelectedIndex());
			autoSelect = false;
		} else if (cbName.equals("Result")) {
			autoSelect = true;
			cbEquation.setSelectedIndex(cbResult.getSelectedIndex());
			autoSelect = false;
		}
	}

}
