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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * The Convertator Custom class displays a window to customize units,
 * constants, or pre-defined equations.  These may be retrieved or stored
 * in files.
 * 
 * This class uses the CvtrFile class to read or write a Convertator data file.
 */
public class CvtrCustom
{
	static final boolean verbose = false;
	public JFrame customFrame = new JFrame();
	private CvtrWindow parentWindow = null;
	private Container customWindow = null;
	private GridBagLayout gbLayout = null;
	private CvtrMessages cvtrMessages = null;

	private JRadioButton bUnit = null;
	private JRadioButton bConstant = null;
	private JRadioButton bEquation = null;
	private int idxUnit = -2;
	private JComboBox cbName = null;
	private JComboBox cbValue = null;
	private JComboBox cbCategory = null;
	private int idxConstant = -2;
	private int idxEquation = -2;
	private JTextField txName = null;
	private JTextField txValue = null;
	private JTextField txCategory = null;

	private ArrayList <String[]> UnitNames = new ArrayList<String[]>();
	private ArrayList <String[]> UnitValues = new ArrayList<String[]>();
	private ArrayList <String> UnitCategories = new ArrayList<String>();
	private ArrayList <String[]> ConstantNames = new ArrayList<String[]>();
	private ArrayList <String[]> ConstantValues = new ArrayList<String[]>();
	private ArrayList <String> ConstantCategories = new ArrayList<String>();
	private ArrayList <String[]> EquationNames = new ArrayList<String[]>();
	private ArrayList <String[]> EquationValues = new ArrayList<String[]>();
	private ArrayList <String> EquationCategories = new ArrayList<String>();

/**
 * The Convertator IO constructor sets the parent class, which conatins the CvtrEngine class
 * and must provide the following Dialog(JFrame parent, String title, String message) methods:
 * <ul>
 *   <li>infoDialog</li>
 *   <li>errorDialog</li>
 *   <li>yesnoDialog</li>
 *   <li>helpDialog</li>
 * </ul>
 * 
 * @param parent	The parent class.
 */
	public CvtrCustom(CvtrWindow parent)
	{

		parentWindow = parent;
		customFrame.setTitle("Customization");
		customWindow = customFrame.getContentPane();
		gbLayout = new GridBagLayout();
		customWindow.setLayout(gbLayout);
		customFrame.setJMenuBar(customMenu());
		cvtrMessages = new CvtrMessages(customFrame, parent.cvtrFile);

		initData();
		initLayout();
		start();
	}

/**
 * Open the Customization Window.
 */
	public void start() {
		customFrame.pack();
		customFrame.setVisible(true);
	}

/**
 * Close the Customization Window.
 */
	public void stop() {
		customFrame.setVisible(false);
		customFrame.dispose();
	}

/**
 * Set the Customization Window location.
 * 
 * @param x	The x coordinate of the top left corner of the Customization window.
 * @param y	The y coordinate of the top left corner of the Customization window.
 */
	public void setLocation(int x, int y) {
		customFrame.setLocation(x, y);
	}

/**
 * Create the main menu bar.
 * 
 * @return JMenuBar	The menu bar created for the Customization window.
 */
	private JMenuBar customMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		menuItem = new JMenuItem("Export to File", KeyEvent.VK_I);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { exportFile(); } });
		menu.add(menuItem);

		menuItem = new JMenuItem("Close", KeyEvent.VK_C);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { stop(); } });
		menu.add(menuItem);

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);

		menuItem = new JMenuItem("Customization", KeyEvent.VK_S);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cvtrMessages.helpDialog("HELP", "interface/custom.cvi"); } });
		menu.add(menuItem);

		return menuBar;
	}

