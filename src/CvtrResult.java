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
 * The Convertator Result maintains information about equation result fields
 * so that the base and units may be changed easily.
 */
public class CvtrResult
{
/** The result base */
	public boolean resultBase = false;
/** The unit or base abbreviation */
	public String resultAbbrev = null;
/** The conversion value */
	public double conversionFactor = 0;
/** The category or base index */
	public int indexType = 0;
/** The unit index in the category */
	public int indexUnit = 0;

 /**
  * The Convertator Result constructor sets the class fields from the supplied parameters.
  * 
  * @param base	If true, the result base, otherwise a unit in the result.
  * @param abbrev	The unit or base abbreviation.
  * @param conversion	The conversion value of a unit.
  * @param typeIndex	The index of the result's Type.
  * @param unitIndex	The unit's index in the Unit array.
  */
	public CvtrResult(boolean base, String abbrev, double conversion, int typeIndex, int unitIndex)
	{
		resultBase = base;
		resultAbbrev = abbrev;
		conversionFactor = conversion;
		indexType = typeIndex;
		indexUnit = unitIndex;
	}

/**
 * Set a new base to be used to display the equation result.
 * 
 * @param baseIndex	The value of the new base index from the CvtrEngine.
 * @param baseAbbrev	The abbreviation of the new base from the CvtrEngine.
 */
	public void setBase(int baseIndex, String baseAbbrev) {
		if (resultBase) {
			indexType = baseIndex;
			resultAbbrev = baseAbbrev;
		}
	}

/**
 * Set a new base to be used to display the equation result.
 * 
 * @param unitIndex	The value of the new unit index.
 * @param unitAbbrev	The abbreviation of the new unit.
 * @param unitConversion	The conversion factor for the new unit.
 */
	public void setUnit(int unitIndex, String unitAbbrev, double unitConversion) {
		if (resultBase) {
			indexType = unitIndex;
			resultAbbrev = unitAbbrev;
			conversionFactor = unitConversion;
		}
	}

}
