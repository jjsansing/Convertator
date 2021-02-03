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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;

/**
 * The Convertator Import class displays a window to download units, constants, or
 * pre-defined equations from a website.  The CvtrUpdate class parses the data and
 * the formats are described in that class. 
 * 
 * This class uses the CvtrFile class to read a Convertator data file
 * for validation.
 */
public class CvtrImport
{
	private CvtrWindow parentWindow = null;
	private Container importWindow = null;
	public JFrame importFrame = new JFrame();
	private GridBagLayout gbLayout = null;
	private CvtrUpdate updateData;
	private CvtrPrefs userPrefs;
	private CvtrMessages cvtrMessages = null;

	private JComboBox cbSiteName = null;
	private JTextField txSiteName = null;
	private JTextField txSiteDomain = null;
	private JTextField txSitePath = null;
	private JRadioButton bUnit = null;
	private JComboBox cbUnitGroups = null;
	private JComboBox cbUnits = null;
	private JRadioButton bConstant = null;
	private JComboBox cbConstantGroups = null;
	private JComboBox cbConstants = null;
	private JRadioButton bEquation = null;
	private JComboBox cbEquationGroups = null;
	private JComboBox cbEquations = null;


/**
 * The Convertator Import constructor sets the parent class, which contains the CvtrFile class
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
	public CvtrImport(CvtrWindow parent, CvtrPrefs preferences)
	{

		parentWindow = parent;
		userPrefs = preferences;
		updateData = new CvtrUpdate(userPrefs.primaryLangAbbrev, userPrefs.secondLangAbbrev);
		importFrame.setTitle("Import");
		importWindow = importFrame.getContentPane();
		gbLayout = new GridBagLayout();
		importWindow.setLayout(gbLayout);
		importFrame.setJMenuBar(importMenu());
		cvtrMessages = new CvtrMessages(importFrame, parent.cvtrFile);

		initData();
		importInit();
		start();
	}

/**
 * Open the Import Window.
 */
	public void start() {
		importFrame.pack();
		importFrame.setVisible(true);
	}

/**
 * Close the Import Window.
 */
	public void stop() {
		importFrame.setVisible(false);
		importFrame.dispose();
	}

/**
 * Set the Import Window location.
 * 
 * @param x	The x coordinate of the top left corner of the Import window.
 * @param y	The y coordinate of the top left corner of the Import window.
 */
	public void setLocation(int x, int y) {
		importFrame.setLocation(x, y);
	}

/**
 * Create the main menu bar.
 * 
 * @return JMenuBar	The menu bar created for the Customization window.
 */
	private JMenuBar importMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		menuItem = new JMenuItem("Close", KeyEvent.VK_C);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { stop(); } });
		menu.add(menuItem);

		menu = new JMenu("Help");
		menu.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menu);

		menuItem = new JMenuItem("Import", KeyEvent.VK_I);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cvtrMessages.helpDialog("HELP", "interface/import.cvi"); } });
		menu.add(menuItem);

		return menuBar;
	}

/**
 * Initialize the data for the selected site.
 */
	private void initData() {
		int idx;
		String fileData;

		if (updateData.SiteNames.size() == 0) {
			// Check home directory first
			if ((fileData = parentWindow.cvtrFile.getTextFile(parentWindow.cvtrFile.homeDir + "site.cvu")) == null) {
				// If not found check application directory
				if ((fileData = parentWindow.cvtrFile.getTextFile("site.cvu")) == null) {
					updateData.SiteNames.add("NONE");
				} else {
					updateData.parseSiteUpdate(fileData, false);
				}
			} else {
				updateData.parseSiteUpdate(fileData, false);
			}
		}
		for (idx=0; idx < updateData.SiteNames.size(); idx++) {
			// Check home directory first
			if ((fileData = parentWindow.cvtrFile.getTextFile(parentWindow.cvtrFile.homeDir + updateData.SiteNames.get(idx) + "_data.cvu")) == null) {
				// If not found check application directory
				if ((fileData = parentWindow.cvtrFile.getTextFile(updateData.SiteNames.get(idx) + "_data.cvu")) != null) {
					updateData.parseSiteData(idx, fileData, false);
				}
			} else {
				updateData.parseSiteData(idx, fileData, false);
			}
		}

	}