/**
 * Initialize data from the main Convertator engine.
 */
	private void initData() {
		int i, j;
		String[] cData, customData;
		CvtrEngine customEngine = parentWindow.cvtrEngine;

		for (i=0; i < customEngine.UnitCategories.size(); i++)
			UnitCategories.add(customEngine.UnitCategories.get(i));
		// Names and Values are one-for-one
		for (i=0; i < customEngine.UnitNames.size(); i++) {
			cData = customEngine.UnitNames.get(i);
			customData = new String[cData.length];
			for (j=0; j < cData.length; j++)
				customData[j] = cData[j];
			UnitNames.add(customData);
			cData = customEngine.UnitValues.get(i);
			customData = new String[cData.length];
			for (j=0; j < cData.length; j++)
				customData[j] = cData[j];
			UnitValues.add(customData);
		}

		for (i=0; i < customEngine.ConstantCategories.size(); i++)
			ConstantCategories.add(customEngine.ConstantCategories.get(i));
		// Names and Values are one-for-one
		for (i=0; i < customEngine.ConstantNames.size(); i++) {
			cData = customEngine.ConstantNames.get(i);
			customData = new String[cData.length];
			for (j=0; j < cData.length; j++)
				customData[j] = cData[j];
			ConstantNames.add(customData);
			cData = customEngine.ConstantValues.get(i);
			customData = new String[cData.length];
			for (j=0; j < cData.length; j++)
				customData[j] = cData[j];
			ConstantValues.add(customData);
		}

		for (i=0; i < customEngine.EquationCategories.size(); i++)
			EquationCategories.add(customEngine.EquationCategories.get(i));
		// Names and Values are one-for-one
		for (i=0; i < customEngine.EquationNames.size(); i++) {
			cData = customEngine.EquationNames.get(i);
			customData = new String[cData.length];
			for (j=0; j < cData.length; j++)
				customData[j] = cData[j];
			EquationNames.add(customData);
			cData = customEngine.EquationValues.get(i);
			customData = new String[cData.length];
			for (j=0; j < cData.length; j++)
				customData[j] = cData[j];
			EquationValues.add(customData);
		}

	}

/**
 * Initialize the Convertator Customization window.
 */
	private void initLayout() {
		int y = 0;
		JLabel tLabel;
		String[] names, values;

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

	// o Unit  o Constant  o Equation
		c.insets = new Insets(10,3,2,3);
		bUnit = new JRadioButton("Unit"); bUnit.setActionCommand("Unit"); bUnit.setSelected(true);
		bUnit.setHorizontalAlignment(SwingConstants.LEFT);
		c.weightx = 0.33; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		bUnit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { handleButtonSelection(); }
		});
		gbLayout.setConstraints(bUnit, c); customWindow.add(bUnit);
		bConstant = new JRadioButton("Constant"); bConstant.setActionCommand("Constant");
		bConstant.setHorizontalAlignment(SwingConstants.CENTER);
		c.weightx = 0.33; c.gridwidth = 4; c.gridx = 1; c.gridy = y;
		bConstant.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { handleButtonSelection(); }
		});
		gbLayout.setConstraints(bConstant, c); customWindow.add(bConstant);
		bEquation = new JRadioButton("Equation"); bEquation.setActionCommand("Equation");
		bEquation.setHorizontalAlignment(SwingConstants.RIGHT);
		c.weightx = 0.33; c.gridwidth = 1; c.gridx = 5; c.gridy = y;
		bEquation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { handleButtonSelection(); }
		});
		gbLayout.setConstraints(bEquation, c); customWindow.add(bEquation);

		ButtonGroup bgData = new ButtonGroup();
		bgData.add(bUnit); bgData.add(bConstant); bgData.add(bEquation);

	// Name  Value
		y++;
		tLabel = new JLabel("Name"); tLabel.setHorizontalAlignment(SwingConstants.CENTER);
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(tLabel, c); customWindow.add(tLabel);
		tLabel = new JLabel("Value"); tLabel.setHorizontalAlignment(SwingConstants.CENTER);
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 3; c.gridy = y;
		gbLayout.setConstraints(tLabel, c); customWindow.add(tLabel);

	// Name --v  Value --v
		y++;
		names = (String[]) UnitNames.get(0);
		cbName = new JComboBox(names); cbName.setName("UnitName");
		cbName.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { handlePopup("Name"); }
			public void popupMenuCanceled(PopupMenuEvent e) { } });
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(cbName, c); customWindow.add(cbName);
		values = (String[]) UnitValues.get(0);
		cbValue = new JComboBox(values); cbValue.setName("UnitValue");
		cbValue.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { handlePopup("Value"); }
			public void popupMenuCanceled(PopupMenuEvent e) { } });
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 3; c.gridy = y;
		gbLayout.setConstraints(cbValue, c); customWindow.add(cbValue);

	// |-Name-| |-Value-|
		y++;
		txName = new JTextField(12); txName.setEditable(true);
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(txName, c); customWindow.add(txName);
		txValue = new JTextField(12); txValue.setEditable(true);
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 3; c.gridy = y;
		gbLayout.setConstraints(txValue, c); customWindow.add(txValue);

	// Add Update Delete
		y++;
		c.fill = GridBagConstraints.NONE;
		JButton bAddName = new JButton("Add"); bAddName.setName("AddName");
		bAddName.setHorizontalAlignment(SwingConstants.LEFT);
		c.weightx = 0.33; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(bAddName, c); customWindow.add(bAddName);
		bAddName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { addNameEvent(); } });
		JButton bChangeName = new JButton("Updt"); bChangeName.setName("ChangeName");
		bChangeName.setHorizontalAlignment(SwingConstants.CENTER);
		c.weightx = 0.33; c.gridwidth = 4; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(bChangeName, c); customWindow.add(bChangeName);
		bChangeName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { changeNameEvent(); } });
		JButton bDeleteName = new JButton("Del"); bDeleteName.setName("DeleteName");
		bDeleteName.setHorizontalAlignment(SwingConstants.RIGHT);
		c.weightx = 0.33; c.gridwidth = 1; c.gridx = 5; c.gridy = y;
		gbLayout.setConstraints(bDeleteName, c); customWindow.add(bDeleteName);
		bDeleteName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { deleteNameEvent(); } });

	// Category --v  |-Category-|
		y++;
		tLabel = new JLabel("Category"); tLabel.setHorizontalAlignment(SwingConstants.CENTER);
		c.weightx = 1.0; c.gridwidth = 6; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(tLabel, c); customWindow.add(tLabel);

		// Fifth row:  Category --v  |-Category-|
			y++;
		cbCategory = new JComboBox(UnitCategories.toArray()); cbCategory.setName("UnitCategory");
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(cbCategory, c); customWindow.add(cbCategory);
		cbCategory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {handleCategoryAction (); } });
		txCategory = new JTextField(12); txCategory.setEditable(true);
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 3; c.gridy = y;
		gbLayout.setConstraints(txCategory, c); customWindow.add(txCategory);

	// Add Delete
		y++;
		JButton bAddCategory = new JButton("Add"); bAddCategory.setName("AddCategory");
		bAddCategory.setHorizontalAlignment(SwingConstants.CENTER);
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(bAddCategory, c); customWindow.add(bAddCategory);
		bAddCategory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { addCategoryEvent(); } });
		JButton bDeleteCategory = new JButton("Del"); bDeleteCategory.setName("DeleteCategory");
		bDeleteCategory.setHorizontalAlignment(SwingConstants.CENTER);
		c.weightx = 0.5; c.gridwidth = 3; c.gridx = 3; c.gridy = y;
		gbLayout.setConstraints(bDeleteCategory, c); customWindow.add(bDeleteCategory);
		bDeleteCategory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { deleteCategoryEvent(); } });

	}

