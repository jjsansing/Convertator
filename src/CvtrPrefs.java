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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * The Convertator Preferences maintains information about equation result fields
 * so that the base and units may be changed easily.
 */
public class CvtrPrefs
{
	private CvtrWindow parentWindow = null;
	public JFrame prefFrame = null;
	private Container prefWindow = null;
	private GridBagLayout gbLayout = null;
	private boolean initPrefs = false;
	private CvtrMessages cvtrMessages = null;

/** The name to use in the copyright notice */
	public String ownerName = "anonymous";
	private JTextField txOwnerName = null;

/** The primary language to use. */
	public String primaryLang = "English";
	public String primaryLangAbbrev = "en";
	private JComboBox cbPrimaryLang = null;

/** The secondary language to use. */
	public String secondLang = "English";
	public String secondLangAbbrev = "en";
	private JComboBox cbSecondLang = null;

/** The directory containing sound files to use. */
	public String soundDir = "NONE";
	private JComboBox cbSoundDir = null;

/** The default download site to use. */
	public String defaultSite = "English";
	private JComboBox cbDefaultSite = null;

	private JButton bOK = null;
	private JButton bReset = null;
	private JButton bCancel = null;

/**
 * The Convertator Preferences constructor sets the class fields from the supplied parameters.
 * 
 * @param parent	The parent class.
 * @param fileHandler	The Convertator file handler.
 */
	public CvtrPrefs(CvtrWindow parent) {
		parentWindow = parent;
		prefFrame = new JFrame();
		prefFrame.setTitle("Preferences");
		prefWindow = prefFrame.getContentPane();
		gbLayout = new GridBagLayout();
		prefWindow.setLayout(gbLayout);
		Point p = parentWindow.getLocation();
		prefFrame.setLocation(p.x + 75, p.y + 30);
		cvtrMessages = new CvtrMessages(prefFrame, parent.cvtrFile);
	}

/**
 * Read preferences from the user's file.  If the file does not already
 * exist, create it.  Otherwise, parse the preferences and set their values.
 * 
 * @return boolean	True if successful, otherwise false.
 */
	public boolean getPreferences() {
		int i, from = 0, to;
		String prefList, prefItem;

		// If no prefs file, open prefs window.
		if ((prefList = parentWindow.cvtrFile.getTextFile(parentWindow.cvtrFile.homeDir + "preferences")) == null) {
			initPrefs = true;
			setPreferences();
			// Wait for preferences to be set
			try {
				while (initPrefs) {
					// Check once a second
					Thread.sleep(1000);
				}
			} catch (InterruptedException error) {
				;	// Ignore it
			}
			if ((prefList = parentWindow.cvtrFile.getTextFile(parentWindow.cvtrFile.homeDir + "preferences")) == null) {
				cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);
				return false;
			}
		}

		while (from < prefList.length()) {
			if ((to = prefList.indexOf("\n", from)) < 0)
				to = prefList.length();
			prefItem = prefList.substring(from, to);
			from = to + 1;
			i = prefItem.indexOf(":");
			// Parse preferences
			if (prefItem.substring(0, i).equals("OwnerName")) {
				ownerName = prefItem.substring(i + 1);
			} else if (prefItem.substring(0, i).equals("PrimaryLang")) {
				primaryLang = prefItem.substring(i + 1);
			} else 	if (prefItem.substring(0, i).equals("SecondLang")) {
				secondLang = prefItem.substring(i + 1);
			} else if (prefItem.substring(0, i).equals("SoundDirectory")) {
				soundDir = prefItem.substring(i + 1);
			} else if (prefItem.substring(0, i).equals("DefaultSite")) {
				defaultSite = prefItem.substring(i + 1);
			}
		}

		return true;
	}

/**
 * Open an interactive window for the user to set preferences.
 */
	public void setPreferences() {
		initPrefDialog();
	}