/**
 * Initialize the Convertator Import window.
 */
	private void importInit() {
		int y = 0;
		String[] cbData;
		JLabel lSite;

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

	// First row: Site --v
		c.insets = new Insets(10,3,2,3);
		lSite = new JLabel("Site"); lSite.setHorizontalAlignment(JLabel.RIGHT);
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(lSite, c); importWindow.add(lSite);
		cbSiteName = new JComboBox(updateData.SiteNames.toArray());
		c.weightx = 0.23; c.gridwidth = 3; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(cbSiteName, c); importWindow.add(cbSiteName);
		cbSiteName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { newSiteEvent(); } });

	// Second row: New Site (Add) (Refresh)
		y++;
		c.fill = GridBagConstraints.NONE;
		lSite = new JLabel("Site"); lSite.setHorizontalAlignment(JLabel.RIGHT);
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(lSite, c); importWindow.add(lSite);
		JButton bAdd = new JButton("Add/Update");
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(bAdd, c); importWindow.add(bAdd);
		bAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { addSiteEvent(); } });
		JButton bRefresh = new JButton("Refresh");
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 2; c.gridy = y;
		gbLayout.setConstraints(bRefresh, c); importWindow.add(bRefresh);
		bRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { refreshEvent(); } });

		// Third row: Name |---|
		y++;
		c.fill = GridBagConstraints.HORIZONTAL;
		lSite = new JLabel("Name"); lSite.setHorizontalAlignment(JLabel.CENTER);
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(lSite, c); importWindow.add(lSite);
		txSiteName = new JTextField(15); txSiteName.setEditable(true);
		c.weightx = 0.23; c.gridwidth = 3; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(txSiteName, c); importWindow.add(txSiteName);

		// Fourth row: Domain |---|
		y++;
		lSite = new JLabel("Domain"); lSite.setHorizontalAlignment(JLabel.CENTER);
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(lSite, c); importWindow.add(lSite);
		txSiteDomain = new JTextField(25); txSiteDomain.setEditable(true);
		c.weightx = 0.23; c.gridwidth = 3; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(txSiteDomain, c); importWindow.add(txSiteDomain);
		
		// Fifth row: Path |---|
		y++;
		lSite = new JLabel("Path"); lSite.setHorizontalAlignment(JLabel.CENTER);
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(lSite, c); importWindow.add(lSite);
		txSitePath = new JTextField(25); txSitePath.setEditable(true);
		c.weightx = 0.23; c.gridwidth = 3; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(txSitePath, c); importWindow.add(txSitePath);

	// Sixth Row: Download
		y++;
		c.fill = GridBagConstraints.NONE;
		JButton bDownload = new JButton("Download");
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(bDownload, c); importWindow.add(bDownload);
		bDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { downloadEvent(); } });

	// Seventh row: o Unit Category --v
		y++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,3,2,3);
		bUnit = new JRadioButton("Units"); bUnit.setActionCommand("Unit"); bUnit.setSelected(true);
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(bUnit, c); importWindow.add(bUnit);
		if (updateData.UnitGroups.size() == 0 ||
				updateData.UnitGroups.get(0).size() == 0) {
			cbData = new String[1];
			cbData[0] = "NONE";
		} else {
			cbData = new String[updateData.UnitGroups.get(0).size()];
			updateData.UnitGroups.get(0).toArray(cbData);
		}
		cbUnitGroups = new JComboBox(cbData); cbUnitGroups.setName("UnitGroup");
		c.weightx = 0.23; c.gridwidth = 1; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(cbUnitGroups, c); importWindow.add(cbUnitGroups);
		cbUnitGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { setGroup(e); } });
		if (updateData.Units.size() == 0 ||
				updateData.Units.get(0).size() == 0 ||
				updateData.Units.get(0).get(0).size() == 0) {
			cbData = new String[1];
			cbData[0] = "NONE";
		} else {
			cbData = new String[updateData.Units.get(0).get(0).size()];
			updateData.Units.get(0).get(0).toArray(cbData);
		}
		cbUnits = new JComboBox(cbData);
		c.weightx = 0.23; c.gridwidth = 1; c.gridx = 2; c.gridy = y;
		gbLayout.setConstraints(cbUnits, c); importWindow.add(cbUnits);

	// Eighth row: o Constant Category --v  Constants --v
		y++;
		c.insets = new Insets(2,3,2,3);
		bConstant = new JRadioButton("Constants"); bConstant.setActionCommand("Constant");
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(bConstant, c); importWindow.add(bConstant);
		if (updateData.ConstantGroups.size() == 0 ||
				updateData.ConstantGroups.get(0).size() == 0) {
			cbData = new String[1];
			cbData[0] = "NONE";
		} else {
			cbData = new String[updateData.ConstantGroups.get(0).size()];
			updateData.ConstantGroups.get(0).toArray(cbData);
		}
		cbConstantGroups = new JComboBox(cbData); cbConstantGroups.setName("ConstantGroup");
		c.weightx = 0.23; c.gridwidth = 1; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(cbConstantGroups, c); importWindow.add(cbConstantGroups);
		cbConstantGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { setGroup(e); } });
		if (updateData.Constants.size() == 0 ||
				updateData.Constants.get(0).size() == 0 ||
				updateData.Constants.get(0).get(0).size() == 0) {
			cbData = new String[1];
			cbData[0] = "NONE";
		} else {
			cbData = new String[updateData.Constants.get(0).get(0).size()];
			updateData.Constants.get(0).get(0).toArray(cbData);
		}
		cbConstants = new JComboBox(cbData);
		c.weightx = 0.23; c.gridwidth = 1; c.gridx = 2; c.gridy = y;
		gbLayout.setConstraints(cbConstants, c); importWindow.add(cbConstants);

	// Ninth row: o Equation Category --v  Equations --v
		y++;
		c.insets = new Insets(2,3,2,3);
		bEquation = new JRadioButton("Equations"); bEquation.setActionCommand("Equation");
		c.weightx = 0.1; c.gridwidth = 1; c.gridx = 0; c.gridy = y;
		gbLayout.setConstraints(bEquation, c); importWindow.add(bEquation);
		if (updateData.EquationGroups.size() == 0 ||
				updateData.EquationGroups.get(0).size() == 0) {
			cbData = new String[1];
			cbData[0] = "NONE";
		} else {
			cbData = new String[updateData.EquationGroups.get(0).size()];
			updateData.EquationGroups.get(0).toArray(cbData);
		}
		cbEquationGroups = new JComboBox(cbData); cbEquationGroups.setName("EquationGroup");
		c.weightx = 0.23; c.gridwidth = 1; c.gridx = 1; c.gridy = y;
		gbLayout.setConstraints(cbEquationGroups, c); importWindow.add(cbEquationGroups);
		cbEquationGroups.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { setGroup(e); } });
		if (updateData.Equations.size() == 0 ||
				updateData.Equations.get(0).size() == 0 ||
				updateData.Equations.get(0).get(0).size() == 0) {
			cbData = new String[1];
			cbData[0] = "NONE";
		} else {
			cbData = new String[updateData.Equations.get(0).get(0).size()];
			updateData.Equations.get(0).get(0).toArray(cbData);
		}
		cbEquations = new JComboBox(cbData);
		c.weightx = 0.23; c.gridwidth = 1; c.gridx = 2; c.gridy = y;
		gbLayout.setConstraints(cbEquations, c); importWindow.add(cbEquations);

		ButtonGroup bgData = new ButtonGroup();
		bgData.add(bUnit); bgData.add(bConstant); bgData.add(bEquation);

	}