/**
 * Process the selection of a radio button
 */
	private void handleButtonSelection() {
		int i;

		cbCategory.removeAllItems();
		if (bUnit.isSelected()) {
			for (i=0; i < UnitCategories.size(); i++) {
				cbCategory.addItem(UnitCategories.get(i));
				idxUnit = -2;
			}
		} else if (bConstant.isSelected()) {
			for (i=0; i < ConstantCategories.size(); i++) {
				cbCategory.addItem(ConstantCategories.get(i));
				idxConstant = -2;
			}
		} else if (bEquation.isSelected()) {
			for (i=0; i < EquationCategories.size(); i++) {
				cbCategory.addItem(EquationCategories.get(i));
				idxEquation = -2;
			}
		}
		cbCategory.setSelectedIndex(0);
		handleCategoryAction();
	}

/**
 * Process the selection of a name or value.
 * 
 * @param pName	The name of the popup field, either "Name" or "Value".
 */
	private void handlePopup(String pName)
	{
		int idx;

		if (bUnit.isSelected()) {
			if (pName.equals("Name")) {
				if ((idx = cbName.getSelectedIndex()) >= 0) {
					if (idx != idxUnit) {
						idxUnit = idx;
						if (idx < cbValue.getItemCount())
							cbValue.setSelectedIndex(idx);
					}
				}
			} else if (pName.equals("Value")) {
				if ((idx = cbValue.getSelectedIndex()) >= 0) {
					if (idx != idxUnit) {
						idxUnit = idx;
						if (idx < cbName.getItemCount())
							cbName.setSelectedIndex(idx);
					}
				}
			}
		} else if (bConstant.isSelected()) {
			if (pName.equals("Name")) {
				if ((idx = cbName.getSelectedIndex()) >= 0) {
					if (idx != idxConstant) {
						idxConstant = idx;
						if (idx < cbValue.getItemCount())
							cbValue.setSelectedIndex(idx);
					}
				}
			} else if (pName.equals("Value")) {
				if ((idx = cbValue.getSelectedIndex()) >= 0) {
					if (idx != idxConstant) {
						idxConstant = idx;
						if (idx < cbName.getItemCount())
							cbName.setSelectedIndex(idx);
					}
				}
			}
		} else if (bEquation.isSelected()) {
			if (pName.equals("Name")) {
				if ((idx = cbName.getSelectedIndex()) >= 0) {
					if (idx != idxEquation) {
						idxEquation = idx;
						if (idx < cbValue.getItemCount())
							cbValue.setSelectedIndex(idx);
					}
				}
			} else if (pName.equals("Value")) {
				if ((idx = cbValue.getSelectedIndex()) >= 0) {
					if (idx != idxEquation) {
						idxEquation = idx;
						if (idx < cbName.getItemCount())
							cbName.setSelectedIndex(idx);
					}
				}
			}
		}

		customFrame.pack();
		customFrame.repaint();
	}

