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
 * The Convertator Data maintains information about the Data fields which are
 * stored in Convertator format files.  See the CvtrFile class for a description
 * of the file format.
 */
public class CvtrData
{
	private String dataCopyright = null;
/** The name of the category of these elements. */
	public String elementCategory = null;
/** The list of element names. */
	public String[] elementNames = null;
/** The list of element values. */
	public String[] elementValues = null;
	private boolean isUnits = false;
	private boolean isConstants = false;
	private boolean isEquations = false;
/** The flag indicating that characters are either UTF-8 (false) or UTF-16 (true). */
	public boolean isUnicode = false;

 /**
  * The Convertator Data constructor.
  */
	public CvtrData()
	{

	}

/**
 * The Convertator Data constructor validates the data type then sets it,
 * the copyright, and the category name.
 * 
 * @param copyright	The copyright string of the Data, which must be no larger
 *					than 8192 characters.
 * @param category	The category name of the data.
 * @param type	The data type (U=Units, C=Constants, E=Equations).
 * @param unicode	The format of data strings (true=Unicode, false=UTF-8)
 * 
 * @throws IllegalArgumentException	If the data type is invalid or the copyright
 *									length exceeds the maximum.
 */
	public CvtrData(String copyright, String category, String type, boolean unicode) throws Exception
	{
		IllegalArgumentException exception;

		// Validate the data type
		if (type.equalsIgnoreCase("U"))
			isUnits = true;
		else if (type.equalsIgnoreCase("C"))
			isConstants = true;
		else if (type.equalsIgnoreCase("E"))
			isEquations = true;
		else {
			exception = new IllegalArgumentException("Invalid data type");
			throw exception;
		}

		// Validate copyright length
		if (copyright.length() > 8192) {
			exception = new IllegalArgumentException("Copyright length exceeds maximum");
			throw exception;
		} else
			dataCopyright = copyright;

		elementCategory = category;
		isUnicode = unicode;
	}

/**
 * Validate that all required fields are set.
 *
 * @return String	The errors found or null if successful.
 */
	public String validateData()
	{
		String errors = "";

		if (elementCategory == null) {
			errors += "Category not defined\n";
		}
		if (getCopyright() == null) {
			errors += "Copyright not defined\n";
		}
		if (elementNames == null) {
			errors += "No entries defined\n";
		}
		if (elementValues == null) {
			errors += "No entry values defined\n";
		}
		if (!isUnits && !isConstants && !isEquations) {
			errors += "Data type not defined\n";
		}

		if (errors.length() > 0)
			return errors;
		else
			return null;
	}

/**
 * Set the requested data type flag to true and sets the others to false.  If the
 * input is not valid, all flags are set to false.
 *
  * @param type	The data type (U=Units, C=Constants, E=Equations).
 */
	public void setDataType(String type)
	{
		if (type.equalsIgnoreCase("U")) {
			isUnits = true;
			isConstants = false;
			isEquations = false;
		} else if (type.equalsIgnoreCase("C")) {
			isUnits = false;
			isConstants = true;
			isEquations = false;
		} else if (type.equalsIgnoreCase("E")) {
			isUnits = false;
			isConstants = false;
			isEquations = true;
		} else {
			isUnits = false;
			isConstants = false;
			isEquations = false;
		}
	}

/**
 * Get the data type where:
 * <ul>
 *   <li>U = Units</li>
 *   <li>C = Constants</li>
 *   <li>E = Equations</li>
 *   <li>blank = None</li>
 * </ul>
 *
  * @return String	The data type (U=Units, C=Constants, E=Equations).
 */
	public String getDataType()
	{
		if (isUnits) {
			return "U";
		} else if (isConstants) {
			return "C";
		} else if (isEquations) {
			return "E";
		} else {
			return "??";
		}
	}

/**
 * Set the Copyright text.  The length of the text may be no greater than 8192 characters.
 * The form of the notice should be:
 * <p/>
 * <ul>
 *   Copyright <i>date</i> <i>name</i>
 *   <p/>
 *   <i>Location of full text of copyright</i>
 * </ul>
 * <p/>
 * The default copyright used for Convertator data is the Creative Commons license:
 * <p/>
 *   <ul>Attribution-NonCommercial-ShareAlike 3.0</ul>
 * <p/>
 * This license is <a href="http://creativecommons.org/licenses/by-nc-sa/3.0">explained</a>
 * at the Creative Commons site, and includes the
 * <a href="http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode">full text</a>.
 * A copy of the full text is also delivered with the Convertator application.
 *
 * @param copyright	The copyright string for the Data.
 * 
 * @return boolean	False if the copyright length exceeds the maximum, otherwise true.
 */
	public boolean setCopyright(String copyright)
	{

		if (copyright.length() > 8192) {
			return false;
		}

		dataCopyright = copyright;
		
		return true;
	}

/**
 * Get the Copyright text.
 *
 * @return String	The copyright text.
 */
	public String getCopyright()
	{

		return dataCopyright;
	}

}