/**
 * Set the units, constants, and equations for the selected site.  The
 * constants and equations are set by modifying the groups and selecting
 * the first element
 */
	private void newSiteEvent() {
		int i, idx = cbSiteName.getSelectedIndex();

		// Upate unit data
		cbUnitGroups.removeActionListener(cbUnitGroups.getActionListeners()[0]);
		cbUnitGroups.removeAllItems();
		if (updateData.UnitGroups.size() == 0 || updateData.UnitGroups.get(idx).size() == 0) {
			cbUnitGroups.addItem("NONE");
			cbUnits.removeAllItems();
			cbUnits.addItem("NONE");
			cbUnitGroups.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { setGroup(e); } });
		} else {
			for (i=0; i < updateData.UnitGroups.get(idx).size(); i++) {
				cbUnitGroups.addItem(updateData.UnitGroups.get(idx).get(i));
			}
			cbUnitGroups.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { setGroup(e); } });
			cbUnitGroups.setSelectedIndex(0);
		}
		// Update constants data
		cbConstantGroups.removeActionListener(cbConstantGroups.getActionListeners()[0]);
		cbConstantGroups.removeAllItems();
		if (updateData.ConstantGroups.size() == 0 || updateData.ConstantGroups.get(idx).size() == 0) {
			cbConstantGroups.addItem("NONE");
			cbConstants.removeAllItems();
			cbConstants.addItem("NONE");
			cbConstantGroups.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { setGroup(e); } });
		} else {
			for (i=0; i < updateData.ConstantGroups.get(idx).size(); i++) {
				cbConstantGroups.addItem(updateData.ConstantGroups.get(idx).get(i));
			}
			cbConstantGroups.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { setGroup(e); } });
			cbConstantGroups.setSelectedIndex(0);
		}
		// Update equations data
		cbEquationGroups.removeActionListener(cbEquationGroups.getActionListeners()[0]);
		cbEquationGroups.removeAllItems();
		if (updateData.EquationGroups.size() == 0 || updateData.EquationGroups.get(idx).size() == 0) {
			cbEquationGroups.addItem("NONE");
			cbEquations.removeAllItems();
			cbEquations.addItem("NONE");
			cbEquationGroups.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { setGroup(e); } });
		} else {
			for (i=0; i < updateData.EquationGroups.get(idx).size(); i++) {
				cbEquationGroups.addItem(updateData.EquationGroups.get(idx).get(i));
			}
			cbEquationGroups.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) { setGroup(e); } });
			cbEquationGroups.setSelectedIndex(0);
		}
		
		importFrame.pack();
		importFrame.repaint();
	}