/**
 * Process the selection of a category.
 */
	private void handleCategoryAction()
	{
		int i, idx;
		String[] aName, aValue;

		if (bUnit.isSelected()) {
			if ((idx = cbCategory.getSelectedIndex()) >= 0) {
				cbName.removeAllItems();
				cbValue.removeAllItems();
				aName = UnitNames.get(idx);
				aValue = UnitValues.get(idx);
				for (i=0; i < aName.length; i++) {
					cbName.addItem(aName[i]);
					cbValue.addItem(aValue[i]);
				}
			}
		} else if (bConstant.isSelected()) {
			if ((idx = cbCategory.getSelectedIndex()) >= 0) {
				cbName.removeAllItems();
				cbValue.removeAllItems();
				aName = ConstantNames.get(idx);
				aValue = ConstantValues.get(idx);
				for (i=0; i < aName.length; i++) {
					cbName.addItem(aName[i]);
					cbValue.addItem(aValue[i]);
				}
			}
		} else if (bEquation.isSelected()) {
			if ((idx = cbCategory.getSelectedIndex()) >= 0) {
				cbName.removeAllItems();
				cbValue.removeAllItems();
				aName = EquationNames.get(idx);
				aValue = EquationValues.get(idx);
				for (i=0; i < aName.length; i++) {
					cbName.addItem(aName[i]);
					cbValue.addItem(aValue[i]);
				}
			}
		}

		customFrame.pack();
		customFrame.repaint();
	}

/**
 * Handle add name/value button.
 */
	private void addNameEvent()
	{
		int i, j, idx = -1, idxType = -1;
		double d;
		String[] sName, newName, sValue, newValue;
		boolean isDone = false;
		CvtrUnit cUnit;

		if (bUnit.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			sName = UnitNames.get(idxType);
			sValue = UnitValues.get(idxType);
		} else if (bConstant.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			sName = ConstantNames.get(idxType);
			sValue = ConstantValues.get(idxType);
		} else if (bEquation.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			sName = EquationNames.get(idxType);
			sValue = EquationValues.get(idxType);
		} else
			return;

		if (sName.length != sValue.length) {
			cvtrMessages.errorDialog("ERROR", "The Name and Value fields are not in sync.");
			return;
		}

		// Validate entry
		if (txName.getText().length() == 0 || txValue.getText().length() == 0) {
			cvtrMessages.errorDialog("ERROR", "Both the Name and Value fields must be entered.");
			return;
		}
		if (bUnit.isSelected()) {
			try {
				d = Double.parseDouble(txValue.getText());
				if (d == 0) {
					cvtrMessages.errorDialog("ERROR", "Zero value for " + txName.getText() + " not allowed.");
					return;
				}
			} catch (NumberFormatException err) {
				cvtrMessages.errorDialog("ERROR", "Value for " + txName.getText() + " is not a valid floating point number.");
				return;
			}
		}
		if (bUnit.isSelected()) {
			i = txName.getText().indexOf("(") + 1;
			if (i <= 0 || txName.getText().indexOf(")") < 0) {
				cvtrMessages.errorDialog("ERROR", "The format for Unit names is: name (abbreviation)");
				return;
			} else {
				String temp = txName.getText().substring(i, txName.getText().indexOf(")"));
				if (temp.length() < 2) {
					cvtrMessages.errorDialog("ERROR", "The Unit abbreviation must be 2 or more characters");
					return;
				} else if ((cUnit = parentWindow.cvtrEngine.getUnit(temp)) != null) {
					if (!cUnit.displayName.equals(txName.getText())) {
						cvtrMessages.errorDialog("ERROR", "The Unit abbreviation matches the Unit: " + cUnit.displayName);
						return;
					}
				}
			}
		}

		// Add entry to selected list
		if (bUnit.isSelected()) {
			newName = new String[sName.length + 1];
			newValue = new String[sName.length + 1];
			d = Double.parseDouble(txValue.getText());
			j = 0;
			// Sort Units by floating point value
			for (i=0; i < sName.length; i++) {
				if (!isDone) {
					try {
						// Location of new value is found
						if (d < Double.parseDouble(sValue[i])) {
							isDone = true;
							idx = j;
							newName[j] = txName.getText();
							newValue[j++] = txValue.getText();
							newName[j] = sName[i];
							newValue[j++] = sValue[i];
						} else {
							newName[j] = sName[i];
							newValue[j++] = sValue[i];
						}
					// Ignore non-floating point values
					} catch (NumberFormatException err) {
						newName[j] = sName[i];
						newValue[j++] = sValue[i];
					}
				} else {
					newName[j] = sName[i];
					newValue[j++] = sValue[i];
				}
			}
			if (idx < 0) {
				idx = j;
				newName[j] = txName.getText();
				newValue[j++] = txValue.getText();
			}
		} else {
			newName = new String[sName.length + 1];
			newValue = new String[sName.length + 1];
			j = 0;
			// Sort Constants and Equations by name
			for (i=0; i < sName.length; i++) {
				// Location of new value is found
				if (!isDone && txName.getText().compareToIgnoreCase((sName[i])) < 0) {
					isDone = true;
					idx = j;
					newName[j] = txName.getText();
					newValue[j++] = txValue.getText();
					newName[j] = sName[i];
					newValue[j++] = sValue[i];
				} else {
					newName[j] = sName[i];
					newValue[j++] = sValue[i];
				}
			}
			if (idx < 0) {
				idx = j;
				newName[j] = txName.getText();
				newValue[j++] = txValue.getText();
			}
		}

		// Update Name and Value settings
		if (bUnit.isSelected()) {
			UnitNames.remove(idxType);
			UnitNames.add(idxType, newName);
			UnitValues.remove(idxType);
			UnitValues.add(idxType, newValue);
			cbName.insertItemAt(txName.getText(), idx);
			cbName.setSelectedIndex(idx);
			cbValue.insertItemAt(txValue.getText(), idx);
			cbValue.setSelectedIndex(idx);
		} else if (bConstant.isSelected()) {
			ConstantNames.remove(idxType);
			ConstantNames.add(idxType, newName);
			ConstantValues.remove(idxType);
			ConstantValues.add(idxType, newValue);
			cbName.insertItemAt(txName.getText(), idx);
			cbName.setSelectedIndex(idx);
			cbValue.insertItemAt(txValue.getText(), idx);
			cbValue.setSelectedIndex(idx);
		} else if (bEquation.isSelected()) {
			EquationNames.remove(idxType);
			EquationNames.add(idxType, newName);
			EquationValues.remove(idxType);
			EquationValues.add(idxType, newValue);
			cbName.insertItemAt(txName.getText(), idx);
			cbName.setSelectedIndex(idx);
			cbValue.insertItemAt(txValue.getText(), idx);
			cbValue.setSelectedIndex(idx);
		}

		customFrame.pack();
		customFrame.repaint();

	}

