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
 * The Convertator Unit maintains information about the Units field which is
 * used for equation conversions.
 */
public class CvtrUnit implements Comparable<CvtrUnit>
{
/** The string displayed in JComboBox lists. */
	public String displayName = null;
/** The abbreviation used in Equations.  This field is used to sort arrays of this class. */
	public String unitAbbrev = null;
/** The conversion value, which is relative to the other values in this Unit category. */
	public double conversionFactor = 0;
/** The category index */
	public int indexType = 0;
/** The unit index within the category */
	public int indexUnit = 0;

 /**
  * The Convertator Unit constructor validates the name then sets it and
  * the conversion factor.
  * 
  * @param display	The display string of the Unit in the format 'Name (Abbreviation)'.
  * @param conversion	The conversion value of the unit as a string.
  * @param typeIndex	The index of the unit's Type.
  * @param unitIndex	The unit's index in the Unit array.
  * @param keycodes	The list of key letters and symbols that may not be used as unit abbreviations.
  * 
  * @throws IllegalArgumentException	If the display string is not properly formatted.
  */
	public CvtrUnit(String display, String conversion, int typeIndex, int unitIndex, String keycodes) throws IllegalArgumentException {
		int i;
		double d;
		String codeList;
		IllegalArgumentException exception;

		if (keycodes == null)
			codeList = "";
		else
			codeList = keycodes;

		// Validate the display string
		if (display.indexOf(")") < 0) {
			exception = new IllegalArgumentException("Unit abbreviation not found.");
			throw exception;
		}
		i = display.indexOf("(");
		if (i < 0) {
			exception = new IllegalArgumentException("Unit abbreviation not found.");
			throw exception;
		}
		i++;
		displayName = display;
		unitAbbrev = display.substring(i, display.indexOf(")"));
		if (codeList.indexOf(unitAbbrev) >= 0) {
			exception = new IllegalArgumentException("Unit abbreviation matches a key code: " + unitAbbrev);
			throw exception;
		}
		
		try {
			d = Double.parseDouble(conversion);
			if (d == 0) {
				exception = new IllegalArgumentException("Conversion value must not equal zero.");
				throw exception;
			}
		} catch (NumberFormatException err) {
			exception = new IllegalArgumentException("Invalid conversion value.");
			throw exception;
		}
		conversionFactor = d;
		indexType = typeIndex;
		indexUnit = unitIndex;
	}

/**
 * Compares the abbreviation of two Convertator Units for sorting.
 * 
 * @param cu	The Convertator Unit to compare.
 * 
 * @return int	The comparison result: 0 = The Units are equal<br/>
 *				&lt; 0 = This unit is less than the compared unit<br/> 
 *				&gt; 0 = This unit is greater than the compared unit 
 */
	public int compareTo(CvtrUnit cu) {
		return (cu.unitAbbrev.compareTo(unitAbbrev));
	}

}