/**
 * Set the constant or equation groups based on the selected category.
 * 
 * @param e	The event to be handled.
 */
	private void setGroup(ActionEvent e) {
		int i, j;
		String[] newGroups;
		JComboBox cb = (JComboBox) e.getSource();
		String bName = cb.getName();

		if (bName.equals("UnitGroup")) {
			if (cbUnitGroups.getSelectedItem().toString().equals("NONE"))
				return;
			i = cbSiteName.getSelectedIndex();
			j = cbUnitGroups.getSelectedIndex();
			newGroups = new String[updateData.Units.get(i).get(j).size()];
			updateData.Units.get(i).get(j).toArray(newGroups);
			cbUnits.removeAllItems();
			if (newGroups.length == 0)
				cbUnits.addItem("NONE");
			else {
				for (i=0; i < newGroups.length; i++) {
					cbUnits.addItem(newGroups[i]);
				}
			}
		} else if (bName.equals("ConstantGroup")) {
			if (cbConstantGroups.getSelectedItem().toString().equals("NONE"))
				return;
			i = cbSiteName.getSelectedIndex();
			j = cbConstantGroups.getSelectedIndex();
			newGroups = new String[updateData.Constants.get(i).get(j).size()];
			updateData.Constants.get(i).get(j).toArray(newGroups);
			cbConstants.removeAllItems();
			if (newGroups.length == 0)
				cbConstants.addItem("NONE");
			else {
				for (i=0; i < newGroups.length; i++) {
					cbConstants.addItem(newGroups[i]);
				}
			}
		} else if (bName.equals("EquationGroup")) {
			if (cbEquationGroups.getSelectedItem().toString().equals("NONE"))
				return;
			i = cbSiteName.getSelectedIndex();
			j = cbEquationGroups.getSelectedIndex();
			newGroups = new String[updateData.Equations.get(i).get(j).size()];
			updateData.Equations.get(i).get(j).toArray(newGroups);
			cbEquations.removeAllItems();
			if (newGroups.length == 0)
				cbEquations.addItem("NONE");
			else {
				for (i=0; i < newGroups.length; i++) {
					cbEquations.addItem(newGroups[i]);
				}
			}
		}

		importFrame.pack();
		importFrame.repaint();
	}