/**
 * Handle change name/value button.
 */
	private void changeNameEvent()
	{
		int i, j, idx = -1, idxType = -1, idxCurrent = -1;
		double d;
		String[] sName, newName = null, sValue, newValue = null;
		boolean isDone = false;
		CvtrUnit cUnit;

		if (txName.getText().length() == 0 || txValue.getText().length() == 0) {
			cvtrMessages.errorDialog("ERROR", "Both the Name and Value fields are required.");
			return;
		}

		if (bUnit.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			idxCurrent = cbName.getSelectedIndex();
			sName = UnitNames.get(idxType);
			sValue = UnitValues.get(idxType);
		} else if (bConstant.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			idxCurrent = cbName.getSelectedIndex();
			sName = ConstantNames.get(idxType);
			sValue = ConstantValues.get(idxType);
		} else if (bEquation.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			idxCurrent = cbName.getSelectedIndex();
			sName = EquationNames.get(idxType);
			sValue = EquationValues.get(idxType);
		} else
			return;

		if (sName.length != sValue.length) {
			cvtrMessages.errorDialog("ERROR", "The Name and Value fields are not in sync.");
			return;
		}

		// Validate entry
		if (bUnit.isSelected()) {
			try {
				d = Double.parseDouble(txValue.getText());
				if (d == 0) {
					cvtrMessages.errorDialog("ERROR", "Zero value for " + txName.getText() + " not allowed.");
					return;
				}
			} catch (NumberFormatException err) {
				cvtrMessages.errorDialog("ERROR", "Value for " + txName.getText() + " is not a valid floating point number.");
				return;
			}
			i = txName.getText().indexOf("(") + 1;
			if (i <= 0 || txName.getText().indexOf(")") < 0) {
				cvtrMessages.errorDialog("ERROR", "The format for Unit names is: name (abbreviation)");
				return;
			} else {
				String temp = txName.getText().substring(i, txName.getText().indexOf(")"));
				if (temp.length() < 2) {
					cvtrMessages.errorDialog("ERROR", "The Unit abbreviation must be 2 or more characters");
					return;
				} else if ((cUnit = parentWindow.cvtrEngine.getUnit(temp)) != null) {
					if (!cUnit.displayName.equals(txName.getText())) {
						cvtrMessages.errorDialog("ERROR", "The Unit abbreviation matches the Unit: " + cUnit.displayName);
						return;
					}
				}
			}
		}

		// Add entry to selected list
		if (bUnit.isSelected()) {
			newName = new String[sName.length];
			newValue = new String[sName.length];
			d = Double.parseDouble(txValue.getText());
			j = 0;
			// Sort Units by floating point value
			for (i=0; i < sName.length; i++) {
				if (i == idxCurrent)
					continue;
				if (!isDone) {
					try {
						// Location of new value is found
						if (d < Double.parseDouble(sValue[i])) {
							isDone = true;
							idx = j;
							newName[j] = txName.getText();
							newValue[j++] = txValue.getText();
							newName[j] = sName[i];
							newValue[j++] = sValue[i];
						} else {
							newName[j] = sName[i];
							newValue[j++] = sValue[i];
						}
					// Ignore non-floating point values
					} catch (NumberFormatException err) {
						newName[j] = sName[i];
						newValue[j++] = sValue[i];
					}
				} else {
					newName[j] = sName[i];
					newValue[j++] = sValue[i];
				}
			}
			if (idx < 0) {
				idx = j;
				newName[j] = txName.getText();
				newValue[j++] = txValue.getText();
			}
		} else {
			newName = new String[sName.length];
			newValue = new String[sName.length];
			j = 0;
			// Sort Constants and Equations by name
			for (i=0; i < sName.length; i++) {
				if (i == idxCurrent)
					continue;
				// Location of new value is found
				if (!isDone && txName.getText().compareToIgnoreCase((sName[i])) < 0) {
					isDone = true;
					idx = j;
					newName[j] = txName.getText();
					newValue[j++] = txValue.getText();
					newName[j] = sName[i];
					newValue[j++] = sValue[i];
				} else {
					newName[j] = sName[i];
					newValue[j++] = sValue[i];
				}
			}
			if (idx < 0) {
				idx = j;
				newName[j] = txName.getText();
				newValue[j++] = txValue.getText();
			}
		}

		// Update Name and Value settings
		if (bUnit.isSelected()) {
			if (newName != null) {
				UnitNames.remove(idxType);
				UnitNames.add(idxType, newName);
				UnitValues.remove(idxType);
				UnitValues.add(idxType, newValue);
			} else {
				sName[idx] = txName.getText();
				sValue[idx] = txValue.getText();
			}
			if (txName.getText().length() > 0) {
				cbName.removeItemAt(idxCurrent);
				cbName.insertItemAt(txName.getText(), idx);
				cbName.setSelectedIndex(idx);
			}
			if (txValue.getText().length() > 0) {
				cbValue.removeItemAt(idxCurrent);
				cbValue.insertItemAt(txValue.getText(), idx);
				cbValue.setSelectedIndex(idx);
			}
		} else if (bConstant.isSelected()) {
			if (newName != null) {
				ConstantNames.remove(idxType);
				ConstantNames.add(idxType, newName);
				ConstantValues.remove(idxType);
				ConstantValues.add(idxType, newValue);
			} else {
				sName[idx] = txName.getText();
				sValue[idx] = txValue.getText();
			}
			if (txName.getText().length() > 0) {
				cbName.removeItemAt(idxCurrent);
				cbName.insertItemAt(txName.getText(), idx);
				cbName.setSelectedIndex(idx);
			}
			if (txValue.getText().length() > 0) {
				cbValue.removeItemAt(idxCurrent);
				cbValue.insertItemAt(txValue.getText(), idx);
				cbValue.setSelectedIndex(idx);
			}
		} else if (bEquation.isSelected()) {
			if (newName != null) {
				EquationNames.remove(idxType);
				EquationNames.add(idxType, newName);
				EquationValues.remove(idxType);
				EquationValues.add(idxType, newValue);
			} else {
				sName[idx] = txName.getText();
				sValue[idx] = txValue.getText();
			}
			if (txName.getText().length() > 0) {
				cbName.removeItemAt(idxCurrent);
				cbName.insertItemAt(txName.getText(), idx);
				cbName.setSelectedIndex(idx);
			}
			if (txValue.getText().length() > 0) {
				cbValue.removeItemAt(idxCurrent);
				cbValue.insertItemAt(txValue.getText(), idx);
				cbValue.setSelectedIndex(idx);
			}
		}

		customFrame.pack();
		customFrame.repaint();

	}

