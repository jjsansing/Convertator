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

/**
 * The Convertator application is a converting calculator.  This means that it
 * allows equations to be entered, with units assigned to numeric values, and
 * converts them to a common unit before solving the equation.  For example,
 * feet/second may be added to miles/hour. 
 */
public class Convertator
{
 /**
  *  The Convertator constructor.
  */
	public Convertator()
	{
	}

/**
 * The main method initializes the Convertator and places the window.
 * 
 * @param args	The arguments supplied when the application is started.  Currently
 *				there are no arguments supported.
 */
	public static void main(String[] args)
	{
		CvtrWindow cvtrWindow = new CvtrWindow();
		cvtrWindow.setLocation(300, 200);
		cvtrWindow.start();
	}
}