/**
 * Add a new site name and verify it has Convertator data.
 */
	private void addSiteEvent() {
		int i, selIdx, siteLen = updateData.SiteNames.size();
		String siteData;
		Date now;

		// Verify update if site exists
		for (i=0; i < cbSiteName.getItemCount(); i++) {
			if (cbSiteName.getItemAt(i).toString().equalsIgnoreCase(txSiteName.getText().trim()))
				break;
		}
		if (i < cbSiteName.getItemCount()) {
			if (cvtrMessages.yesnoDialog("SITE EXISTS", "The site " + txSiteName.getText().trim() + " exists.\nDo you want to replace it?") != JOptionPane.YES_OPTION) {
				return;
			}
		}
		// Add or update site info
		now = new Date();
		siteData = txSiteName.getText().trim() + ";" + txSiteDomain.getText().trim() + ";" + txSitePath.getText().trim() + ";" + now.toString();
		if (!updateData.parseSiteUpdate(siteData, true)) {
			cvtrMessages.errorDialog("ERROR", "Invalid site information");
			return;
		}
		// New site requires empty arrays
		if (siteLen < updateData.SiteNames.size()) {
			updateData.Units.add(new ArrayList<ArrayList<String>>());
			updateData.UnitGroups.add(new ArrayList<String>());
			updateData.UnitPaths.add(new ArrayList<ArrayList<String>>());
			updateData.UnitDates.add(new ArrayList<ArrayList<String>>());
			updateData.Units.get(siteLen).add(new ArrayList<String>());
			updateData.UnitPaths.get(siteLen).add(new ArrayList<String>());
			updateData.UnitDates.get(siteLen).add(new ArrayList<String>());
			updateData.Constants.add(new ArrayList<ArrayList<String>>());
			updateData.ConstantGroups.add(new ArrayList<String>());
			updateData.ConstantPaths.add(new ArrayList<ArrayList<String>>());
			updateData.ConstantDates.add(new ArrayList<ArrayList<String>>());
			updateData.Constants.get(siteLen).add(new ArrayList<String>());
			updateData.ConstantPaths.get(siteLen).add(new ArrayList<String>());
			updateData.ConstantDates.get(siteLen).add(new ArrayList<String>());
			updateData.Equations.add(new ArrayList<ArrayList<String>>());
			updateData.EquationGroups.add(new ArrayList<String>());
			updateData.EquationPaths.add(new ArrayList<ArrayList<String>>());
			updateData.EquationDates.add(new ArrayList<ArrayList<String>>());
			updateData.Equations.get(siteLen).add(new ArrayList<String>());
			updateData.EquationPaths.get(siteLen).add(new ArrayList<String>());
			updateData.EquationDates.get(siteLen).add(new ArrayList<String>());
		}
		
		// Update Site popup menu
		cbSiteName.removeActionListener(cbSiteName.getActionListeners()[0]);
		while (cbSiteName.getItemCount() > 0) {
			cbSiteName.removeItemAt(0);
		}
		selIdx = -1;
		for (i=0; i < updateData.SiteNames.size(); i++) {
			cbSiteName.addItem(updateData.SiteNames.get(i));
			if (updateData.SiteNames.get(i).equals(txSiteName.getText().trim()))
				selIdx = i;
		}
		cbSiteName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { newSiteEvent(); } });
		if (selIdx >= 0)
			cbSiteName.setSelectedIndex(selIdx);
		else
			cbSiteName.setSelectedIndex(0);
		importFrame.pack();
		importFrame.repaint();
	}

