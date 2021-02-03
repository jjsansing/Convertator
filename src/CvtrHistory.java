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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * The Convertator History class allows undos in the equation text field.
 */
public class CvtrHistory extends AbstractAction
{
	static final long serialVersionUID = 0;
	CvtrWindow parentWindow = null;
	Action defAction;

	public CvtrHistory(Action a, CvtrWindow parent) {
		super("Convertator History");
		parentWindow = parent;
		defAction = a;
	}
	public void actionPerformed(ActionEvent e) {
		// Update the history
		parentWindow.historyUpdate();
		// Call the installed default action
		if (defAction != null) {
			defAction.actionPerformed(e);
		}
	}
} 

