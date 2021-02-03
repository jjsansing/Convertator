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
 * The Convertator Operand is a container for operand data.
 */
public class CvtrOperand
{
/** The value of the operand.  The default is 1 which is the case of a unit without a numeric value. */
	public double value = 1;
/** The String value of the operand if dotted decimal, ASCII, or Unicode. */
	public String sValue = null;
/** The index of the base within the Convertator Engine base array. */
	public int base = -1;
/** The Unit abbreviation used in Equations or Function code. */
	public String unit = "";
/** The conversion value, which is relative to the other values in the Unit category. */
	public double conversionFactor = 0;
/** The category index.  The default is -1 which is a number without any units. */
	public int indexType = -1;
/** The unit index within the category.  The default is -1 which is a number without any units. */
	public int indexUnit = -1;
/** The number of time same units are multiplied. */
	public int unitPower = 1;
/** The operation to be performed with the previous operand. */
	public String operation = "";
/** The level of parsing parentheses pairs. */
	public int nestLevel = 0;
/** The parsing groups to which the operand belongs at each parsing level up to the equation original level. */
	public int[] nestGroup = null;
/** The operand is place holder for two units multiplied together */ 
	public boolean groupUnit = false;
/** The operand is a function (ie. Sine). */
	public boolean function = false;
/** The level of parsing division. */
//	public int divLevel = 0;

/**
 * The CvtrOperand constructor creates the container for operands.  Note the following
 * intial settings:
 * <ul>
 *   <li>Numeric value = 1</li>
 *   <li>Base = -1 (not set)</li>
 *   <li>Category/Unit = -1 (not set)</li>
 *   <li>Unit power = 1</li>
 *   <li>All others are null, zero, or false.</li>
 * </ul>
 */
	public CvtrOperand() {

	}

/**
 * Duplicate the current Convertator operand.
 * 
 * @return CvtrOperand	The duplicated operand.
 */
	public CvtrOperand dupOperand() {
		int i;
		CvtrOperand oper = new CvtrOperand();
		
		oper.value = value;
		oper.sValue = sValue;
		oper.base = base;
		oper.unit = unit;
		oper.conversionFactor = conversionFactor;
		oper.indexType = indexType;
		oper.indexUnit = indexUnit;
		oper.unitPower = unitPower;
		oper.operation = operation;
		oper.function = function;
		oper.nestLevel = nestLevel;
		oper.nestGroup = new int[nestLevel + 1];
		for (i=0; i <= nestLevel; i++)
			oper.nestGroup[i] = nestGroup[i];
		oper.groupUnit = groupUnit;
//		oper.divLevel = divLevel;
		
		return oper;
	}

/**
 * Set the unit information so that no unit is associated with the operand.
 */
	public void clearUnit() {
		indexType = -1;
		indexUnit = -1;
		unit = "";
		unitPower = 1;
		
		return;
	}

}