/**
 * Handle refreshing the site information.
 */
	private void refreshEvent() {
		int i, idx, sidx = cbSiteName.getSelectedIndex();
		String siteURL, result, fileData;
		String siteName, siteDomain, sitePath, siteDate;
		CvtrWeb cvtrWeb;

		if (updateData.SiteNames.size() == 0) {
			cvtrMessages.errorDialog("ERROR", "There are no sites listed");
			return;
		}

		// Get site update info
		cvtrWeb = new CvtrWeb(parentWindow.cvtrFile.homeDir);
		siteURL = updateData.SiteDomains.get(sidx) + updateData.SitePaths.get(sidx) + "/site.cvu";
		result = cvtrWeb.downloadFile(siteURL, "newSiteUpdate", "text");
		if (result == null) {
			cvtrMessages.errorDialog("ERROR", cvtrWeb.errorMessage);
			return;
		}
		// Parse site update info
		siteName = siteDomain = sitePath = siteDate = null;
		if ((fileData = parentWindow.cvtrFile.getTextFile(result)) != null) {
			if ((idx = fileData.indexOf(";")) > 0) {
				siteName = fileData.substring(0, idx);
				if ((i = idx + 1) <= fileData.length() && (idx = fileData.indexOf(";", i)) > 0) {
					siteDomain = fileData.substring(i, idx);
					if ((i = idx + 1) <= fileData.length() && (idx = fileData.indexOf(";", i)) > 0) {
						sitePath = fileData.substring(i, idx);
						if ((i = idx + 1) <= fileData.length()) {
							if ((idx = fileData.indexOf("\n")) < 0)
								idx = fileData.length();
							siteDate = fileData.substring(i, idx);
						}
					}
				}
			}
			// Validate the update info
			if (siteDate == null) {
				cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);
				parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
				return;
			} else if (!updateData.SiteNames.get(sidx).equals(siteName) ||
					!updateData.SiteDomains.get(sidx).equals(siteDomain) ||
					!updateData.SitePaths.get(sidx).equals(sitePath)) {
				result = "Mismatch in site data:\n  " +
					updateData.SiteNames.get(sidx) + ":\n    " + updateData.SiteDomains.get(sidx) + "\n    " + updateData.SitePaths.get(sidx) + "\n  " +
					siteName + ":\n    " + siteDomain + "\n    " + sitePath;
				cvtrMessages.errorDialog("ERROR", result);
				parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
				return;
			}
			// Check update date against last downloaded update information
			if (updateData.SiteDates.get(sidx).equals(siteDate)) {
				cvtrMessages.infoDialog("UPDATE", "There is no new update");
				parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
				return;
			}
		} else {
			cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);
			parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
			return;
		}

		// If valid and date has changed, get download info
		siteURL = updateData.SiteDomains.get(sidx) + updateData.SitePaths.get(sidx) + "/data.cvu";
		result = cvtrWeb.downloadFile(siteURL, "newSiteUpdate", "text");
		if (result == null) {
			cvtrMessages.errorDialog("ERROR", cvtrWeb.errorMessage);
			parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
			return;
		}
		// Get data from temp file
		if ((fileData = parentWindow.cvtrFile.getTextFile(result)) == null) {
			cvtrMessages.errorDialog("ERROR", "Error opening data information file:\n  " + updateData.errorMessage);
			parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
			return;
		// Parse the data
		} else if (!updateData.parseSiteData(sidx, fileData, true)) {
			cvtrMessages.errorDialog("ERROR", "Error parsing data information:\n  " + updateData.errorMessage);
			parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
			return;
		// Save the data in a file named for the site
		} else if (!parentWindow.cvtrFile.saveTextFile(fileData, siteName + "_data.cvu")) {
			cvtrMessages.errorDialog("ERROR", "Error saving data information:\n  " + parentWindow.cvtrFile.errorMessage);
			parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
			return;
		}

		// Save the new site list in the update file
		fileData = "";
		for (idx=0; idx < updateData.SiteNames.size(); idx++) {
			if (updateData.SiteNames.get(idx).equals(siteName))
					fileData += siteName + ";" + siteDomain + ";" + sitePath + ";" + siteDate + "\n";
			else
				fileData += updateData.SiteNames.get(idx) + ";" + updateData.SiteDomains.get(idx) + ";" +
					updateData.SitePaths.get(idx) + ";" + updateData.SiteDates.get(idx) + "\n";
		}
		if (!parentWindow.cvtrFile.saveTextFile(fileData, "site.cvu")) {
			cvtrMessages.errorDialog("ERROR", "Error saving updated site information:\n  " + parentWindow.cvtrFile.errorMessage);
		}
		// Delete temp file and select new site in popup menu
		parentWindow.cvtrFile.deleteFile("newSiteUpdate", null);
		updateData.SiteDates.set(sidx, siteDate);
		newSiteEvent();

	}