/**
 * Set preferences selected in the preferences dialog.
 */
	private void getNewPreferences() {
		String newPrefs = "";

		ownerName = txOwnerName.getText().toString();
		primaryLang = cbPrimaryLang.getSelectedItem().toString();
		secondLang = cbSecondLang.getSelectedItem().toString();
		soundDir = cbSoundDir.getSelectedItem().toString();
		defaultSite = cbDefaultSite.getSelectedItem().toString();

		newPrefs += "OwnerName:" + ownerName + "\n";
		newPrefs += "PrimaryLang:" + primaryLang + "\n";
		newPrefs += "SecondLang:" + secondLang + "\n";
		newPrefs += "SoundDirectory:" + soundDir + "\n";
		newPrefs += "DefaultSite:" + defaultSite + "\n";

		if (!parentWindow.cvtrFile.saveTextFile(newPrefs, "preferences"))
			cvtrMessages.errorDialog("ERROR", parentWindow.cvtrFile.errorMessage);

	}

/**
 * Reset preference dialog items to current selections.
 */
	private void resetPreferences() {

		txOwnerName.setText(ownerName);
		cbPrimaryLang.setSelectedItem(primaryLang);
		cbSecondLang.setSelectedItem(secondLang);
		cbSoundDir.setSelectedItem(soundDir);
		cbDefaultSite.setSelectedItem(defaultSite);

	}

/**
 * Create and display the preferences dialog window.
 */
	private void initPrefDialog() {
		int row = 0;
		String[] foo = {"NONE"};
		JLabel tLabel = null;

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(2,3,2,3);

		// Get owner name
		tLabel = new JLabel("Owner Name");
		c.gridwidth = 1; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(tLabel, c); prefWindow.add(tLabel);
		txOwnerName = new JTextField();
		c.gridwidth = 3; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(txOwnerName, c); prefWindow.add(txOwnerName);

		row++;
		// Get list of languages
// TODO: Get list of language directories from application directory
		tLabel = new JLabel("Primary Language");
		c.gridwidth = 1; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(tLabel, c); prefWindow.add(tLabel);
		cbPrimaryLang = new JComboBox(foo);
		c.gridwidth = 3; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(cbPrimaryLang, c); prefWindow.add(cbPrimaryLang);

		row++;
		// Get list of languages
// TODO: Get list of language directories from application directory
		tLabel = new JLabel("Secondary Language");
		c.gridwidth = 1; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(tLabel, c); prefWindow.add(tLabel);
		cbSecondLang = new JComboBox(foo);
		c.gridwidth = 3; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(cbSecondLang, c); prefWindow.add(cbSecondLang);

		row++;
		// Get list of sound directories
// TODO: Get sound directories from user and/or application directory
		tLabel = new JLabel("Sound Theme");
		c.gridwidth = 1; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(tLabel, c); prefWindow.add(tLabel);
		cbSoundDir = new JComboBox(foo);
		c.gridwidth = 3; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(cbSoundDir, c); prefWindow.add(cbSoundDir);

		row++;
		// Get list of download sites
// TODO: Get list of download sites from user and/or application directory
		tLabel = new JLabel("Default Site");
		c.gridwidth = 1; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(tLabel, c); prefWindow.add(tLabel);
		cbDefaultSite = new JComboBox(foo);
		c.gridwidth = 3; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(cbDefaultSite, c); prefWindow.add(cbDefaultSite);

		row++;
		JLabel bar = new JLabel(" ");
		c.gridwidth = 3; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(bar, c); prefWindow.add(bar);

		row++;
		// Display option buttons
		bOK = new JButton("OK");
		c.gridwidth = 1; c.gridx = 0; c.gridy = row;
		gbLayout.setConstraints(bOK, c); prefWindow.add(bOK);
		bOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initPrefs = false;
				getNewPreferences();
				stop();
			} });
		bReset = new JButton("Reset");
		c.gridwidth = 1; c.gridx = 1; c.gridy = row;
		gbLayout.setConstraints(bReset, c); prefWindow.add(bReset);
		bReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initPrefs = false;
				resetPreferences();
			} });
		bCancel = new JButton("Cancel");
		c.gridwidth = 1; c.gridx = 2; c.gridy = row;
		gbLayout.setConstraints(bCancel, c); prefWindow.add(bCancel);
		bCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initPrefs = false;
				stop();
			} });

		prefFrame.pack();
		prefFrame.setVisible(true);
	}

/**
 * Close the preferences dialog window.
 */
	public void stop() {
		prefFrame.setVisible(false);
		prefFrame.dispose();
		prefFrame = null;
	}

}