/**
 * Handle delete name/value button.
 */
	private void deleteNameEvent()
	{
		int i, j, idxType = -1, idxCurrent = -1;
		String vName, vValue;
		String[] sName, newName, sValue, newValue;

		if (bUnit.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			idxCurrent = cbName.getSelectedIndex();
			sName = UnitNames.get(idxType);
			sValue = UnitValues.get(idxType);
			vName = cbName.getSelectedItem().toString();
			vValue = cbValue.getSelectedItem().toString();
		} else if (bConstant.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			idxCurrent = cbName.getSelectedIndex();
			sName = ConstantNames.get(idxType);
			sValue = ConstantValues.get(idxType);
			vName = cbName.getSelectedItem().toString();
			vValue = cbValue.getSelectedItem().toString();
		} else if (bEquation.isSelected()) {
			idxType = cbCategory.getSelectedIndex();
			idxCurrent = cbName.getSelectedIndex();
			sName = EquationNames.get(idxType);
			sValue = EquationValues.get(idxType);
			vName = cbName.getSelectedItem().toString();
			vValue = cbValue.getSelectedItem().toString();
		} else
			return;

		// Verify delete
		if (cvtrMessages.yesnoDialog("DELETE", "Delete " + vName + ": " + vValue + " from list?")  == JOptionPane.NO_OPTION)
			return;

		// Remove selected field from Name and Value arrays
		if (sName.length < 2) {
			newName = new String[1]; newName[0] = "NONE";
			newValue = new String[1]; newValue[0] = "NONE";
		} else {
			newName = new String[sName.length -1];
			newValue = new String[sValue.length -1];
			j = 0;
			for (i=0; i < sName.length; i++) {
				if (i == idxCurrent)
					continue;
				newName[j] = sName[i];
				newValue[j++] = sValue[i];
			}
		}

		// Update Name and Value settings
		if (bUnit.isSelected()) {
			UnitNames.remove(idxType);
			UnitNames.add(idxType, newName);
			UnitValues.remove(idxType);
			UnitValues.add(idxType, newValue);
			cbName.removeItemAt(idxCurrent);
			cbName.setSelectedIndex(0);
			cbValue.removeItemAt(idxCurrent);
			cbValue.setSelectedIndex(0);
		} else if (bConstant.isSelected()) {
			ConstantNames.remove(idxType);
			ConstantNames.add(idxType, newName);
			ConstantValues.remove(idxType);
			ConstantValues.add(idxType, newValue);
			cbName.removeItemAt(idxCurrent);
			cbName.setSelectedIndex(0);
			cbValue.removeItemAt(idxCurrent);
			cbValue.setSelectedIndex(0);
		} else if (bEquation.isSelected()) {
			EquationNames.remove(idxType);
			EquationNames.add(idxType, newName);
			EquationValues.remove(idxType);
			EquationValues.add(idxType, newValue);
			cbName.removeItemAt(idxCurrent);
			cbName.setSelectedIndex(0);
			cbValue.removeItemAt(idxCurrent);
			cbValue.setSelectedIndex(0);
		}

		customFrame.pack();
		customFrame.repaint();

	}