/**
 * Handle add name/value button.
 */
	private void downloadEvent() {
		int i, j, k;
		String dataURL, request, result, dataFile = null;
		String fType = "UNK";
		CvtrWeb cvtrWeb;
		CvtrData tData;

		i = cbSiteName.getSelectedIndex();
		dataURL = updateData.SiteDomains.get(i) + updateData.SitePaths.get(i) + "/";
		if (bUnit.isSelected()) {
			if (cbUnits.getSelectedItem().toString().equals("NONE")) {
				return;
			}
			fType = "data" + File.separator + "Units" + File.separator;
			j = cbUnitGroups.getSelectedIndex();
			k = cbUnits.getSelectedIndex();
			dataFile = cbUnits.getSelectedItem().toString();
			dataURL += updateData.UnitPaths.get(i).get(j).get(k);
			request = "Units -> " + updateData.UnitPaths.get(i).get(j).get(k);
		} else if (bConstant.isSelected()) {
			if (cbConstants.getSelectedItem().toString().equals("NONE")) {
				return;
			}
			fType = "data" + File.separator + "Constants" + File.separator;
			j = cbConstantGroups.getSelectedIndex();
			k = cbConstants.getSelectedIndex();
			dataFile = cbConstants.getSelectedItem().toString();
			dataURL += updateData.ConstantPaths.get(i).get(j).get(k);
			request = "Constants -> " + updateData.ConstantPaths.get(i).get(j).get(k);
		} else if (bEquation.isSelected()) {
			if (cbEquations.getSelectedItem().toString().equals("NONE")) {
				return;
			}
			fType = "data" + File.separator + "Equations" + File.separator;
			j = cbEquationGroups.getSelectedIndex();
			k = cbEquations.getSelectedIndex();
			dataFile = cbEquations.getSelectedItem().toString();
			dataURL += updateData.EquationPaths.get(i).get(j).get(k);
			request = "Equations -> " + updateData.EquationPaths.get(i).get(j).get(k);
		} else {
			cvtrMessages.errorDialog("ERROR", "Invalid download selection.");
			return;
		}
		// Verify download request
		i = cvtrMessages.yesnoDialog("REFRESH", "Do you want to download the data for:\n  " + request);
		if (i == JOptionPane.NO_OPTION)
			return;
		cvtrWeb = new CvtrWeb(parentWindow.cvtrFile.homeDir);
		result = cvtrWeb.downloadFile(dataURL, "newSiteData", "binary");
		if (result == null) {
			cvtrMessages.errorDialog("ERROR", cvtrWeb.errorMessage);
			return;
		} else if ((tData = parentWindow.cvtrFile.getCvtrFile(result)) == null) {
			cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);
			parentWindow.cvtrFile.deleteFile("newSiteData", null);
			return;
		} else if (!parentWindow.cvtrFile.saveCvtrFile(tData, parentWindow.cvtrFile.homeDir + fType + dataFile + ".cvd")) {
			cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);
			parentWindow.cvtrFile.deleteFile("newSiteData", null);
			return;
		}

		parentWindow.cvtrFile.deleteFile("newSiteData", null);
	}

}
