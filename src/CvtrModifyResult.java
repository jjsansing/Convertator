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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;


/**
 * The Convertator Modify Result class is used to change the base,
 * units, precision, and scientific notation choice of a Result.
 */
public class CvtrModifyResult
{
	private CvtrResult[] mrResult;
	private CvtrEngine mrEngine;
	private JFrame mrFrame = new JFrame();
	public Container mrWindow = null;
	private GridBagLayout gbLayout = null;

	private JComboBox cbBases = null;
	private int unitIndex = 1;
	private int rbIndex;
	private JComboBox cbUnits = null;
	private JSpinner spPrecision = null;
	private JSpinner parent_spPrecision = null;
	private JCheckBox parent_cbSci = null;
	private JCheckBox cbSci = null;
	private JButton bFinish = null;

/**
 * Initialize the Modify Result screen when it is created.
 */
	public CvtrModifyResult(CvtrResult[] result, CvtrEngine engine, JButton finish,
			JSpinner precision, JCheckBox scinot) {

		mrResult = result;
		mrEngine = engine;
		bFinish = finish;
		parent_spPrecision = precision;
		parent_cbSci = scinot;

		mrFrame.setTitle("Modify Result");
		mrWindow = mrFrame.getContentPane();
		gbLayout = new GridBagLayout();
		mrWindow.setLayout(gbLayout);

		initLayout();
		start();
	}

/**
 * Open the Customization Window.
 */
	public void start() {
		mrFrame.pack();
		mrFrame.setVisible(true);
	}

/**
 * Set the Customization Window location.
 * 
 * @param x	The x coordinate of the top left corner of the Customization window.
 * @param y	The y coordinate of the top left corner of the Customization window.
 */
	public void setLocation(int x, int y) {
		mrFrame.setLocation(x, y);
	}

/**
 * Initialize the main result modification layout.
 */
	private void initLayout() {
		int row = 1, col = 0;
		ButtonGroup bgUnits = null;
		JRadioButton rb;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		// Set the list of bases and current selection
		c.insets = new Insets(10,3,2,3);
		cbBases = new JComboBox(mrEngine.getBases());
		cbBases.setSelectedIndex(mrResult[0].indexType);
		c.weightx = 1.0; c.gridwidth = 3; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(cbBases, c); mrWindow.add(cbBases);
		cbBases.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { baseEvent(); }
			public void popupMenuCanceled(PopupMenuEvent e) { } });

		// Set the Unit type radio buttons and get the list of units for each type
		row++;
		bgUnits = new ButtonGroup();
		for (rbIndex=1; rbIndex < mrResult.length; rbIndex++) {
			if (col == 3) {
				row++;
				col = 0;
			}
			rb = new JRadioButton(mrEngine.UnitCategories.get(mrResult[rbIndex].indexType));
			c.weightx = 0.33; c.gridwidth = 1; c.gridx = col; c.gridy = row;
			rb.setName("" + rbIndex);
			gbLayout.setConstraints(rb, c); mrWindow.add(rb);
			if (rbIndex == 1)
				rb.setSelected(true);
			rb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { categoryEvent(e); }
			});
			bgUnits.add(rb);
			col++;
		}

	// Set the first list of units and current selection
		row++;
		if (mrResult.length > 1) {
			cbUnits = new JComboBox(mrEngine.UnitNames.get(mrResult[1].indexType));
			cbUnits.setSelectedIndex(mrResult[1].indexUnit);
			cbUnits.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) { }
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { unitEvent(); }
				public void popupMenuCanceled(PopupMenuEvent e) { } });
		} else {
			String[] dummy = new String[1];
			dummy[0] = "No Units";
			cbUnits = new JComboBox(dummy);
		}
		c.weightx = 1.0; c.gridwidth = 3; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(cbUnits, c); mrWindow.add(cbUnits);

	// Set the current precision value
		row++;
		c.anchor = GridBagConstraints.CENTER;
		JLabel lPrec = new JLabel("Prec", JLabel.CENTER);
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(lPrec, c); mrWindow.add(lPrec);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		SpinnerListModel sl = new SpinnerListModel(mrEngine.getPrecision());
		spPrecision = new JSpinner(sl);
		spPrecision.setValue(parent_spPrecision.getValue());
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(spPrecision, c); mrWindow.add(spPrecision);
		spPrecision.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int i = mrEngine.setPrecision(spPrecision.getValue().toString());
				spPrecision.setValue("" + i);
				parent_spPrecision.setValue(spPrecision.getValue());
			} });
	// Set the current scientific notation choice
		cbSci = new JCheckBox("SciNot");
		cbSci.setSelected(parent_cbSci.isSelected());
		c.weightx = 0.08; c.gridwidth = 1; c.gridx = 2; c.gridy = row;
		gbLayout.setConstraints(cbSci, c); mrWindow.add(cbSci);
		cbSci.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				mrEngine.scientificNotation = cbSci.isSelected();
				parent_cbSci.setSelected(cbSci.isSelected());
			} });

	// Set the finish button
		row++;
		c.gridwidth = 1; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(bFinish, c); mrWindow.add(bFinish);

	}

/**
 * When a new base is selected, set the index in the result set.
 */
	private void baseEvent() {
		mrResult[0].indexType = cbBases.getSelectedIndex();
	}

/**
 * When a new category is selected, set the list of units in that category.
 * Set the current unit selection.
 * 
 * @param bIndex	The index of the selected unit type radio button.
 */
	private void categoryEvent(ActionEvent event) {
		int idx, bIndex;
		String bName;
		String[] newUnits;
		JRadioButton rb = (JRadioButton) event.getSource();
		
		bName = rb.getName();
		bIndex = Integer.parseInt(bName);
		if (bIndex == unitIndex) {
			return;
		} else {
			unitIndex = bIndex;
		}
		newUnits = mrEngine.UnitNames.get(mrResult[bIndex].indexType);

		cbUnits.removeAllItems();
		for (idx=0; idx < newUnits.length; idx++) {
			cbUnits.addItem(newUnits[idx]);
		}
		cbUnits.setSelectedIndex(mrResult[bIndex].indexUnit);
		mrFrame.pack();
		mrFrame.repaint();
	}

/**
 * When a new unit is selected, set the index in the result set.
 */
	private void unitEvent() {
		String uName = cbUnits.getSelectedItem().toString();
		CvtrUnit cu;

		mrResult[unitIndex].indexUnit = cbUnits.getSelectedIndex();
		if ((cu = mrEngine.getUnit(mrEngine.getUnitAbbrev(uName))) != null) {
			mrResult[unitIndex].indexUnit = cu.indexUnit;
			mrResult[unitIndex].resultAbbrev = cu.unitAbbrev;
			mrResult[unitIndex].conversionFactor = cu.conversionFactor;
		}
	}

/**
 * Return the modifications to the caller.
 */
	public void finishEvent() {
		mrFrame.setVisible(false);
		mrFrame.dispose();
	}

}