/**
 * Handle add category button.  This updates the category list and adds a single
 * placeholder for the name and value of the new category.
 */
	private void addCategoryEvent()
	{
		int i, idx;
		String[] aInit = {"NONE"};

		if (bUnit.isSelected()) {
			// Validate entry
			idx = UnitCategories.size();
			for (i=0; i < idx; i++) {
				if (UnitCategories.get(i).equalsIgnoreCase(txCategory.getText())) {
					cvtrMessages.errorDialog("ERROR", "The category name already exists.");
					return;
				}
			}
			// Update Category settings
			UnitCategories.add(txCategory.getText());
			cbCategory.addItem(txCategory.getText());
			// Add initial Name and Value field
			UnitNames.add(aInit);
			UnitValues.add(aInit);
			cbCategory.setSelectedIndex(idx);
		} else if (bConstant.isSelected()) {
			// Validate entry
			idx = ConstantCategories.size();
			for (i=0; i < ConstantCategories.size(); i++) {
				if (ConstantCategories.get(i).equalsIgnoreCase(txCategory.getText())) {
					cvtrMessages.errorDialog("ERROR", "The category name already exists.");
					return;
				}
			}
			// Update Category settings
			ConstantCategories.add(txCategory.getText());
			cbCategory.addItem(txCategory.getText());
			// Add initial Name and Value field
			ConstantNames.add(aInit);
			ConstantValues.add(aInit);
			cbCategory.setSelectedIndex(idx);
		} else if (bEquation.isSelected()) {
			// Validate entry
			idx = EquationCategories.size();
			for (i=0; i < EquationCategories.size(); i++) {
				if (EquationCategories.get(i).equalsIgnoreCase(txCategory.getText())) {
					cvtrMessages.errorDialog("ERROR", "The category name already exists.");
					return;
				}
			}
			// Update Category settings
			EquationCategories.add(txCategory.getText());
			cbCategory.addItem(txCategory.getText());
			// Add initial Name and Value field
			EquationNames.add(aInit);
			EquationValues.add(aInit);
			cbCategory.setSelectedIndex(idx);
		} else
			return;

		customFrame.pack();
		customFrame.repaint();

	}

