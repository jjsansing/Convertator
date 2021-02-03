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

import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The Convertator Message class handles several different types of messages.
 * Each message type is displayed in a window that is anchored to the window
 * where the activity generating the message occurs.
 */
public class CvtrMessages
{
/** The parent window which is public to allow for it to be changed as needed. */
	public JFrame msgFrame = null;
/** The Convertator class for handling file operations in the help dialog method. */
	public CvtrFile cvtrFile = null;

 /**
  * The CvtrMessages constructor sets the parent window and the CvtrFile for
  * getting and handling data.
  * 
  * @param parent	The parent window where the message is generated.
  * @param fileHandler	The CvtrFile object for handling files.  Note that this is only used 
  * 					by the helpDialog method.  This allows for it to initially be null
  * 					if the file handler is not available when the messages class is created.
  */
	public CvtrMessages(JFrame parent, CvtrFile fileHandler) {
		msgFrame = parent;
		cvtrFile = fileHandler;
	}

/**
 * Display an information window with an OK button.
 * 
 * @param title	The window title
 * @param message	The message to be displayed
 */
	public void infoDialog(String title, String message) {
		JOptionPane.showMessageDialog(msgFrame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

/**
 * Display an error message in a window with an OK button.
 * 
 * @param title	The window title.
 * @param message	The message to be displayed.
 */
	public void errorDialog(String title, String message) {
		JOptionPane.showMessageDialog(msgFrame, message, title, JOptionPane.ERROR_MESSAGE);
	}

/**
 * Display a message in a window with an OK button.
 * 
 * @param title	The window title.
 * @param message	The message to be displayed.
 * 
 * @return int	The value of the selection.  See javax.swing.JOptionPane for the list
 * 				of selection values.
 */
	public int yesnoDialog(String title, String message) {
		return JOptionPane.showConfirmDialog(msgFrame, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

/**
 * Display window with help information.
 * 
 * @param title	The window title.
 * @param helpFile	The help filename.
 */
	public void helpDialog(String title, String helpFile) {
		String message = cvtrFile.getTextFile(helpFile);
		JFrame f;
		Point p;
		JTextArea ta;
		JScrollPane sp;

		if (message != null) {
			f = new JFrame(title);
			p = msgFrame.getLocation();
			ta = new JTextArea(message, 30, 40);
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			sp = new JScrollPane(ta);
	
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.add(sp);
			f.setLocation(p.x + 50, p.y + 50);
			f.pack();
			f.setVisible(true);
		} else
			errorDialog( "ERROR", cvtrFile.errorMessage);
	}

/**
 * Display window with help information.
 * 
 * @param title	The window title.
 * @param helpFile	The help information to be displayed.
 */
	public void helpStringDialog(String title, String message) {
		JFrame f;
		Point p;
		JTextArea ta;
		JScrollPane sp;

		if (message != null) {
			f = new JFrame(title);
			p = msgFrame.getLocation();
			ta = new JTextArea(message, 30, 30);
			ta.setLineWrap(true);
			ta.setWrapStyleWord(true);
			sp = new JScrollPane(ta);
	
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.add(sp);
			f.setLocation(p.x + 50, p.y + 50);
			f.pack();
			f.setVisible(true);
		} else
			errorDialog( "ERROR", cvtrFile.errorMessage);
	}

}