/**
 * Handle delete category button.
 */
	private void deleteCategoryEvent()
	{
		String filename, dataType;
		String aysMessage = "Do you want to delete the file: " + txCategory.getText() + "?";

		if (bUnit.isSelected()) 
			dataType = "Units";
		else if (bConstant.isSelected()) 
			dataType = "Constants";
		else if (bEquation.isSelected()) 
			dataType = "Equations";
		else {
			cvtrMessages.errorDialog("ERROR", "No data type selected.");
			return;
		}
		filename = "data" + File.separator + dataType + File.separator + txCategory.getText() + ".cvd";
		if (txCategory.getText().length() == 0) {
			cvtrMessages.infoDialog("WARNING", "Enter the Category to be deleted\nin the Category text field.");
		} else if (!parentWindow.cvtrFile.deleteFile(filename, aysMessage)) {
			cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);
		}
else if (verbose)
System.out.println(" == Deleted file: " + txCategory.getText());
	}

/**
 * Save a file to disk.
 */
	private void exportFile()
	{
		int i = 0, idxType = 0;
		String dataDir = "UNK";
		double d;
		Calendar cal = Calendar.getInstance();
		String dfltCopyright = "Copyright " + cal.get(Calendar.YEAR) + " " +
								parentWindow.userPrefs.ownerName + "\n\n" +
								"This Convertator data file is licensed under the " +
								"Creative Commons Attribution-NonCommercial-ShareAlike 3.0 license.  " +
								"The full text of this license may be found at:\n\n" +
								"http://creativecommons.org/licenses/by-nc-sa/3.0/us/legalcode";
		CvtrData cData;
		String saveCategory, saveType;
		boolean unicode = false;

		if (bUnit.isSelected()) {
			saveType = "U";
			dataDir = "data" + File.separator + "Units" + File.separator;
			idxType = cbCategory.getSelectedIndex();
			saveCategory = cbCategory.getSelectedItem().toString();
		} else if (bConstant.isSelected()) {
			saveType = "C";
			dataDir = "data" + File.separator + "Constants" + File.separator;
			idxType = cbCategory.getSelectedIndex();
			saveCategory = cbCategory.getSelectedItem().toString();
		} else if (bEquation.isSelected()) {
			saveType = "E";
			dataDir = "data" + File.separator + "Equations" + File.separator;
			idxType = cbCategory.getSelectedIndex();
			saveCategory = cbCategory.getSelectedItem().toString();
		} else {
			cvtrMessages.errorDialog("ERROR", "No data type selected.");
			return;
		}

		if (parentWindow.textEncoding.equalsIgnoreCase("UTF-16"))
			unicode = true;
		else if (!parentWindow.textEncoding.equalsIgnoreCase("UTF-8")) {
			cvtrMessages.infoDialog("WARNING", "Warning: Character encoding " + parentWindow.textEncoding +
					" is not explicitly supported, but should work for local files.");
		}

		try {
			cData = new CvtrData(dfltCopyright, saveCategory, saveType, unicode);
		} catch (Exception err) {
			cvtrMessages.errorDialog("ERROR", "Error setting Convertator data values:\n\n  " + err.getMessage());
			return;
		}

		// Get Unit data
		if (saveType.equals("U")) {
			cData.elementNames = UnitNames.get(idxType);
			cData.elementValues = UnitValues.get(idxType);
			try {
				for (i=0; i < cData.elementValues.length; i++) {
					d = Double.parseDouble(cData.elementValues[i]);
					if (d == 0) {
						cvtrMessages.errorDialog("ERROR", "Zero value for " + cData.elementNames[i] + " not allowed.");
						return;
					}
				}
			} catch (NumberFormatException err) {
				cvtrMessages.errorDialog("ERROR", "Value for " + cData.elementNames[i] + " is not a valid floating point number.");
				return;
			}
		// Get Constant data
		} else if (saveType.equals("C")) {
			cData.elementNames = ConstantNames.get(idxType);
			cData.elementValues = ConstantValues.get(idxType);
		// Get Equation data
		} else if (saveType.equals("E")) {
			cData.elementNames = EquationNames.get(idxType);
			cData.elementValues = EquationValues.get(idxType);
		}

		// Attempt to save the file in the HOME data directory
		if (!parentWindow.cvtrFile.saveCvtrFile(cData, parentWindow.cvtrFile.homeDir + dataDir + cData.elementCategory + ".cvd")) {
			cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);
		}

	}

}
