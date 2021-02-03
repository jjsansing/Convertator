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

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The primary function of the Convertator Engine class is to parse an equation that contains units,
 * convert the values to a common unit, calcalate the result, and convert it to the requested unit.
 * 
 * There is no operator precedence.  All operations are processed in the order in which they appear
 * unless enclosed in parentheses.
 *   
 * The class maintains the supported operators, bases, and precision.  These are provided in String
 * arrays to be displayed in selection lists.
 */
public class CvtrEngine
{
	static final boolean verbose = false;
	public CvtrFile cvtrFile = null;
	public String errorMessage = null;
	public CvtrResult[] EquationResult = null;

	private String[] sOperators = {"(", ")", "Add (+)", "Sub (-)", "Mult (*)", "Div (/)", "Mod (%)", "Pwr (^)", "SqRt (\\)",
			"Sine (S)", "Cos (O)", "Tngt (T)", "Log (L)", "ntLg (l)", "AND (&)", "OR (|)", "XOR (#)", "NOT (!)"};
	private String opList = " ()+-*/%^&|#!\\";
	// Group types must set none, addsub to 0 and 1 respectively
	final private static int none = 0, addsub = 1, mult = 2, div = 3;
	private String[] sBases = {"Binary (n#)", "Octal (o#)", "Decimal", "Hexadecimal (x#)", "Degrees (g#)", "Radians (r#)", "DottedDec (i#)", "ASCII (s'..')", "Unicode (u'..')"};
	private String[] digitList = {"01", "01234567", "0123456789.-", "0123456789abcdefABCDEF", "0123456789.-", "0123456789.-", "0123456789."};
	private int[] digitLen = {32, 10, 23, 8, 23, 23, 1000};
	final private static int bin = 0, oct = 1, dec = 2, hex = 3, deg = 4, rad = 5, dot = 6, asc = 7, uni = 8;
	final private static int ddBase = dot;
	private String[] sPrecision = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
	private int iPrecision = 0;
	private String keyCodes = "n(o)x+g-r*i/s%u^S&O|T#L!l\\d";
	public boolean scientificNotation = false;

/** The complete list of Units which can have a binary search performed on the abbreviation. */
	public CvtrUnit[] cUnits = null;
/** The list of Unit names, in order of category. */
	public ArrayList <String[]> UnitNames = new ArrayList<String[]>();
/** The list of Unit conversion values, in order of category. */
	public ArrayList <String[]> UnitValues = new ArrayList<String[]>();
/** The list of Unit categories. */
	public ArrayList <String> UnitCategories = new ArrayList<String>();
/** The list of Unit files, in order of category. */
	public ArrayList <String> UnitFiles = new ArrayList<String>();
/** The list of Constant names, in order of category. */
	public ArrayList <String[]> ConstantNames = new ArrayList<String[]>();
/** The list of Constant values, in order of category. */
	public ArrayList <String[]> ConstantValues = new ArrayList<String[]>();
/** The list of Constant categories. */
	public ArrayList <String> ConstantCategories = new ArrayList<String>();
/** The list of Constant files, in order of category. */
	public ArrayList <String> ConstantFiles = new ArrayList<String>();
/** The list of Equation names, in order of category. */
	public ArrayList <String[]> EquationNames = new ArrayList<String[]>();
/** The list of Equation values, in order of category. */
	public ArrayList <String[]> EquationValues = new ArrayList<String[]>();
/** The list of Equation categories. */
	public ArrayList <String> EquationCategories = new ArrayList<String>();
/** The list of Equation files, in order of category. */
	public ArrayList <String> EquationFiles = new ArrayList<String>();

// Global values for calculations
	private ArrayList <CvtrOperand> operandStack;
	private int nestLevel;
	private int metaNest;
	private int powerLevel;
	private int maxNesting;
// The maximum depth of nested parentheses
	final private static int maxNestLevel = 64;
	private int[] nestGroup = new int[maxNestLevel];
	private ArrayList <ArrayList<String>> groupCount;
	private String currentOperator;
	private int currentBase;
	private boolean powerOperator;
	private int quoteLocation;
	private boolean charEquation;
	private String calcError;
	private String calcProgress;
	
/**
 * The Convertator Engine constructor sets the parent class, which must provide the
 * following Dialog(JFrame parent, String title, String message) methods:
 * <ul>
 *   <li>infoDialog</li>
 *   <li>errorDialog</li>
 *   <li>yesnoDialog</li>
 * </ul>
 * <p/>
 * The Convertator File class must be created before the Engine, because it is
 * called to initialize the Engine data.
 * 
 * @throws IllegalArgumentException	If the conversion data cannot be read from the
 * 									application or user files (*.cvd).
 */
	public CvtrEngine() throws Exception {
		IllegalArgumentException exception;

		if (!initEngine()) {
			exception = new IllegalArgumentException(errorMessage);
			throw exception;
		}
	}

/**
 * Initialize the data for the Engine.
 * 
 * @return boolean	True if successful.
 * 					Otherwise, the reason is saved in errorMessage and false is returned.
 */
	public boolean initEngine() {

		try {
			cvtrFile = new CvtrFile();
		} catch (Exception err) {
			errorMessage = "Error creating Convertator file handler:\n  " + err.getMessage();
			return false;
		}
		if (!cvtrFile.getDataFiles(this)) {
			errorMessage = cvtrFile.errorMessage;
			return false;
		} else if (cvtrFile.errorMessage != null) {
			errorMessage = cvtrFile.errorMessage;
		}

		if (UnitCategories.size() == 0 || ConstantCategories.size() == 0 || EquationCategories.size() == 0)
			setDefaults();
		setUnits();

		return true;
	}

/**
 * Setting the Convertator engine defaults simply puts placeholders in every array.
 */
	public void setDefaults() {
		String[] names = {"NONE"}, values = {"NONE"};

		if (UnitCategories.size() == 0) {
			UnitNames.add(names);
			UnitValues.add(values);
			UnitCategories.add("NONE");
		}
		if (ConstantCategories.size() == 0) {
			ConstantNames.add(names);
			ConstantValues.add(values);
			ConstantCategories.add("NONE");
		}
		if (EquationCategories.size() == 0) {
			EquationNames.add(names);
			EquationValues.add(values);
			EquationCategories.add("NONE");
		}
	}

/**
 * Set the Units for fast searches when parsing Equations.
 * 
 * @return boolean	True if successful.
 * 					Otherwise, the reason is saved in errorMessage and false is returned.
 */
	private boolean setUnits() {
		int i = 0, j = 0, unitSize = 0, idx = 0;
		String[] uName = null, uValue = null;

		for (i=0; i < UnitCategories.size(); i++)
			unitSize += UnitNames.get(i).length;

		try {
			cUnits = new CvtrUnit[unitSize];
			for (i=0; i < UnitCategories.size(); i++) {
				j = 0;
				uName = UnitNames.get(i);
				uValue = UnitValues.get(i);
				for (; j < UnitNames.get(i).length; j++) {
					cUnits[idx++] = new CvtrUnit(uName[j], uValue[j], i, j, keyCodes);
				}
			}
		} catch (IllegalArgumentException err) {
			String errData;
			if (uName != null)
				errData = uName[j] + ", ";
			else
				errData = "Unknown, ";
			if (uValue != null)
				errData += uValue[j];
			else
				errData += "Unknown";
			errorMessage = "Error in Unit " + errData + ":\n\n  " + err.getMessage();
			return false;
		}

		// Sort the Units by abbreviation
		Arrays.sort(cUnits);

		return true;
	}

/**
 * Validate a unit abbreviation.
 * 
 * @param unit	The unit abbreviation to be validated.
 * 
 * @return boolean	True if the unit abbreviation is acceptable, false if it matches
 *					a Convertator keyword.
 */
	public boolean validateUnit(String unit) {

		return (keyCodes.indexOf(unit) < 0);
	}

/**
 * Get the supported operators.
 * 
 * @return String[]	The array of supported operators.
 */
	public String[] getOperators() {
		return sOperators;
	}

/**
 * Get the supported bases.
 * 
 * @return String[]	The array of supported bases.
 */
	public String[] getBases() {
		return sBases;
	}

/**
 * Get the default base.
 * 
 * @return String	The default base.
 */
	public String getDefaultBase() {
		return "Decimal";
	}

/**
 * Get the supported precision sizes.
 * 
 * @return String[]	The array of supported precisions.
 */
	public String[] getPrecision() {
		return sPrecision;
	}

/**
 * Set the current precision size.
 * 
 * @param precision	The selected precision
 */
	public int setPrecision(String precision) {
		int i;
		try {
			i = Integer.valueOf(precision);
			if (i < 0)
				iPrecision = 0;
			else if (i > 10)
				iPrecision = 10;
			else
				iPrecision = i;
		} catch (NumberFormatException e) {
			;	// Ignore errors
		}
		
		return iPrecision;
	}

/**
 * Get the operator sign.
 * 
 * @param operator	The operator display value.
 * 
 * @return String	The sign of the operator.
 */
	public String getOpSign(String operator) {
		int i;
		String opSign;

		if (operator.equals(")") || operator.equals("("))
			opSign = operator;
		else
		{
			i = operator.indexOf('(') + 1;
			opSign = operator.substring(i, operator.indexOf(')'));
		}

		return opSign;
	}

/**
 * Get the number of allowed digits for the supplied base.
 * 
 * @param base	The base to be displayed.
 * @param equation	If true the base sign is for the equation field,
 * 					otherwise it is for the result field.
 * 
 * @return int	The sign of the base which precedes the values.
 */
	public String getBaseSign(String base, boolean equation) {
		String baseSign = "";

		if (base.indexOf("Binary") == 0) {
			baseSign = "n";
		} else if (base.indexOf("Octal") == 0) {
			baseSign = "o";
		} else if (base.indexOf("Decimal") == 0) {
			if (equation) {
				baseSign = "m";
			}
		} else if (base.indexOf("Hexadecimal") == 0) {
			baseSign = "x";
		} else if (base.indexOf("Degrees") == 0) {
			baseSign = "g";
		} else if (base.indexOf("Radians") == 0) {
			baseSign = "r";
		} else if (base.indexOf("DottedDec") == 0) {
			baseSign = "i";
		} else if (base.indexOf("ASCII") == 0) {
			baseSign = "s''";
		} else if (base.indexOf("Unicode") == 0) {
			baseSign = "u''";
		}

		return baseSign;
	}

/**
 * Get the number of allowed digits for the supplied base.
 * 
 * @param base	The base to be displayed.
 * 
 * @return int	The number of allowed digits where the digits are:<br>
 *				0, 1, 2, 3, 4, 5, 6, 7, 8, 9, A, B, C, D, E, F
 */
	public int getAllowedDigits(String base) {
		int allowed = 0;

		if (base.indexOf("Binary") == 0) {
			allowed = 2;
		} else if (base.indexOf("Octal") == 0) {
			allowed = 8;
		} else if (base.indexOf("Decimal") == 0 || base.indexOf("DottedDec") == 0) {
			allowed = 10;
		} else if (base.indexOf("Hexadecimal") == 0 || base.indexOf("ASCII") == 0 || base.indexOf("Unicode") == 0) {
			allowed = 16;
		}

		return allowed;
	}

/**
 * Get the Unit abbreviation from the displayed name.
 * 
 * @param uName	The displayed unit name in the user interface list of units.
 * 
 * @return String	The abbreviation of the unit.
 */
	public String getUnitAbbrev(String uName) {
		int i, j;

		if ((i = uName.indexOf("(")) < 0 || (j = uName.indexOf(")")) < 0 || j < i) {
			return "";
		} else {
			return uName.substring(i + 1, j);
		}
	}

/**
 * Get the CvtrUnit element from the abbreviation of the displayed name.
 * 
 * @param uName	The abbreviation of the unit.
 * 
 * @return CvtrUnit	The unit information.  If the unit is not found, null is returned.
 */
	public CvtrUnit getUnit(String uName) {
		int i;
		CvtrUnit cu;

		try {
			cu = new CvtrUnit("temp (" + uName + ")", "1", 0, 0, keyCodes);
			if ((i = Arrays.binarySearch(cUnits, cu)) >= 0) {
				return cUnits[i];
			}
		} catch (IllegalArgumentException err) {
			calcError = "?? Unit " + uName;
			return null;
		}

		calcError = "?? Unit " + uName;
		return null;
	}

/**
 * Get the calculation error message if the return value from calculate is -1.
 * 
 * @return String	The error message.
 */
	public String getCalcError() {
		return calcError;
	}

/**
 * Initialize the global values maintained for equations at the beginning of each calculation.
 */
	private void initCalc() {
		int i;

		operandStack = new ArrayList<CvtrOperand>();
		for (i=0; i < maxNestLevel; i++)
			nestGroup[i] = 0;
		maxNesting = 0;
		groupCount = new ArrayList<ArrayList<String>>();
		groupCount.add(new ArrayList<String>());
		groupCount.get(0).add("0");
		calcError = "?? Internal";
		currentOperator = "";
		currentBase = -1;
		nestLevel = 0;
		metaNest = 0;
		powerLevel = 0;
		powerOperator = false;
		EquationResult = null;
		quoteLocation = -1;
		charEquation = false;
	}

/**
 * Increment or decrement the nested group count for the specified nesting level and group.
 * The nested group count is a two dimensional array list.
 * 
 * @param x	The nestLevel of the group.
 * @param y	The nestGroup of the group.
 * @param increment	If true, the value is incremented, otherwise it is decremented.  If the
 *					result is less than 0, the value is set to 0.
 * 
 * @return boolean	If the x, y values result in an IndexOutOfBoundsException, the class
 *					error code is set and false is returned.
 */
	private boolean modifyGroupCount(int x, int y, boolean increment) {
		int i;

		// Add elements, if necessary, to the specified location in the 2D array
		while (groupCount.size() <= x) {
			groupCount.add(new ArrayList<String>());
			groupCount.get(groupCount.size() - 1).add("0");
		}
		while (groupCount.get(x).size() <= y) {
			groupCount.get(x).add("0");
		}
		try {
			i = Integer.parseInt(groupCount.get(x).get(y).toString());
			if (increment)
				i++;
			else
				i--;
			if (i < 0)
				i = 0;
			groupCount.get(x).set(y, "" + i);
		} catch (IndexOutOfBoundsException err) {
			calcError = "?? Internal";
			return false;
		}

		return true;
	} /* end modifyGroupCount */

/**
 * Perform calculations on the Equation string.  This routine is recursive as needed.
 * 
 * @param equation	The equation to be calculated.
 * @param index	The location in the equation to start parsing.  This is used for calling
 *				the method recursively and the original call should always use zero.
 * 
 * @return int	The location in the equation left to parse.  If -1, there was an error.
 *				Success is indicated by a return value that equals the length of the equation.
 */
	public int calculate(String equation, int index) {
		int i, newIndex = index;
		String temp;
		CvtrOperand oper = new CvtrOperand();
if (verbose)
System.out.println("Calculate: " + equation.substring(index));
		// Initialize the operand stack and global calculation values
		if (index == 0) {
			initCalc();
			calcProgress = equation + "\n";
		}

		while (newIndex < equation.length()) {
			temp = equation.substring(newIndex, newIndex + 1);
			// Skip spaces
			if (temp.equals(" ")) {
				newIndex++;
				continue;
			}
			// Parsing a string
			if (newIndex < quoteLocation) {
				if ((newIndex = parseValue(equation, newIndex)) == -1)
					return -1;
			// Open parentheses generates a recursive call to calculate
			} else if (temp.equals("(")) {
				newIndex++;
				nestLevel++;
				if (nestLevel >= groupCount.size()) {
					groupCount.add(new ArrayList<String>());
					groupCount.get(nestLevel).add("0");
				}
				if (maxNesting < nestLevel)
					maxNesting = nestLevel;
				if (nestLevel > maxNestLevel) {
					calcError = "?? Max ()";
					return -1;
				}
if (verbose)
System.out.println(" Open Paren level = " + nestLevel);
				if ((newIndex = calculate(equation, newIndex)) == -1)
					return -1;
			// Close parentheses must have a match with open parens
			} else if (temp.equals(")")) {
				currentOperator = "";
				newIndex++;
				nestGroup[nestLevel]++;
				groupCount.get(nestLevel).add("0");
				nestLevel--;
				if (metaNest > 0) {
					nestGroup[nestLevel]++;
					groupCount.get(nestLevel).add("0");
					nestLevel--;
					metaNest--;
				}
				if (nestLevel < 0) {
					calcError = "?? )";
					return -1;
				}
if (verbose)
System.out.println(" Close Paren level = " + nestLevel + ", " + nestGroup[nestLevel]);
				return newIndex;
			// Test for operators
			} else if (temp.equals("+") || temp.equals("-") ||
						temp.equals("*") || temp.equals("/") ||
						temp.equals("&") || temp.equals("|") ||
						temp.equals("%") || temp.equals("#")) {
				// Operand must preceed operator
				if (operandStack.size() == 0) {
					// First number is negative
					if (temp.equals("-")) {
						if ((newIndex = parseValue(equation, newIndex)) == -1) {
							return -1;
						}
					} else {
						calcError = "?? " + temp;
						return -1;
					}
				} else {
					i = operandStack.size() - 1;
					if (!powerOperator && !operandStack.get(i).function && currentOperator.length() == 0) {
						currentOperator = temp;
						newIndex++;
					// Minus sign allowed as first character of number
					} else if (temp.equals("-")) {
						// Check for new open parenthesis, function, or unary operator
						i = operandStack.size() - 1;
						// Space after minus sign, treat as operator
						if (newIndex < (equation.length() - 1) && equation.substring(newIndex + 1, newIndex + 2).equals(" ")) {
							i = -1;
							newIndex++;
						// Normal processing
						} else if (operandStack.get(i).nestLevel == nestLevel && operandStack.get(i).nestGroup != null &&
								operandStack.get(i).nestGroup[operandStack.get(i).nestLevel] == nestGroup[operandStack.get(i).nestLevel])
							i = 0;
						// Minus sign follows parenthesis
						else
							i = 1;
						if (i >= 0 && (newIndex = parseValue(equation, newIndex)) == -1)
							return -1;
						// Operator is always valid after open parenthesis
						if (i > 0) {
							currentOperator = "";
						} else if (i < 0) {
							currentOperator = temp;
						}
					// Test that operations are enclosed in parentheses
					} else if (!currentOperator.equals(temp)) {
						if (!verifyOperation(currentOperator, temp)) {
							return -1;
						}
						currentOperator = temp;
						newIndex++;
					} else if (temp.equals("/") && currentOperator.equals("/")) {
						calcError = "?? (//)";
						return -1;
					} else {
						newIndex++;
					}
				}
if (verbose)
System.out.println(" Current operator: " + currentOperator);
			// Test for power operator
			} else if (temp.equals("^")) {
				newIndex++;
				powerLevel = nestLevel;
				powerOperator = true;
if (verbose)
System.out.println(" Power operator level: " + nestLevel);
			// Test for functions
			} else if (temp.equals("S") || temp.equals("O") || temp.equals("T") ||
						temp.equals("L") || temp.equals("l") ||
						temp.equals("\\") || temp.equals("!")) {
				newIndex++;
				oper = new CvtrOperand();
				oper.operation = temp;
				oper.function = true;
				oper.sValue = temp;
				// Create a function group
				nestLevel++;
				metaNest++;
				if (nestLevel >= groupCount.size()) {
					groupCount.add(new ArrayList<String>());
					groupCount.get(nestLevel).add("0");
				}
				if (maxNesting < nestLevel)
					maxNesting = nestLevel;
				if (nestLevel > maxNestLevel) {
					calcError = "?? Max ()";
					return -1;
				}
				oper.nestLevel = nestLevel;
				oper.nestGroup = new int[nestLevel + 1];
				for (i=0; i < (nestLevel + 1); i++) {
					oper.nestGroup[i] = nestGroup[i];
				}
				operandStack.add(oper);
if (verbose)
System.out.println(" Function: " + oper.sValue + ", Nesting: " + nestLevel + ", " + nestGroup[nestLevel]);
			} else {
				// Check for new open parenthesis
				i = operandStack.size() - 1;
				if (i >= 0 && operandStack.get(i).nestLevel == nestLevel && operandStack.get(i).nestGroup != null &&
						operandStack.get(i).nestGroup[operandStack.get(i).nestLevel] == nestGroup[operandStack.get(i).nestLevel])
					i = 0;
				else
					i = 1;
				if ((newIndex = parseValue(equation, newIndex)) == -1)
					return -1;
				// Operator is always valid after open parenthesis
				if (i == 1) {
					currentOperator = "";
				}
			}
		}

		// Equation includes strings, simply display them
		if (charEquation) {
			if (getResultUnits() == null) {
				return -1;
			}
			return newIndex;
		}

		// Verify that all parentheses were paired
if (verbose)
System.out.println(" == Nest level: " + nestLevel + ", Meta nest: " + metaNest);
	// if (newIndex == equation.length() && (metaNest == 0 && nestLevel > 0)) {
		if (newIndex == equation.length() && metaNest != nestLevel) {
			calcError = "?? (";
			return -1;
		}

		// Check stack state
		if (operandStack.size() == 0)
			return -1;
		else if (operandStack.size() == 1) {
			if (getResultUnits() == null) {
				return -1;
			}
			return newIndex;
		}
		// Get greatest unit conversion factor (smallest unit) for each category
		calcProgress += "  Unit conversion:\n";
		if (!reduceUnits())
			return -1;
		// Test for operations or functions in equation
		for (i=0; i < operandStack.size(); i++) {
			if (operandStack.get(i).function || operandStack.get(i).operation.length() > 0)
				break;
		}
		// Equation includes operands
		if (i < operandStack.size()) {
			calcProgress += displayStack(operandStack) + "\n  Initial reduction:\n";
			// Calculate the result
			calcError = "";
			if ((operandStack = getEquationNesting(0, maxNesting, operandStack)) == null || calcError.length() > 0)
				return -1;
			calcProgress += displayStack(operandStack) + "\n  Group reduction:\n";
			if (!finalEquation())
				return -1;
		}
		// Get the equation format list
		if (getResultUnits() == null) {
			return -1;
		}

		return newIndex;
	} /* end calculate */

/**
 * Parse the value of the operand and perform any operations based operands currently
 * on the stack at the same level.
 * 
 * @param equation	The equation being calculated.
 * @param index	The location in the equation to start parsing the value.
 * 
 * @return int	The location in the equation left to parse.  If -1, there was an error.
 */
	private int parseValue(String equation, int index) {
		int i, j, idx, newIndex = index, pLen;
		long l, l1;
		double d;
		byte[] b;
		boolean setPower = false;
		String number = "", temp;
		CvtrOperand oper;
		CvtrUnit cu;
if (verbose)
System.out.println("Parse value: " + equation.substring(index));

		if ((pLen = equation.substring(index).length()) == 0) {
			newIndex++;
			return newIndex;
		}

		// Get the operand container
		oper = new CvtrOperand();
		oper.operation = currentOperator;
		oper.nestLevel = nestLevel;
		oper.nestGroup = new int[nestLevel + 1];
		for (i=0; i < (nestLevel + 1); i++) {
			oper.nestGroup[i] = nestGroup[i];
		}
// sBases = "Binary (n)", "Octal (o)", "Decimal (m)", "Hexadecimal (x)",
//          "Degrees (g)", "Radians (r)",
//          "DottedDec (i)", "ASCII (s')", "Unicode (u')"
		if (pLen > 1 && newIndex > quoteLocation) {
			if (equation.substring(newIndex, newIndex + 1).equals("n")) {
				if (pLen == 1) {
					calcError = "?? n|";
					return -1;
				}
				temp = equation.substring(newIndex + 1, newIndex + 2);
				if (temp.equals(" ") || digitList[bin].indexOf(temp) >= 0) {
					oper.base = bin;
					newIndex++;
				} else {
					calcError = "?? n|";
					return -1;
				}
			} else if (equation.substring(newIndex, newIndex + 1).equals("o")) {
				if (pLen == 1) {
					calcError = "?? o|";
					return -1;
				}
				temp = equation.substring(newIndex + 1, newIndex + 2);
				if (temp.equals(" ") || digitList[oct].indexOf(temp) >= 0) {
					oper.base = oct;
					newIndex++;
				} else {
					calcError = "?? o|";
					return -1;
				}
			} else if (equation.substring(newIndex, newIndex + 1).equals("m")) {
				if (pLen == 1) {
					calcError = "?? m|";
					return -1;
				}
				temp = equation.substring(newIndex + 1, newIndex + 2);
				if (temp.equals(" ") || digitList[oct].indexOf(temp) >= 0) {
					oper.base = dec;
					newIndex++;
				} else {
					calcError = "?? m|";
					return -1;
				}
			} else if (equation.substring(newIndex, newIndex + 1).equals("x")) {
				if (pLen == 1) {
					calcError = "?? x|";
					return -1;
				}
				temp = equation.substring(newIndex + 1, newIndex + 2);
				if (temp.equals(" ") || digitList[hex].indexOf(temp) >= 0) {
					oper.base = hex;
					newIndex++;
				} else {
					calcError = "?? x|";
					return -1;
				}
			} else if (equation.substring(newIndex, newIndex + 1).equals("g")) {
				if (pLen == 1) {
					calcError = "?? g|";
					return -1;
				}
				temp = equation.substring(newIndex + 1, newIndex + 2);
				if (temp.equals(" ") || digitList[deg].indexOf(temp) >= 0) {
					oper.base = deg;
					newIndex++;
				} else {
					calcError = "?? g|";
					return -1;
				}
			} else if (equation.substring(newIndex, newIndex + 1).equals("r")) {
				if (pLen == 1) {
					calcError = "?? r|";
					return -1;
				}
				temp = equation.substring(newIndex + 1, newIndex + 2);
				if (temp.equals(" ") || digitList[rad].indexOf(temp) >= 0) {
					oper.base = rad;
					newIndex++;
				} else {
					calcError = "?? r|";
					return -1;
				}
			} else if (equation.substring(newIndex, newIndex + 1).equals("i")) {
				if (pLen == 1) {
					calcError = "?? i|";
					return -1;
				}
				oper.base = dot;
				temp = equation.substring(newIndex + 1, newIndex + 2);
				if (temp.equals(" ") || digitList[dot].indexOf(temp) >= 0) {
					oper.base = dot;
					newIndex++;
				} else {
					calcError = "?? i|";
					return -1;
				}
			} else if (pLen > 2 && equation.substring(newIndex, newIndex + 2).equals("s'")) {
				if (pLen == 2) {
					calcError = "?? s'|";
					return -1;
				}
				oper.base = asc;
				newIndex += 2;
				quoteLocation = equation.substring(newIndex).indexOf("'");
				if (quoteLocation < 0)
					quoteLocation = equation.length();
				else
					quoteLocation += newIndex;
			} else if (pLen > 2 && equation.substring(newIndex, newIndex + 2).equals("u'")) {
				if (pLen == 2) {
					calcError = "?? u'|";
					return -1;
				}
				oper.base = uni;
				newIndex += 2;
				quoteLocation = equation.substring(newIndex).indexOf("'");
				if (quoteLocation < 0)
					quoteLocation = equation.length();
				else
					quoteLocation += newIndex;
			} else {
				if (currentBase < 0) {
					temp = equation.substring(newIndex, newIndex + 1);
					try {
						if (!temp.equals("-"))
							Integer.parseInt(temp);
					} catch (NumberFormatException e) {
						calcError = "?? " + temp;
						return -1;
					}
					currentBase = dec;
				}
				oper.base = currentBase;
			}
			if (oper.base < 0) {
				oper.base = currentBase;
			} else {
				currentBase = oper.base;
			}
		} else {
			if (currentBase < 0) {
				temp = equation.substring(newIndex, newIndex + 1);
				try {
					Integer.parseInt(temp);
					currentBase = dec;
				} catch (NumberFormatException e) {
					calcError = "?? " + temp;
					return -1;
				}
			}
			oper.base = currentBase;
		}

		// Get the operand value for numbers
if (verbose)
System.out.println("  Base: " + oper.base);
		while (newIndex < equation.length() && equation.substring(newIndex, newIndex + 1).equals(" "))
			newIndex++;
		if (oper.base < ddBase) {
			idx = newIndex;
			boolean numberEnd = false;
			while (idx < equation.length()) {
				temp = equation.substring(idx, idx + 1);
				// Special case negative decimal numbers
				if ((oper.base == dec || oper.base == deg || oper.base == rad) &&
						number.length() == 0 && temp.equals("-")) {
					number += temp;
					idx++;
				// Character is not numeric
				} else if (digitList[oper.base].indexOf(temp) < 0) {
					numberEnd = true;
				} else {
					number += temp;
					idx++;
				}
				// No number entered, assume value is 1
				if (number.length() == 0)
					break;
				// Only read an integer's worth of digits (digitLen) per operand
				if (numberEnd || number.length() == digitLen[oper.base] || idx == equation.length()) {
					newIndex = idx;
					try {
						if (oper.base == bin) {
							l = Long.parseLong(number, 2);
							oper.value = l;
						} else if (oper.base == oct) {
							l = Long.parseLong(number, 8);
							oper.value = l;
						} else if (oper.base == dec) {
							oper.value = new Double(number);
						} else if (oper.base == hex) {
							l = Long.parseLong(number, 16);
							oper.value = l;
						} else {
							oper.value = new Double(number);
						}
					} catch (NumberFormatException err) {
						calcError = "?? " + number;
						return -1;
					}
					break;
				}
			}
			if (oper.base == deg) {
				d = Math.toRadians(oper.value % 360);
				oper.value = d;
				oper.base = rad;
			}
		// Get the operand value for dotted decimal (initially string)
		} else if (oper.base == dot) {
			oper.sValue = "";
			l = 0;
			j = 0;
			temp = "";
			idx = newIndex;
			try {
				while (j < 4) {
					if (idx < equation.length())
						number = equation.substring(idx, idx + 1);
					if (idx == equation.length() || number.equals(".") || number.equals(" ")) {
						l <<= 8;
						l1 = (long) Long.parseLong(temp);
						if (l1 > 255) {
							calcError = "?? dd>255";
							return -1;
						} else if (l1 < 0) {
							calcError = "?? dd<0";
							return -1;
						}
						l |= l1;
						oper.sValue += temp;
						if (idx == equation.length() || number.equals(" "))
							break;
						if (j < 3)
							oper.sValue += number;
						idx++;
						temp = "";
						j++;
					} else {
						temp += number;
						idx++;
					}
				}
			} catch (NumberFormatException err) {
				calcError = "?? #.#";
				return -1;
			}
			oper.value = l;
			newIndex = idx;
			charEquation = true;
if (verbose)
System.out.println("  Dotted decimal: " + oper.sValue);
			if (!operandStack.add(oper)) {
				calcError = "?? Stack Err";
				return -1;
			}
			if (newIndex == index)
				newIndex++;
			return newIndex;
		// Get the operand value for ASCII or Unicode strings
		} else {
if (verbose)
System.out.println(" == Index: " + newIndex + ", qLoc: " + quoteLocation);
			if (newIndex < quoteLocation) {
				if (equation.substring(newIndex, quoteLocation).length() > 4) {
					oper.sValue = equation.substring(newIndex, newIndex + 4);
					newIndex += 4;
				} else {
					oper.sValue = equation.substring(newIndex, quoteLocation);
					newIndex = quoteLocation + 1;
				}
				try {
					if (oper.base == asc) {
						b = oper.sValue.getBytes("ASCII");
					} else if (oper.base == uni) {
						b = oper.sValue.getBytes("UTF-16");
					} else {
						calcError = "?? Base";
						return -1;
					}
				} catch (UnsupportedEncodingException err) {
					calcError = "?? 'Char Code'";
					return -1;
				}
				l = 0;
				for (i=0; i < b.length; i++) {
					l <<= 8;
					l |= (long) b[i];
				}
				oper.value = l;
			} else {
				newIndex++;
			}
			charEquation = true;
if (verbose)
System.out.println("  String: " + oper.sValue);
			if (!operandStack.add(oper)) {
				calcError = "?? Stack Err";
				return -1;
			}
			if (newIndex == index)
				newIndex++;
			return newIndex;
		}

		// Handle power operator possiblities: a^b, (a+b)^c, a^(b+c), (a+b)^(c+d)
		if (powerOperator) {
if (verbose)
System.out.println("  Power nest level: " + nestLevel);
			// No power operand
			i = operandStack.size() - 1;
			if (i < 0) {
				calcError = "?? ^" + oper.value;
				return -1;
			}
			oper.operation = "^";
			powerOperator = false;
			while (newIndex < equation.length() && equation.substring(newIndex, newIndex + 1).equals(" "))
				newIndex++;
			// Power operand is a group
			if (powerLevel != operandStack.get(i).nestLevel ||
					nestGroup[powerLevel] != operandStack.get(i).nestGroup[operandStack.get(i).nestLevel]) {
if (verbose)
System.out.println("  Power operand is group");
				idx = operandStack.get(i).nestLevel;
				j = operandStack.get(i).nestGroup[idx];
				while (i > 0 && (powerLevel != operandStack.get(i).nestLevel ||
						nestGroup[powerLevel] != operandStack.get(i).nestGroup[operandStack.get(i).nestLevel])) {
					i--;
				}
				// Create a power group
				nestLevel++;
				metaNest++;
				powerLevel++;
				if (nestLevel >= groupCount.size()) {
					groupCount.add(new ArrayList<String>());
					groupCount.get(nestLevel).add("0");
				}
				if (maxNesting < nestLevel)
					maxNesting = nestLevel;
				if (nestLevel > maxNestLevel) {
					calcError = "?? Max ()";
					return -1;
				}
				while (i < operandStack.size() && operandStack.get(i).nestLevel >= idx &&
						operandStack.get(i).nestGroup[operandStack.get(i).nestLevel] == nestGroup[idx]) {
					operandStack.get(i).nestLevel = nestLevel;
					operandStack.get(i).nestGroup = new int[nestLevel + 1];
					for (j=0; j < (nestLevel + 1); j++) {
						operandStack.get(j).nestGroup[j] = nestGroup[j];
					}
					i++;
				}
				oper.nestLevel = nestLevel;
				oper.nestGroup = new int[nestLevel + 1];
				for (i=0; i < (nestLevel + 1); i++) {
					oper.nestGroup[i] = nestGroup[i];
				}
			}
			// Power value is a group
			if (powerLevel != oper.nestLevel) {
if (verbose)
System.out.println("  Power value is group");
				if (!verifyOperation(currentOperator, "^")) {
					return -1;
				}
			} else {
				if (metaNest > 0) {
					nestGroup[nestLevel]++;
					groupCount.get(nestLevel).add("0");
					nestLevel--;
					metaNest--;
					if (nestLevel < 0) {
						calcError = "?? )";
						return -1;
					}
				} else {
					setPower = true;
					d = Math.pow(operandStack.get(i).value, oper.value);
if (verbose)
System.out.println("  Calculate power: " + operandStack.get(i).value + "^" + oper.value + " = " + d);
					operandStack.get(i).value = d;
					// Set the unit power to the integer value of the power because
					// fractional powers of units is not supported
					if (operandStack.get(i).indexType >= 0)
						operandStack.get(i).unitPower = (int) oper.value;
				}
			}
		}

		// Get unit information
		while (newIndex < equation.length() && equation.substring(newIndex, newIndex + 1).equals(" ")) {
			newIndex++;
		}
		idx = newIndex;
		temp = "";
		while (idx < equation.length() && opList.indexOf(equation.substring(idx, idx + 1)) < 0) {
			temp += equation.substring(idx, idx + 1);
			idx++;
		}
		if (temp.length() > 0) {
if (verbose)
System.out.println(" == Get unit: " + temp);
			// Might not be units
			if ((cu = getUnit(temp)) != null) {
				if (setPower) {
					calcError = "?? ^" + temp;
					return -1;
				}
				newIndex = idx;
				oper.unit = temp;
				oper.indexType = cu.indexType;
				oper.indexUnit = cu.indexUnit;
				oper.conversionFactor = cu.conversionFactor;
			}
		}
		if (setPower)
			return newIndex;

		// Test for function to be calculated
		if ((i = operandStack.size() - 1) >= 0) {
			if (operandStack.get(i).function && operandStack.get(i).nestLevel == nestLevel) {
				try {
					currentOperator = "";
					oper.value = calculateFunction(operandStack.get(i).sValue, oper);
					operandStack.remove(i);
					nestGroup[nestLevel]++;
					groupCount.get(nestLevel).add("0");
					nestLevel--;
					metaNest--;
					if (nestLevel < 0) {
						calcError = "?? )";
						return -1;
					}
				} catch (IllegalArgumentException err) {
					calcError = "?? " + err.getMessage();
					return -1;
				}
			}
		}

		// Put the new operand on the stack
if (verbose) {
System.out.print("  Put value on stack: " + oper.value + oper.unit + "^" + oper.unitPower + ", " + oper.operation + " (" + oper.nestLevel + ", ");
for (i=oper.nestLevel; i >= 0; i--)
  System.out.print(">" + oper.nestGroup[i]);
System.out.println(")");
}
		if (!operandStack.add(oper)) {
			calcError = "?? Stack Err";
			return -1;
		}
		if (!modifyGroupCount(oper.nestLevel, oper.nestGroup[oper.nestLevel], true))
			return -1;

		if (newIndex == index)
			newIndex++;
		return newIndex;
	} /* end parseValue */

/**
 * Reduce the units of an equation operand stack to the smallest common unit.
 * 
 * @return boolean	True for success, false if an error occurs.
 */
	private boolean reduceUnits() {

		int i, idx;
		int typeCount = 0;
		int[] types = new int[UnitCategories.size()];
		int[] units = new int[UnitCategories.size()];
		String[] uAbbrev = new String[UnitCategories.size()];
		double[] unitFactor = new double[UnitCategories.size()];
		double c1, c2, d;
if (verbose)
System.out.println("Reduce units");

		// Get greatest unit conversion factor (smallest unit) for each category
		for (idx=0; idx < operandStack.size(); idx++) {
			// Verify operand has a Unit Category
			if (operandStack.get(idx).indexType < 0)
				continue;
			if (typeCount == 0) {
				types[typeCount] = operandStack.get(idx).indexType;
				units[typeCount] = operandStack.get(idx).indexUnit;
				uAbbrev[typeCount] = operandStack.get(idx).unit;
				unitFactor[typeCount++] = operandStack.get(idx).conversionFactor;
			} else {
				for (i=0; i < typeCount; i++) {
					if (types[i] == operandStack.get(idx).indexType) {
						if (units[i] < operandStack.get(idx).indexUnit) {
							units[i] = operandStack.get(idx).indexUnit;
							uAbbrev[i] = operandStack.get(idx).unit;
							unitFactor[i] = operandStack.get(idx).conversionFactor;
						}
						break;
					}
				}
				if (i == typeCount) {
					types[typeCount] = operandStack.get(idx).indexType;
					units[typeCount] = operandStack.get(idx).indexUnit;
					uAbbrev[typeCount] = operandStack.get(idx).unit;
					unitFactor[typeCount++] = operandStack.get(idx).conversionFactor;
				}
			}
		}
		// Convert units to smallest
if (verbose)
System.out.println(" Convert");
		for (idx=0; idx < operandStack.size(); idx++) {
			for (i=0; i < typeCount; i++) {
				if (operandStack.get(idx).indexType == types[i]) {
if (verbose)
System.out.println("  Operand " + idx + ": " + operandStack.get(idx).unit + " ==> " + uAbbrev[i]);
					if (operandStack.get(idx).indexUnit < units[i]) {
						try {
							// Handle units raised to a power
							if (operandStack.get(idx).unitPower > 1) {
								c1 = Math.pow(unitFactor[i], (double) operandStack.get(idx).unitPower);
								c2 = Math.pow(operandStack.get(idx).conversionFactor, (double) operandStack.get(idx).unitPower);
							} else {
								c1 = unitFactor[i];
								c2 = operandStack.get(idx).conversionFactor;
							}
							d = (operandStack.get(idx).value * c1) / c2;
							operandStack.get(idx).value = d;
							operandStack.get(idx).indexUnit = units[i];
							operandStack.get(idx).unit = uAbbrev[i];
							operandStack.get(idx).conversionFactor = unitFactor[i];
if (verbose)
System.out.println("   New value: " + d + uAbbrev[i] + " (" + unitFactor[i] + ")");
						} catch (ArithmeticException err ) {
							calcError = "?? Math Err";
							return false;
						}
					}
					break;
				}
			}
		}

		return true;
	} /* end reduceUnits */

/**
 * Get the true size of an equation stack, accounting for operands that are group Units.
 * 
 * @param aStack	The list of operands to be tested for its size.
 */
	private int trueSize(ArrayList<CvtrOperand> aStack) {
		int idx, size = 0;

		for (idx=0; idx < aStack.size(); idx++) {
			if (!aStack.get(idx).groupUnit)
				size++;
		}

		return size;
	}

/**
 * Collect groups of the operands and attempt to reduce them.  The method is called
 * recursively to the highest parse level to attempt to reduce every group to its
 * lowest form.
 * 
 * @param level	The current nesting level.
 * @param nesting	The number of nesting levels to be reduced, which is the number of
 * 				times the method is called recursively.
 * @param nestStack	The list of operands to be reduced.
 * 
 * @return ArrayList<CvtrOperand>	The resulting stack of the recursive call, which may be null.
 * 									If an error is found, then calcError is set.
 */
	private ArrayList<CvtrOperand> getEquationNesting(int level, int nesting, ArrayList<CvtrOperand> nestStack) {
		int i, j;
		ArrayList<CvtrOperand> newStack, tempStack;
		CvtrGroup group;
if (verbose) {
System.out.println("Get Nesting (" + level + " < " + nesting + "): Stack size = " + nestStack.size());
for (i=0; i < nestStack.size(); i++) {
	System.out.print("  Nesting Operand " + i + ": " + nestStack.get(i).value + nestStack.get(i).unit + "^" +
			nestStack.get(i).unitPower + ", " + nestStack.get(i).operation + " (" + nestStack.get(i).nestLevel + ", ");
	for (j=nestStack.get(i).nestLevel; j >= 0; j--) {
		if (nestStack.get(i).nestGroup == null)
			System.out.print("-1");
		else
			System.out.print(">" + nestStack.get(i).nestGroup[j]);
	}
	System.out.println(")");
}
}

		if (nestStack == null) {
			return null;
		} else if (level > nesting) {
			return reduceEquation(nestStack);
		}
		newStack = new ArrayList<CvtrOperand>();
		group = new CvtrGroup(nestStack, level);
		if (group.groupList == null || group.groupList.size() == 0)
			return reduceEquation(nestStack);
		// Recursively reduce nested groups at each level
		for (i=0; i < group.groupList.size(); i++) {
			if (trueSize(group.groupList.get(i)) == 1) {
				tempStack = group.groupList.get(i);
			} else {
				if ((tempStack = getEquationNesting(level + 1, nesting, group.groupList.get(i))) == null) {
					return null;
				}
				if (trueSize(tempStack) == 1) {
					// Modify group count for current and new level/group
					if (tempStack.get(0).nestLevel > 0) {
						modifyGroupCount(tempStack.get(0).nestLevel, tempStack.get(0).nestGroup[tempStack.get(0).nestLevel], false);
						for (j=0; j < tempStack.size(); j++)
							tempStack.get(j).nestLevel--;
						modifyGroupCount(tempStack.get(0).nestLevel, tempStack.get(0).nestGroup[tempStack.get(0).nestLevel], true);
					}
				}
			}
			// Put operation result on the new stack
			while (tempStack.size() > 0) {
				newStack.add(tempStack.get(0));
				tempStack.remove(0);
			}
		}
		if (newStack.size() == 0) {
			calcProgress += displayStack(nestStack) + "\n";
if (verbose)
System.out.println(" !!! GN empty new stack");
			return nestStack;
		} else {
			if (trueSize(newStack) == 2) {
				if (newStack.get(0).function) {
					try {
						newStack.get(1).value = calculateFunction(newStack.get(0).sValue, newStack.get(1));
						newStack.remove(0);
						calcProgress += displayStack(newStack) + "\n";
						return newStack;
					} catch (IllegalArgumentException err) {
						calcError = "?? " + err.getMessage();
						return null;
					}
				// Reduce group of 2 to same nest level
				} else {
					i = maxNesting;
					for (j=0; j < newStack.size(); j++) {
						if (newStack.get(j).nestLevel < i)
							i = nestLevel;
					}
					for (j=0; j < newStack.size(); j++) {
						newStack.get(j).nestLevel = i;
					}
				}
			}
			calcProgress += displayStack(newStack) + "\n";
if (verbose)
System.out.println("GN: " + displayStack(newStack));
			if (trueSize(newStack) == 1)
				return newStack;
			return reduceEquation(newStack);
		}

	} /* end getEquationNesting */

/**
 * Order groups of units by their category to make comparisons between
 * unit groups straightforward.
 * 
 * @param unitStack	The list of operands to be tested for unit groups.
 */
	private void orderUnits(ArrayList<CvtrOperand> unitStack) {
		int i, j, idx;
		String op;
		double d;
		ArrayList<CvtrOperand> tStack;
if (verbose)
System.out.println("Order units");

		// Order groups of units according to unit category
		idx = 0;
		while (idx < unitStack.size()) {
			// Put units belonging to a group on a temp stack
			if ((idx + 1) < unitStack.size() && unitStack.get(idx + 1).groupUnit) {
				d = unitStack.get(idx).value;
				unitStack.get(idx).value = 1;
				op = unitStack.get(idx).operation;
				unitStack.get(idx).operation = unitStack.get(idx + 1).operation;
				unitStack.get(idx).groupUnit = true;
				tStack = new ArrayList<CvtrOperand>();
				tStack.add(unitStack.get(idx));
				for (i=idx + 1; i < unitStack.size(); i++) {
					if (unitStack.get(i).groupUnit) {
						if (tStack.size() == 0) {
							tStack.add(unitStack.get(i));
						} else {
							for (j=0; j < tStack.size(); j++) {
								// Order by category type in descending order
								if (unitStack.get(i).indexType > tStack.get(j).indexType) {
									tStack.add(j, unitStack.get(i));
									break;
								}
							}
							if (j == tStack.size()) {
								tStack.add(unitStack.get(i));
							}
						}
					} else
						break;
				}
				// Replace current group with reordered group
				for (j=0; j < tStack.size(); j++)
					unitStack.remove(idx);
				// Reverse order of temporary stack
				while (tStack.size() > 0) {
					unitStack.add(idx, tStack.get(0));
					tStack.remove(0);
				}
				unitStack.get(idx).value = d;
				unitStack.get(idx).operation = op;
				unitStack.get(idx).groupUnit = false;
				// Skip past group
				idx = i;
			} else
				idx++;
		}

		return;
	} /* end orderUnits */

/**
 * Reduce groups of the operands as much as possible.  The method is called recursively to
 * the highest parse level to attempt to reduce every group to its lowest form.
 * 
 * @param reduceStack	The list of operands to be reduced.
 * 
 * @return ArrayList<CvtrOperand>	The resulting stack if calculation was successful, otherwise null.
 */
	private ArrayList<CvtrOperand> reduceEquation(ArrayList<CvtrOperand> reduceStack) {
		int i, j, idx, idxNx;
		long l1, l2;
		double d;
		boolean multiUnit;
		CvtrOperand oper;
if (verbose)
System.out.println("Reduce Equation: Stack size = " + reduceStack.size());

		// Make sure units are in a consistent order for consolidation
		orderUnits(operandStack);

		// Examine each operand
int debug1 = 10;
		idx = 0;
		while (idx < reduceStack.size()) {
if (debug1-- < 0)
	break;
			multiUnit = false;
			idxNx = idx + 1;

			// Ignore functions
			if (reduceStack.get(idx).function) {
if (verbose)
System.out.println("  Function operand");
				idx++;
				continue;
			// Handle String operands
			} else if (reduceStack.get(idx).sValue != null) {
if (verbose)
System.out.println("  String operand");
				// String operands cannot be reduced
				reduceStack.get(idx).nestLevel = 0;
				idx++;
				continue;
			}

int debug2 = 50;
			while ((idx == 0 || !reduceStack.get(idx).function) &&
					idxNx < reduceStack.size()) {
if (debug2-- < 0) {
	calcError = "?? Loop";
	return null;
}
if (verbose) {
if (reduceStack.get(idxNx).nestGroup == null)
	i = -1;
else
	i = reduceStack.get(idxNx).nestGroup[reduceStack.get(idxNx).nestLevel];
System.out.println("  Operation(" + idx + "): " + reduceStack.get(idx).value + reduceStack.get(idx).unit + "^" + reduceStack.get(idx).unitPower + " (" +
	reduceStack.get(idx).nestLevel + ", " + reduceStack.get(idx).nestGroup[reduceStack.get(idx).nestLevel] + ") " +
	reduceStack.get(idxNx).operation + " " + reduceStack.get(idxNx).value + reduceStack.get(idxNx).unit + "^" + reduceStack.get(idxNx).unitPower + " (" +
	reduceStack.get(idxNx).nestLevel + ", " + i + ") GroupUnit = " + reduceStack.get(idxNx).groupUnit);
}
				// New level of nesting, attempt to reduce it
				if (reduceStack.get(idx).nestLevel < reduceStack.get(idxNx).nestLevel) {
					if (!multiUnit)
						idx = idxNx;
					break;
				}
				// Reduce new nesting level or group
				if (reduceStack.get(idx).nestLevel > reduceStack.get(idxNx).nestLevel ||
						(reduceStack.get(idx).nestGroup[reduceStack.get(idx).nestLevel] != reduceStack.get(idxNx).nestGroup[reduceStack.get(idxNx).nestLevel])) {
					if (!multiUnit)
						idx = idxNx;
					break;
				}
				// Cannot reduce 2 different units or same unit but different power
				if (!reduceStack.get(idxNx).operation.equals("*") && (reduceStack.get(idx).indexType >= 0 && reduceStack.get(idxNx).indexType >= 0) &&
						((reduceStack.get(idx).indexType != reduceStack.get(idxNx).indexType) ||
						(reduceStack.get(idx).unitPower != reduceStack.get(idxNx).unitPower))) {
if (verbose)
System.out.println("   Skip operand");
					multiUnit = true;
					idxNx++;
					continue;
				} else if (reduceStack.get(idx).groupUnit) {
					idx++;
					break;
				}
				if (reduceStack.get(idxNx).operation.equals("/")) {
					// Special case constant denominator
					if (idxNx == (reduceStack.size() - 1) && reduceStack.get(idxNx).indexType < 0) {
						reduceStack.get(idx).value /= reduceStack.get(idxNx).value;
					// Division is handled during group reduction
					} else {
						idx = idxNx;
						break;
					}
				// Perform operation on operands
				} else if (reduceStack.get(idxNx).operation.equals("+")) {
					// Unit is placeholder, do not use in calculations
					if (reduceStack.get(idxNx).groupUnit) {
						idxNx++;
						continue;
					} else if (sameGroupUnits(reduceStack, idx, reduceStack, idxNx)) {
						reduceStack.get(idx).value += reduceStack.get(idxNx).value;
						i = idxNx + 1;
						while (i < reduceStack.size()) {
							if (reduceStack.get(i).groupUnit)
								reduceStack.remove(i);
							else
								break;
						}
					} else if (!multiUnit) {
						idx++;
						break;
					} else {
						break;
					}
				} else if (reduceStack.get(idxNx).operation.equals("-")) {
					// Unit is placeholder, do not use in calculations
					if (reduceStack.get(idxNx).groupUnit) {
						idxNx++;
						continue;
					} else if (sameGroupUnits(reduceStack, idx, reduceStack, idxNx)) {
						reduceStack.get(idx).value -= reduceStack.get(idxNx).value;
						i = idxNx + 1;
						while (i < reduceStack.size()) {
							if (reduceStack.get(i).groupUnit)
								reduceStack.remove(i);
							else
								break;
						}
					} else if (!multiUnit) {
						idx++;
						break;
					} else {
						break;
					}
				} else if (reduceStack.get(idxNx).operation.equals("*")) {
					// Do not multiply like units that have been consolidated
					if (reduceStack.get(idxNx).groupUnit) {
						idxNx++;
						continue;
					}
					//Set the value
					reduceStack.get(idx).value *= reduceStack.get(idxNx).value;
					// Current operand has no unit
					if (reduceStack.get(idx).indexType < 0) {
						reduceStack.get(idx).indexType = reduceStack.get(idxNx).indexType;
						reduceStack.get(idx).indexUnit = reduceStack.get(idxNx).indexUnit;
						reduceStack.get(idx).conversionFactor = reduceStack.get(idxNx).conversionFactor;
						reduceStack.get(idx).unit = reduceStack.get(idxNx).unit;
						reduceStack.get(idx).unitPower = reduceStack.get(idxNx).unitPower;
					// Matching units in both operand groups
					} else if (reduceStack.get(idx).indexType == reduceStack.get(idxNx).indexType) {
						reduceStack.get(idx).unitPower += reduceStack.get(idxNx).unitPower;
					// Consolidate the unit
					} else {
						if (reduceStack.get(idxNx).indexType < 0) {
							reduceStack.remove(idxNx);
							continue;
						}
						i = idx + 1;
						// Check for consolidated like units
						while (i < idxNx) {
							if (reduceStack.get(i).indexType == reduceStack.get(idxNx).indexType) {
								reduceStack.get(i).unitPower += reduceStack.get(idxNx).unitPower;
								reduceStack.remove(idxNx);
								break;
							} else
								i++;
						}
						// Unit needs to be consolidated
						if (i == idxNx) {
							reduceStack.get(idxNx).groupUnit = true;
							reduceStack.get(idxNx).value = 1;
						}
						idxNx++;
						continue;
					}
				} else if (reduceStack.get(idxNx).operation.equals("&") || reduceStack.get(idxNx).operation.equals("|") ||
						reduceStack.get(idxNx).operation.equals("#")) {
					// Do not operate on like units that have been consolidated
					if (reduceStack.get(idxNx).groupUnit) {
						idxNx++;
						continue;
					}
					if (reduceStack.get(idxNx).operation.equals("&")) {
						l1 = (long) reduceStack.get(idxNx).value;
						l2 = (long) reduceStack.get(idx).value;
						reduceStack.get(idx).value = l1 & l2;
					} else if (reduceStack.get(idxNx).operation.equals("|")) {
						l1 = (long) reduceStack.get(idxNx).value;
						l2 = (long) reduceStack.get(idx).value;
						reduceStack.get(idx).value = l1 | l2;
					} else if (reduceStack.get(idxNx).operation.equals("#")) {
						l1 = (long) reduceStack.get(idxNx).value;
						l2 = (long) reduceStack.get(idx).value;
						reduceStack.get(idx).value = l1 ^ l2;
					}
					// Unit groups may need to be updated
					i = idxNx + 1;
					while (i < reduceStack.size()) {
						// End of multiplier group
						if (!reduceStack.get(i).groupUnit)
							break;
						// Test against all units in current group
						oper = reduceStack.get(i);
						reduceStack.remove(i);
						j = idx;
						while (j < i) {
							// End of current group
							if (j > idx && !reduceStack.get(j).groupUnit) {
								break;
							}
							// Current operand has no unit
							if (reduceStack.get(j).indexType < 0) {
								reduceStack.get(j).indexType = oper.indexType;
								reduceStack.get(j).indexUnit = oper.indexUnit;
								reduceStack.get(j).conversionFactor = oper.conversionFactor;
								reduceStack.get(j).unit = oper.unit;
								reduceStack.get(j).unitPower = oper.unitPower;
								oper = null;
								break;
							// Matching units in both operand groups
							} else if (oper.indexType == reduceStack.get(j).indexType) {
								oper = null;
								break;
							}
							j++;
						}
					}
				// Modulo arithmetic is normally performed on integers, but Convertator requires
				// the modulus to be an integer, and allows the operand to be a floating point number 
				} else if (reduceStack.get(idxNx).operation.equals("%")) {
					if (reduceStack.get(idxNx).indexType < 0)
						i = 0;
					else
						i = 1;
					j=idxNx + 1;
					while (j < reduceStack.size() && reduceStack.get(j).groupUnit)
						i++;
					if (i == 0 || sameGroupUnits(reduceStack, idx, reduceStack, idxNx)) {
						l1 = (long) reduceStack.get(idxNx).value;
						d = reduceStack.get(idxNx).value - l1;
						if (d != 0) {
							calcError = "?? N%" + reduceStack.get(idxNx).value;
							return null;
						}
						l1 = (long) reduceStack.get(idx).value;
						d = reduceStack.get(idx).value - l1;
						l2 = (long) reduceStack.get(idxNx).value;
						reduceStack.get(idx).value = (l1 % l2) + d;
						i = idxNx + 1;
						while (i < reduceStack.size()) {
							if (reduceStack.get(i).groupUnit)
								reduceStack.remove(i);
							else
								break;
						}
					} else {
						calcError = "?? Units%Units";
						return null;
					}
				} else if (reduceStack.get(idxNx).operation.equals("^")) {
					if (reduceStack.get(idxNx).indexType >= 0) {
						calcError = "?? ^" + reduceStack.get(idxNx).unit;
						return null;
					}
					d = Math.pow(reduceStack.get(idx).value, reduceStack.get(idxNx).value);
					reduceStack.get(idx).value = d;
					if (reduceStack.get(idx).indexType >= 0)
						reduceStack.get(idx).unitPower = (int) reduceStack.get(idxNx).value;
				}
				// Maintain operand unit with value (except for division which is handled separately
				if (reduceStack.get(idx).indexType < 0 && !reduceStack.get(idxNx).operation.equals("/")) {
					reduceStack.get(idx).indexType = reduceStack.get(idxNx).indexType;
					reduceStack.get(idx).indexUnit = reduceStack.get(idxNx).indexUnit;
					reduceStack.get(idx).conversionFactor = reduceStack.get(idxNx).conversionFactor;
					reduceStack.get(idx).unit = reduceStack.get(idxNx).unit;
					reduceStack.get(idx).unitPower = reduceStack.get(idxNx).unitPower;
				}
				// Decrement group count for level losing operand
				modifyGroupCount(reduceStack.get(idxNx).nestLevel, reduceStack.get(idxNx).nestGroup[reduceStack.get(idxNx).nestLevel], false);
				reduceStack.remove(idxNx);
			}
if (verbose) {
if (idx < reduceStack.size()) {
	if (reduceStack.get(idx).function) 
		i = -1;
	else
		i = reduceStack.get(idx).nestGroup[reduceStack.get(idx).nestLevel];
System.out.println("  New operand(" + idx + "): " + reduceStack.get(idx).value + reduceStack.get(idx).unit + "^" + reduceStack.get(idx).unitPower + " (" +
	reduceStack.get(idx).nestLevel + ", " + i + ") GroupUnit = " + reduceStack.get(idx).groupUnit);
}
}
			if (reduceStack.get(idx).function || multiUnit) {
				idx++;
			} else if (idxNx == reduceStack.size()) {
				idx = idxNx;
			}
		}

if (verbose)
System.out.println(" <== Return reduce equation: Stack size = " + reduceStack.size());
		return reduceStack;
	} /* end reduceEquation */

/**
 * Create the final equation stack by reducing group operations to their minimum terms.
 * 
 * @return boolean	True if operations were successful, otherwise false.
 */
	private boolean finalEquation() {
		int idx;
if (verbose)
System.out.println("Get final equation: " + " Stack size: " + operandStack.size() + ", MaxNesting: " + maxNesting);

		if (operandStack.size() == 1)
			return true;

		// Clear the stack counters
		nestLevel = 0;
		for (idx=0; idx < maxNestLevel; idx++)
			nestGroup[idx] = 0;

		// Reduce groups
		if ((operandStack = reduceGroups(operandStack, 0)) == null)
			return false;

		// Reduce final equation
		if (!reduceFinal(operandStack))
			return false;

		return true;
	} /* end finalEquation */

/**
 * Reduce groups of operands.  This method is called recursively until all groups in the stack
 * have been reduced to their simplest form.  The types of groups are:
 * <ul>
 *   <li>Operands with different unit categories, such as distance (miles) and time (hour)</li>
 *   <li>Operands with the same unit categories but to different powers, such as feet and feet ^ 2</li>
 * </ul>
 * 
 * @param operandStack	The stack to be reduced.
 * @param nLevel	The nesting level to use for dividing operands into groups.  This is used for
 * 					recursion and should always be zero when called initially.
 * 
 * @return ArrayList<CvtrOperand>	The resulting stack if calculation was successful, otherwise null.
 */
	private ArrayList<CvtrOperand> reduceGroups(ArrayList<CvtrOperand> groupStack, int nLevel) {
		int i, idx;
		int gLevel, sLevel;
		boolean listRemove;
		ArrayList <ArrayList<CvtrOperand>> groupList = new ArrayList <ArrayList<CvtrOperand>>();
		ArrayList<CvtrOperand> newStack = new ArrayList<CvtrOperand>(), tempStack = new ArrayList<CvtrOperand>();

		i = 0;
		for (idx=0; idx < groupStack.size(); idx++) {
			if (groupStack.get(idx).nestLevel >= nLevel) {
				i = 1;
				break;
			}
		}
		// Group cannot be reduced
		if (i == 0)
			return groupStack;

if (verbose) {
System.out.println("Reduce groups: " + " (" + groupStack.size() + "): " + nLevel);
for (i=0; i < groupStack.size(); i++) {
	System.out.print("  Reduce groups Operand " + i + ": " + groupStack.get(i).value + groupStack.get(i).unit + "^" +
			groupStack.get(i).unitPower + ", " + groupStack.get(i).operation + " (" + groupStack.get(i).nestLevel + ", ");
	int j;
	for (j=groupStack.get(i).nestLevel; j >= 0; j--) {
		if (groupStack.get(i).nestGroup == null)
			System.out.print("-1");
		else
			System.out.print(">" + groupStack.get(i).nestGroup[j]);
	}
	System.out.println(")");
}
}

		if (groupStack.size() == 0) {
			calcError = "?? Null";
			return null;
		} else if (groupStack.size() == 1) {
			return groupStack;
		}

		// Get groups to be reduced in groupList arrays
		groupList.add(new ArrayList<CvtrOperand>());
		idx = 0;
		gLevel = sLevel = -1;
		while (groupStack.size() > 0) {
			// Get new subgroup if there is a difference in nesting at current nLevel
			if (gLevel < 0 && groupStack.get(0).nestLevel >= nLevel && groupStack.get(0).nestGroup != null)
				gLevel = groupStack.get(0).nestGroup[nLevel];
			if (groupStack.get(0).nestGroup == null) {
				sLevel = groupStack.get(0).nestLevel;
				gLevel = maxNesting + 1;
				if (groupList.get(idx).size() > 0) {
if (verbose)
System.out.println("  Group(" + idx + "): " + displayStack(groupList.get(idx)));
					groupList.add(new ArrayList<CvtrOperand>());
					idx++;
if (verbose)
System.out.println("  Add group list(" + idx + ")null: " + groupList.size());
				}
			// i.nestLevel < nLevel && (i.nestLevel != i+1.nestLevel || i.nestGroup != i+1.nestGroup)
			} else if (sLevel >= 0) {
				if (groupStack.get(0).nestLevel != sLevel || groupStack.get(0).nestGroup[sLevel] != gLevel) {
if (verbose)
System.out.println("  Group(" + idx + "): " + displayStack(groupList.get(idx)));
					groupList.add(new ArrayList<CvtrOperand>());
					idx++;
if (verbose)
System.out.println("  Add group list(" + idx + ")SL: " + groupList.size());
					if (groupStack.get(0).nestLevel >= nLevel) {
						sLevel = -1;
						gLevel = groupStack.get(0).nestGroup[nLevel];
					} else {
						sLevel = groupStack.get(0).nestLevel;
						gLevel = groupStack.get(0).nestGroup[sLevel];
						
					}
				}
			// i.nestLevel != i+1.nestLevel
			} else if (groupStack.get(0).nestLevel < nLevel) {
				sLevel = groupStack.get(0).nestLevel;
				gLevel = groupStack.get(0).nestGroup[sLevel];
				if (groupList.get(idx).size() > 0) {
if (verbose)
System.out.println("  Group(" + idx + "): " + displayStack(groupList.get(idx)));
					groupList.add(new ArrayList<CvtrOperand>());
					idx++;
if (verbose)
System.out.println("  Add group list(" + idx + ")NL: " + groupList.size());
				}
			// i.nestGroup != i+1.nestGroup
			} else if (gLevel != groupStack.get(0).nestGroup[nLevel]) {
				gLevel = groupStack.get(0).nestGroup[nLevel];
				if (groupList.get(idx).size() > 0) {
if (verbose)
System.out.println("  Group(" + idx + "): " + displayStack(groupList.get(idx)));
					groupList.add(new ArrayList<CvtrOperand>());
					idx++;
if (verbose)
System.out.println("  Add group list(" + idx + ")GL: " + groupList.size());
				}
			}
			// Add the operand to the subgroup
			groupList.get(idx).add(groupStack.get(0));
			groupStack.remove(0);
if (verbose)
System.out.println("  Group size: " + groupList.get(idx).size());
		}
if (verbose)
System.out.println("  Group(" + idx + "): " + displayStack(groupList.get(idx)));
		// Recursively call reduceGroups for each subgroup
		for (idx=0; idx < groupList.size(); idx++) {
if (verbose)
System.out.println("  Reduce next group: " + idx + " of " + groupList.size() + " = " + groupList.get(idx).size());
			if (groupList.get(idx).size() > 1) {
				if ((tempStack = reduceGroups(groupList.get(idx), nLevel + 1)) == null)
					return null;
				// Replace subgroup with reduced subgroup
				groupList.remove(idx);
				groupList.add(idx, tempStack);
if (verbose)
System.out.println(" <== Return from recursion(" + nLevel + "): (" + idx + " of " + groupList.size() + ") " + groupList.get(idx).size());
			} else {
if (verbose)
System.out.println(" <== Skip recursion(" + nLevel + "): (" + idx + ") " + groupList.get(idx).size());
			}
		}
		if (groupList.size() == 1) {
			// Single group may need equation reduction
			if (groupList.get(0).size() > 1) {
				if ((newStack = reduceEquation(groupList.get(0))) == null)
					return null;
			} else
				newStack = groupList.get(0);
			return newStack;
		}

		// Perform operations on reduced groups
		newStack = groupList.get(0);
		groupList.remove(0);
		while (groupList.size() > 0) {
			listRemove = true;
if (verbose)
System.out.println("  Group operations: " + newStack.size() + ", " + groupList.get(0).size() +
	", newop " + newStack.get(0).operation + ", glop " + groupList.get(0).get(0).operation);
			calcProgress += displayStack(newStack) + " " + groupList.get(0).get(0).operation + " " + displayStack(groupList.get(0)) + "\n  ===> ";
			if (groupList.get(0).get(0).operation.equals("^")) {
				if ((tempStack = powerGroup(newStack, groupList.get(0))) == null)
					return null;
			} else if (newStack.get(0).function) {
				if ((tempStack = calculateFunctionGroup(newStack, groupList.get(0))) == null)
					return null;
			} else if (groupList.get(0).get(0).operation.equals("+") || groupList.get(0).get(0).operation.equals("-")) {
				if ((tempStack = addsubGroups(newStack, groupList.get(0))) == null)
					return null;
			} else if (groupList.get(0).get(0).operation.equals("*")) {
				if ((tempStack = multiplyGroups(newStack, groupList.get(0))) == null)
					return null;
			} else if (groupList.get(0).get(0).operation.equals("/")) {
				if ((tempStack = divideGroups(newStack, groupList.get(0))) == null)
					return null;
			} else if (groupList.get(0).get(0).operation.equals("%")) {
				if ((tempStack = moduloGroup(newStack, groupList.get(0))) == null)
					return null;
			} else if (groupList.get(0).get(0).operation.equals("&") || groupList.get(0).get(0).operation.equals("|") ||
					groupList.get(0).get(0).operation.equals("#")) {
				if ((tempStack = logicalGroups(newStack, groupList.get(0))) == null)
					return null;
			} else if (groupList.get(0).get(0).function && groupList.size() > 1) {
				for (idx=0; idx < groupList.get(0).size(); idx++)
					tempStack.add(groupList.get(0).get(idx));
				for (idx=0; idx < groupList.get(1).size(); idx++)
					tempStack.add(groupList.get(1).get(idx));
				i = groupList.get(0).get(0).nestLevel;
				groupList.remove(1);
				groupList.remove(0);
				if ((tempStack = reduceGroups(tempStack, i)) == null)
					return null;
				groupList.add(0, tempStack);
				listRemove = false;
				tempStack = newStack;
			} else {
if (verbose)
System.out.println(" == Err: " + groupList.get(0).get(0).operation + ", Func: " + newStack.get(0).function);
				calcError = "?? ()" + groupList.get(0).get(0).operation + "()";
				return null;
			}
			// Put operation result on the new stack
			newStack = new ArrayList<CvtrOperand>();
			while (tempStack.size() > 0) {
				newStack.add(tempStack.get(0));
				tempStack.remove(0);
			}
			calcProgress += displayStack(newStack) + "\n";
if (verbose)
System.out.println("  New group(" + newStack.size() + "): " + displayStack(newStack));
			// Get a new group at the head of the group list
			if (listRemove)
				groupList.remove(0);
		}

		// Clear group counter 2D array
		for (idx=0; idx < groupCount.size(); idx++) {
			for (i=0; i < groupCount.get(idx).size(); i++)
				groupCount.get(idx).set(i, "0");
		}
		// Set new group counter 2D array
		nLevel = maxNesting;
		sLevel = 0;
		for (idx=0; idx < newStack.size(); idx++) {
			if (nLevel > newStack.get(idx).nestLevel)
				nLevel = newStack.get(idx).nestLevel;
			if (sLevel < newStack.get(idx).nestLevel)
				sLevel = newStack.get(idx).nestLevel;
			modifyGroupCount(newStack.get(idx).nestLevel, newStack.get(idx).nestGroup[newStack.get(idx).nestLevel], true);
		}
		// Reduce equation further
		if ((i = (sLevel - nLevel)) < 0)
			i = 0;
		calcError = "";
		if ((newStack = getEquationNesting(0, i, newStack)) == null || calcError.length() > 0) {
			return null;
		}

if (verbose) {
for (i=0; i < newStack.size(); i++) {
	System.out.print("   End Reduce groups Operand " + i + ": " + newStack.get(i).value + newStack.get(i).unit + "^" +
			newStack.get(i).unitPower + ", " + newStack.get(i).operation + " (" + newStack.get(i).nestLevel + ", ");
	int j;
	for (j=newStack.get(i).nestLevel; j >= 0; j--)
		System.out.print(">" + newStack.get(i).nestGroup[j]);
	System.out.println(")");
}
}
//		calcProgress += displayStack(newStack) + "\n";
		return newStack;
	} /* end reduceGroups */

/**
 * Put two operands in the same group by setting the nest level and group level
 * of the second to the first.
 * 
 * @param stackA	The first operand.
 * @param stackB	The second operand.
 */
	private void syncOperands(CvtrOperand operA, CvtrOperand operB) {
		int idx;
		int[] nGroup;
if (verbose)
System.out.println("Sync: A " + operA.nestLevel + "(" + operA.nestGroup.length + "), B " +  operB.nestLevel);

		if (operB.nestLevel < operA.nestLevel) {
			nGroup = new int[operA.nestLevel + 1];
			for (idx=0; idx < operA.nestLevel; idx++) {
				nGroup[idx] = operA.nestGroup[idx];
			}
			operB.nestGroup = nGroup;
		} else {
			operB.nestGroup[operA.nestLevel] = operA.nestGroup[operA.nestLevel];
		}
		operB.nestLevel = operA.nestLevel;

	}

/**
 * Verify an operation is consistent.  Addition and subtraction are consistent with each other,
 * as are the bitwise operators AND, OR, and XOR.  All other operations are unique.
 * 
 * @param operation	The expected operation.
 * @param testOperation	The operation to be verified.
 * 
 * @return boolean	True if the operands are consistent, otherwise false.
 */
	private boolean verifyOperation(String operation, String testOperation) {
		String addList = "+-", logicList = "&|#";
if (verbose)
System.out.println("Verify operation: " + operation + " ?= " + testOperation);
// TODO: Handle groups
		if (operation.length() == 0) {
			return true;
		} else if (testOperation.equals(operation)) {
			return true;
		} else if (addList.indexOf(operation) >= 0 && addList.indexOf(testOperation) >= 0) {
			return true;
		} else if (logicList.indexOf(operation) >= 0 && logicList.indexOf(testOperation) >= 0) {
			return true;
		} else {
			calcError ="?? !Op(" + operation + testOperation + ")";
			return false;
		}

	}

/**
 * Determine the overall type of a group, which is the primary operation.
 * The operations within the group (starting with the second operand) must be
 * consistent (see verifyOperation).  Because Convertator does not support
 * operator precedence, there should never be different types of operations in
 * the same group.  However, division may be fully reduced and still include
 * addition/subtraction in either the numerator or denominator or both.
 * 
 * If the group is to be subtracted, it is changed to addition and each operand
 * is multiplied by -1.
 * 
 * If the group operation is multiplication or a bitwise logic operation, the group
 * is reduced to a single value with the remaining operands groupUnit flag set to
 * indicate that the operand is not to be used in further calculations.  This means
 * that these group types are defined to be none.
 * 
 * @param groupStack	The group stack to be evaluated.
 * 
 * @return int	The group type ID.  If there is an error, -1 is returned.
 */
	private int groupType(ArrayList<CvtrOperand> groupStack) {
		int idx, idxOp, opType, divCount = 0;
		String operation = "";
		ArrayList<CvtrOperand> tempStack;
		if (groupStack.size() == 0) {
			calcError = "?? Null Group";
			return -1;
		} else if (groupStack.size() == 1) {
			return none;
		}

		// Find operand with active operation
		for (idxOp=1; idxOp < groupStack.size(); idxOp++) {
			if (!groupStack.get(idxOp).groupUnit)
				break;
		}
		if (idxOp == groupStack.size()) {
			return none;
		}

		// Test for fully reduced division
		if (!groupStack.get(idxOp).operation.equals("/")) {
			for (idx=idxOp; idx < groupStack.size(); idx++) {
				if (groupStack.get(idx).operation.equals("/"))
					divCount++;
			}
			// Only one level of division allowed in a group
			if (divCount > 1) {
				calcError = "?? (//)";
				return -1;
			} else if (divCount == 1) {
				// Test numerator
				tempStack = new ArrayList<CvtrOperand>();
				for (idx=idxOp; idx < groupStack.size() && !groupStack.get(idx).operation.equals("/"); idx++) {
					tempStack.add(groupStack.get(idx));
				}
				if ((opType = groupType(tempStack)) < 0) {
					return -1;
				}
				// Test denominator
				tempStack = new ArrayList<CvtrOperand>();
				for (; idx < groupStack.size(); idx++) {
					tempStack.add(groupStack.get(idx));
				}
				if ((opType = groupType(tempStack)) < 0) {
					return -1;
				} else {
					return div;
				}
			}
		}

		// Test group for consistency
		if (groupStack.get(idxOp).operation.equals("/")) {
			for (idx=idxOp; idx < groupStack.size(); idx++) {
				if (!groupStack.get(idx).groupUnit) {
					if (operation.length() == 0) {
						operation = groupStack.get(idx).operation;
					// Only one level of division allowed in a group
					} else if (groupStack.get(idx).operation.equals("/")) {
						calcError = "?? (//)";
						return -1;
					// Test for fully reduced division
					} else {
						tempStack = new ArrayList<CvtrOperand>();
						for (; idx < groupStack.size(); idx++) {
							tempStack.add(groupStack.get(idx));
						}
						if ((opType = groupType(tempStack)) < 0) {
							return -1;
						// Only one level of division allowed in a group
						} else if (opType == div) {
							calcError = "?? (//)";
							return -1;
						} else {
							return div;
						}
					}
				}
			}
			return div;
		} else if (groupStack.get(idxOp).operation.equals("+") || groupStack.get(idxOp).operation.equals("-")) {
			for (idx=idxOp; idx < groupStack.size(); idx++) {
				if (operation.length() == 0 && !groupStack.get(idx).groupUnit) {
					operation = groupStack.get(idx).operation;
				} else if (!groupStack.get(idx).groupUnit && !verifyOperation(operation, groupStack.get(idx).operation)) {
					return -1;
				}
			}
			// If the group is subtracted, add the negated values
			if (groupStack.get(0).operation.equals("-")) {
				if (!subtractUnits(groupStack)) {
					return -1;
				}
			}
			return addsub;
		} else if (groupStack.get(idxOp).operation.equals("*")) {
			for (idx=idxOp; idx < groupStack.size(); idx++) {
				if (operation.length() == 0 && !groupStack.get(idx).groupUnit) {
					operation = groupStack.get(idx).operation;
				} else if (!groupStack.get(idx).groupUnit && !verifyOperation(operation, groupStack.get(idx).operation)) {
					return -1;
				}
			}
			multiplyUnits(groupStack);
			return none;
		} else if (groupStack.get(idxOp).operation.equals("&") || groupStack.get(idxOp).operation.equals("|") || groupStack.get(idxOp).operation.equals("#")) {
			for (idx=idxOp; idx < groupStack.size(); idx++) {
				if (operation.length() == 0 && !groupStack.get(idx).groupUnit) {
					operation = groupStack.get(idx).operation;
				} else if (!groupStack.get(idx).groupUnit && !verifyOperation(operation, groupStack.get(idx).operation)) {
					return -1;
				}
			}
			logicUnits(groupStack);
			return none;
		}

		calcError = "?? Op(" + groupStack.get(idxOp).operation + ")";
		return -1;
	} /* end groupType */

/**
 * Convert a group to be subtracted to one for adding.
 * 
 * @param unitStack	The first group in the operation.
 * 
 * @return boolean	True if successful, false if there is an error.
 */
	private boolean subtractUnits(ArrayList<CvtrOperand> unitStack) {
		int idx;
if (verbose)
System.out.println("Subtract units: " + unitStack.size());
		
		// If the second group is subtracted, multiply the values by -1
		if (unitStack.get(0).operation.equals("-")) {
			unitStack.get(0).value *= -1;
			unitStack.get(0).operation = "+";
			for (idx=1; idx < unitStack.size(); idx++) {
				if (unitStack.get(idx).operation.equals("+")) {
					unitStack.get(idx).value *= -1;
				} else if (unitStack.get(idx).operation.equals("-")) {
					unitStack.get(idx).operation = "+";
				} else
					return false;
			}
		} else
			return false;

		return true;
	}

/**
 * Multiply a group of different units.  The first operand is the result of all operands
 * multiplied together, and the remaining operands have the groupUnit flag set to true,
 * with the value set to 1.  If the operation is not multiplication, a new first operand
 * is set and the next group is multiplied.
 * 
 * @param unitStack	The group to be multiplied.
 */
	private void multiplyUnits(ArrayList<CvtrOperand> unitStack) {
		int head = 0, idx = 1;
if (verbose)
System.out.println("Multiply units: " + unitStack.size());
		while (idx < unitStack.size()) {
			if (!unitStack.get(idx).operation.equals("*")) {
				head = idx;
				idx++;
				continue;
			}
			// Set the value
			unitStack.get(head).value *= unitStack.get(idx).value;
			// No unit in second value
			if (unitStack.get(idx).indexType < 0) {
				unitStack.remove(idx);
			// Set placeholder
			} else if (unitStack.get(head).indexType >= 0) {
				if (unitStack.get(head).indexType != unitStack.get(idx).indexType) {
					unitStack.get(idx).value = 1;
					unitStack.get(idx).groupUnit = true;
					idx++;
				} else {
					unitStack.get(head).unitPower += unitStack.get(idx).unitPower;
					unitStack.remove(idx);
				}
			// Same unit, set power
			} else {
				unitStack.get(head).indexType = unitStack.get(idx).indexType;
				unitStack.get(head).indexUnit = unitStack.get(idx).indexUnit;
				unitStack.get(head).conversionFactor = unitStack.get(idx).conversionFactor;
				unitStack.get(head).unit = unitStack.get(idx).unit;
				unitStack.get(head).unitPower = unitStack.get(idx).unitPower;
				unitStack.remove(idx);
			}
		}

		return;
	}

/**
 * Perform bitwise logic operations on a group of different units.  The first operand is
 * the result of the resulting operation, and the remaining operands have the groupUnit
 * flag set to true, with the value set to 0.  Note that logical operations are only
 * performed on integers, and floating point values are converted to integers during
 * the evaluation.  If the operation is not a logic operation, a new first operand
 * is set and the next group is calculated.
 * 
 * @param unitStack	The group to be logically evaluated.
 */
	private void logicUnits(ArrayList<CvtrOperand> unitStack) {
		int head = 0, idx = 1;
		long l1, l2;
		String logicList = "&|#";
if (verbose)
System.out.println("Logical units: " + unitStack.size());
		
		while (idx < unitStack.size()) {
			if (logicList.indexOf(unitStack.get(idx).operation) < 0) {
				head = idx;
				idx++;
				continue;
			}
			l1 = (long) unitStack.get(head).value;
			l2 = (long) unitStack.get(idx).value;
			// Check second operand units
			if (unitStack.get(head).indexType >= 0) {
				if (unitStack.get(idx).operation.equals("&")) {
					unitStack.get(head).value = l1 & l2;
				} else if (unitStack.get(idx).operation.equals("|")) {
					unitStack.get(head).value = l1 | l2;
				} else if (unitStack.get(idx).operation.equals("#")) {
					unitStack.get(head).value = l1 ^ l2;
				}
				// Different units, set placeholder
				if (unitStack.get(head).indexType != unitStack.get(idx).indexType) {
					unitStack.get(idx).value = 0;
					unitStack.get(idx).groupUnit = true;
					idx++;
				// Same or no units
				} else {
					unitStack.remove(idx);
				}
			// First operand has no units
			} else {
				if (unitStack.get(idx).operation.equals("&")) {
					unitStack.get(head).value = l1 & l2;
				} else if (unitStack.get(idx).operation.equals("|")) {
					unitStack.get(head).value = l1 | l2;
				} else if (unitStack.get(idx).operation.equals("#")) {
					unitStack.get(head).value = l1 ^ l2;
				}
				unitStack.get(head).indexType = unitStack.get(idx).indexType;
				unitStack.get(head).indexUnit = unitStack.get(idx).indexUnit;
				unitStack.get(head).conversionFactor = unitStack.get(idx).conversionFactor;
				unitStack.get(head).unit = unitStack.get(idx).unit;
				unitStack.get(head).unitPower = unitStack.get(idx).unitPower;
				unitStack.remove(idx);
			}
		}

		return;
	}

/**
 * Test two groups to determine if all units in the first group are in the second group,
 * including the unit powers.  If one group is larger than the other but all units in one
 * are in the other, the group units are considered to be identical.
 * 
 * @param stackA	The first group to be tested.
 * @param startA	The starting index in the first group.
 * @param stackB	The second group to be tested.
 * @param startB	The starting index in the second group.
 * 
 * @return boolean	True if the two groups have identical units, otherwise false.
 */
	private boolean sameGroupUnits(ArrayList<CvtrOperand> stackA, int startA, ArrayList<CvtrOperand> stackB, int startB) {
		int idxA, idxB, endA, endB;
		boolean result = false;

		// Get index of the end of group units in each group
		for (endA=startA+1; endA < stackA.size(); endA++) {
			if (!stackA.get(endA).groupUnit)
				break;
		}
		for (endB=startB+1; endB < stackB.size(); endB++) {
			if (!stackB.get(endB).groupUnit)
				break;
		}
		// Compare against the larger set of units
		if ((endA - startA) >= (endB - startB)) {
			for (idxA=startA; idxA < endA; idxA++) {
				for (idxB=startB; idxB < endB; idxB++) {
					if (stackA.get(idxA).indexType == stackB.get(idxB).indexType &&
							stackA.get(idxA).indexUnit == stackB.get(idxB).indexUnit &&
							stackA.get(idxA).unitPower == stackB.get(idxB).unitPower)
						break;
				}
				// First group unit not found in second group
				if (idxB == endB)
					break;
			}
			// Units are identical
			if (idxA == endA)
				result = true;
		} else {
			for (idxB=startB; idxB < endB; idxB++) {
				for (idxA=startA; idxA < endA; idxA++) {
					if (stackB.get(idxB).indexType == stackA.get(idxA).indexType &&
							stackB.get(idxB).indexUnit == stackA.get(idxA).indexUnit &&
							stackB.get(idxB).unitPower == stackA.get(idxA).unitPower)
						break;
				}
				// Second group unit not found in first group
				if (idxA == endA)
					break;
			}
			// Units are identical
			if (idxB == endB)
				result = true;
		}

if (verbose)
System.out.println(" == Same Group Units = " + result);
		return result;
	}

/**
 * Add or subtract two groups and return the reduced result.
 * 
 * @param stackA	The first group in the operation.
 * @param stackB	The second group in the operation.
 * 
 * @return ArrayList<CvtrOperand>	The resulting group.
 */
	private ArrayList<CvtrOperand> addsubGroups(ArrayList<CvtrOperand> stackA, ArrayList<CvtrOperand> stackB) {
		int i, idx;
		int opA, opB, iDA, iDB;
		boolean unitsCancel;
		CvtrOperand oper;
		ArrayList<CvtrOperand> newStack = new ArrayList<CvtrOperand>(), tStackA, tStackB;
if (verbose)
System.out.println("Add/Subtract groups: " + stackA.size() + ", " + stackB.size());

		// Groups are single operands
		if (stackA.size() == 1 && stackB.size() == 1) {
			syncOperands(stackA.get(0), stackB.get(0));
			newStack = stackA;
			newStack.add(stackB.get(0));
			if ((newStack = reduceEquation(newStack)) == null) {
				return null;
			}
			return newStack;
		}

		// Get group types
		if ((opA = groupType(stackA)) < 0) {
			return null;
		} else if ((opB = groupType(stackB)) < 0) {
			return null;
		}
if (verbose)
System.out.println(" == OpA :: OpB: " + opA + " :: " + opB);

		// (+|-|N) +|- (+|-|N)
		if ((opA == none || opA <= addsub) && (opB == none || opB <= addsub)) {
			// Put all operands in the first group
			for (idx=0; idx < stackB.size(); idx++) {
				syncOperands(stackA.get(0), stackB.get(idx));
			}
			// Build a new equation and reduce it
			newStack = stackA;
			while (stackB.size() > 0) {
				newStack.add(stackB.get(0));
				stackB.remove(0);
			}
			if ((newStack = reduceEquation(newStack)) == null)
				return null;
		// (*|N) +|- (*|N)
		} else if ((opA == none || opA == mult) && (opB == none || opB == mult)) {
			newStack = stackA;
			// Units are the same and the value can be added or subtracted
			if (sameGroupUnits(stackA, 0, stackB, 0)) {
				if (stackB.get(0).operation.equals("+"))
					newStack.get(0).value += stackB.get(0).value;
				else
					newStack.get(0).value -= stackB.get(0).value;
			// Units are different, merge the two groups
			} else {
				while (stackB.size() > 0) {
					newStack.add(stackB.get(0));
					stackB.remove(0);
				}
			}
		// (/|?) +|- (/|?)
		} else if (opA == div || opB == div) {
			// Get starting operand denominator for each group
			for (iDA=1; iDA < stackA.size(); iDA++) {
				if (stackA.get(iDA).operation.equals("/"))
					break;
			}
			for (iDB=1; iDB < stackB.size(); iDB++) {
				if (stackB.get(iDB).operation.equals("/"))
					break;
			}
			// Check for matching denominator units in both groups
			tStackA = new ArrayList <CvtrOperand>();
			for (idx=iDA; idx < stackA.size(); idx++) {
				tStackA.add(stackA.get(idx));
			}
			tStackB = new ArrayList <CvtrOperand>();
			for (idx=iDB; idx < stackB.size(); idx++) {
				tStackB.add(stackB.get(idx));
			}
			// Get denominator group types
			if (tStackA.size() > 0 && (opA = groupType(tStackA)) < 0) {
				return null;
			} else if (tStackB.size() > 0 && (opB = groupType(tStackB)) < 0) {
				return null;
			}
			// Group type is multiplication or logic
			if (opA != addsub && opA != div && opB != addsub && opB != div) {
				// Units are the same
				if (tStackA.size() > 0 && tStackB.size() > 0 && sameGroupUnits(tStackA, 0, tStackB, 0)) {
					unitsCancel = true;
				// Units are different or only 1 denominator
				} else {
					unitsCancel = false;
				}
			// Group type does not allow numerator/denominator units to cancel
			} else {
				unitsCancel = false;
			}
			// Multiply first group numerator by second group denominator
			tStackA = new ArrayList <CvtrOperand>();
			for (idx=0; idx < iDA; idx++) {
				tStackA.add(stackA.get(idx));
			}
			tStackB = new ArrayList <CvtrOperand>();
			for (idx=iDB; idx < stackB.size(); idx++) {
				if (idx == iDB && iDA < stackA.size()) {
					stackB.get(idx).operation = "*";
				}
				oper = stackB.get(idx).dupOperand();
				// Numerator/Denominator units cancel each other in addition/subtraction
				if (unitsCancel)
					oper.clearUnit();
				// Multiply the group
				if (idx == iDB) {
					oper.operation = "*";
				}
				tStackB.add(oper);
			}
			if (tStackB.size() > 0) {
				if ((tStackA = multiplyGroups(tStackA, tStackB)) == null)
					return null;
			}
			newStack = tStackA;
			// Multiply second group numerator by first group denominator
			tStackB = new ArrayList <CvtrOperand>();
			for (idx=0; idx < iDB; idx++) {
				tStackB.add(stackB.get(idx));
			}
			tStackA = new ArrayList <CvtrOperand>();
			for (idx=iDA; idx < stackA.size(); idx++) {
				oper = stackA.get(idx).dupOperand();
				// Numerator/Denominator units cancel each other in addition/subtraction
				if (unitsCancel)
					oper.clearUnit();
				// Multiply the group
				if (idx == iDA) {
					oper.operation = "*";
				}
				tStackA.add(oper);
			}
			if (tStackA.size() > 0) {
				if ((tStackB = multiplyGroups(tStackB, tStackA)) == null)
					return null;
			}
			// Put the new operands in the same group as the first
			while (tStackB.size() > 0) {
				syncOperands(newStack.get(0), tStackB.get(0));
				newStack.add(tStackB.get(0));
				tStackB.remove(0);
			}
			// Multiply denominator
			if (iDA < stackA.size()) {
				tStackA = new ArrayList <CvtrOperand>();
				for (idx=iDA; idx < stackA.size(); idx++) {
					tStackA.add(stackA.get(idx));
				}
				tStackB = new ArrayList <CvtrOperand>();
				for (idx=iDB; idx < stackB.size(); idx++) {
					// Numerator/Denominator units cancel each other in addition/subtraction
					if (unitsCancel)
						stackB.get(idx).clearUnit();
					tStackB.add(stackB.get(idx));
				}
				if (tStackB.size() > 0) {
					if ((tStackA = multiplyGroups(tStackA, tStackB)) == null)
						return null;
				}
			// Denominator is multiplied by 1
			} else {
				tStackA = new ArrayList <CvtrOperand>();
				for (idx=iDB; idx < stackB.size(); idx++) {
					tStackA.add(stackB.get(idx));
				}
			}
			// Make denominator a separate group
			for (idx=0; idx < tStackA.size(); idx++) {
				syncOperands(newStack.get(0), tStackA.get(idx));
				tStackA.get(idx).nestGroup[tStackA.get(idx).nestLevel]++;
				newStack.add(tStackA.get(idx));
			}
		// (+|-) +|- (*)
		} else if ((opA == addsub && opB == mult) || (opA == mult && opB == addsub)) {
			i = 0;
			if (opA == mult) {
				for (idx=0; idx < stackA.size(); idx++) {
					if (stackA.get(idx).groupUnit)
						i++;
				}
			} else {
				for (idx=0; idx < stackB.size(); idx++) {
					if (stackB.get(idx).groupUnit)
						i++;
				}
			}
			// Multiplication group has been reduced to single value
			if (i == 1) {
				newStack = stackA;
				while (stackB.size() > 0) {
					newStack.add(stackB.get(0));
					stackB.remove(0);
				}
			// Both groups have been reduced as much as possible
			} else {
				newStack = stackA;
				while (stackB.size() > 0) {
					newStack.add(stackB.get(0));
					stackB.remove(0);
				}
			}
		}

if (verbose) {
for (i=0; i < newStack.size(); i++) {
	System.out.print("   End addsubGroups Operand " + i + ": " + newStack.get(i).value + newStack.get(i).unit + "^" +
			newStack.get(i).unitPower + ", " + newStack.get(i).operation + " (" + newStack.get(i).nestLevel + ", ");
	int j;
	for (j=newStack.get(i).nestLevel; j >= 0; j--)
		System.out.print(">" + newStack.get(i).nestGroup[j]);
	System.out.println(")");
}
}
		return newStack;
	} /* end addsubGroups */

/**
 * Multiply two groups and return the reduced result.
 * 
 * @param stackA	The first group in the operation.
 * @param stackB	The second group in the operation.
 * 
 * @return ArrayList<CvtrOperand>	The resulting group.
 */
	private ArrayList<CvtrOperand> multiplyGroups(ArrayList<CvtrOperand> stackA, ArrayList<CvtrOperand> stackB) {
		int i, j, idx, idxA, idxB;
		int opA, opB, iDA, iDB;
		CvtrOperand oper;
		ArrayList<CvtrOperand> newStack = new ArrayList<CvtrOperand>(), tStackA, tStackB, oStack;
if (verbose)
System.out.println("Multiply groups: " + stackA.size() + ", " + stackB.size());

		// Groups are single operands
		if (stackA.size() == 1 && stackB.size() == 1) {
			syncOperands(stackA.get(0), stackB.get(0));
			newStack = stackA;
			newStack.get(0).value *= stackB.get(0).value;
			// Handle units
			if (stackB.get(0).indexType >= 0) {
				// First operand has no unit
				if (newStack.get(0).indexType < 0) {
					newStack.get(0).indexType = stackB.get(0).indexType;
					newStack.get(0).indexUnit = stackB.get(0).indexUnit;
					newStack.get(0).conversionFactor = stackB.get(0).conversionFactor;
					newStack.get(0).unit = stackB.get(0).unit;
					newStack.get(0).unitPower = stackB.get(0).unitPower;
				// Units are the same, add unit powers
				} else if (newStack.get(0).indexType == stackB.get(0).indexType) {
					newStack.get(0).unitPower += stackB.get(0).unitPower;
				// Operand becomes unit group
				} else {
					stackB.get(0).value = 1;
					stackB.get(0).groupUnit = true;
					newStack.add(stackB.get(0));
				}
			}
			return newStack;
		}

		// Get group types
		if ((opA = groupType(stackA)) < 0) {
			return null;
		} else if ((opB = groupType(stackB)) < 0) {
			return null;
		}
if (verbose)
System.out.println(" == OpA :: OpB: " + opA + " :: " + opB);

		// (+|-|N) * (+|-|N)
		if (opA <= addsub && opB <= addsub) {
			tStackA = new ArrayList<CvtrOperand>();
			oStack = new ArrayList<CvtrOperand>();
			tStackB = new ArrayList<CvtrOperand>();
			for (idxA=0; idxA < stackA.size(); idxA++) {
				tStackA.add(stackA.get(idxA));
				// Account for multi-unit operands
				while ((idxA + 1) < stackA.size() && stackA.get(idxA + 1).groupUnit) {
					idxA++;
					tStackA.add(stackA.get(idxA));
				}
				// Multiply each operand in first group by all operands in second group
				for (idxB=0; idxB < stackB.size(); idxB++) {
					oper = stackB.get(idxB).dupOperand();
					// Get sign of multiplier value
					if (oper.operation.equals("-"))
						iDB = -1;
					else
						iDB = 1;
					syncOperands(tStackA.get(0), oper);
					tStackB.add(oper);
					while ((idxB + 1) < stackB.size() && stackB.get(idxB + 1).groupUnit) {
						idxB++;
						oper = stackB.get(idxB).dupOperand();
						syncOperands(tStackA.get(0), oper);
						tStackB.add(oper);
					}
					// Get temporary group in case current group needs to be reused
					for (i=0; i < tStackA.size(); i++) {
						oStack.add(tStackA.get(i).dupOperand());
						// Set operation according to first group operands
						if (i == 0) {
							if (tStackA.get(i).operation.equals("-")) {
								oStack.get(i).operation = "-";
							} else {
								oStack.get(i).operation = "+";
							}
						} else {
							oStack.get(i).operation = "*";
						}
					}
					// Set operand operation and value
					oStack.get(0).value *= tStackB.get(0).value * iDB;
					for (i=0; i < oStack.size(); i++) {
						for (j=0; j < tStackB.size(); j++) {
							// First group primary operand has no units, set to second group units
							if (!oStack.get(i).groupUnit && oStack.get(i).indexType < 0) {
								oStack.get(i).indexType = tStackB.get(j).indexType;
								oStack.get(i).indexUnit = tStackB.get(j).indexUnit;
								oStack.get(i).conversionFactor = tStackB.get(j).conversionFactor;
								oStack.get(i).unit = tStackB.get(j).unit;
								oStack.get(i).unitPower = tStackB.get(j).unitPower;
								tStackB.remove(j);
								j--;
								break;
							// Add powers of duplicate units
							} else if (oStack.get(i).unit.equals(tStackB.get(j).unit)) {
								oStack.get(i).unitPower += tStackB.get(j).unitPower;
								tStackB.remove(j);
								j--;
								break;
							}
						}
					}
					// Add operands to result
					while (oStack.size() > 0) {
						newStack.add(oStack.get(0));
						oStack.remove(0);
					}
					while (tStackB.size() > 0) {
						if (tStackB.get(0).indexType >= 0) {
							tStackB.get(0).groupUnit = true;
							tStackB.get(0).value = 1;
							tStackB.get(0).operation = "*";
							newStack.add(tStackB.get(0));
						}
						tStackB.remove(0);
					}
				}
				while (tStackA.size() > 0)
					tStackA.remove(0);
			}
			newStack.get(0).operation = stackA.get(0).operation;
		// (*) * (*)
		} else if ((opA == none || opA == mult) && (opB == none || opB == mult)) {
			newStack = stackA;
			// Put all operands in the same group
			for (idx=0; idx < stackB.size(); idx++) {
				syncOperands(newStack.get(0), stackB.get(0));
				newStack.add(stackB.get(0));
				stackB.remove(0);
			}
			// Make all units active to allow them to be reduced
			for (idx=0; idx < newStack.size(); idx++) {
				newStack.get(idx).groupUnit = false;
			}
			if ((newStack = reduceEquation(newStack)) == null || newStack.size() == 0)
				return null;
		// (/) * (/)
		} else if ((opA == none || opA == div) && (opB == none || opB == div)) {
			// Get starting operand denominator for each group
			for (iDA=1; iDA < stackA.size(); iDA++) {
				if (stackA.get(iDA).operation.equals("/"))
					break;
			}
			for (iDB=1; iDB < stackB.size(); iDB++) {
				if (stackB.get(iDB).operation.equals("/"))
					break;
			}
			// Multiply numerators
			tStackA = new ArrayList <CvtrOperand>();
			for (idx=0; idx < iDA; idx++) {
				tStackA.add(stackA.get(idx));
			}
			tStackB = new ArrayList <CvtrOperand>();
			for (idx=0; idx < iDB; idx++) {
				tStackB.add(stackB.get(idx));
			}
			if ((newStack = multiplyGroups(tStackA, tStackB)) == null)
				return null;
			// Multiply denominators
			if (iDA < stackA.size()) {
				tStackA = new ArrayList <CvtrOperand>();
				for (idx=iDA; idx < stackA.size(); idx++) {
					tStackA.add(stackA.get(idx));
				}
				tStackB = new ArrayList <CvtrOperand>();
				for (idx=iDB; idx < stackB.size(); idx++) {
					if (idx == iDB && opA == div) {
						stackB.get(idx).operation = "*";
					}
					tStackB.add(stackB.get(idx));
				}
				if (tStackB.size() > 0) {
					if ((tStackA = multiplyGroups(tStackA, tStackB)) == null)
						return null;
				}
			// Denominator is multiplied by 1
			} else {
				tStackA = new ArrayList <CvtrOperand>();
				for (idx=iDB; idx < stackB.size(); idx++) {
					tStackA.add(stackB.get(idx));
				}
			}
			// Make denominator a separate group
			for (idx=0; idx < tStackA.size(); idx++) {
				syncOperands(newStack.get(0), tStackA.get(idx));
				tStackA.get(idx).nestGroup[tStackA.get(idx).nestLevel]++;
				newStack.add(tStackA.get(idx));
			}
			// (+|-) * (/)
		} else if (opA == addsub && opB == div) {
			for (iDB=1; iDB < stackB.size(); iDB++) {
				if (stackB.get(iDB).operation.equals("/"))
					break;
			}
			tStackB = new ArrayList <CvtrOperand>();
			while (iDB > 0) {
				tStackB.add(stackB.get(0));
				stackB.remove(0);
				iDB--;
			}
			if ((newStack = multiplyGroups(stackA, tStackB)) == null)
				return null;
			while (stackB.size() > 0) {
				newStack.add(stackB.get(0));
				stackB.remove(0);
			}
			// (/) * (+|-)
		} else if (opA == div && opB == addsub) {
			for (iDA=1; iDA < stackB.size(); iDA++) {
				if (stackA.get(iDA).operation.equals("/"))
					break;
			}
			tStackA = new ArrayList <CvtrOperand>();
			while (iDA > 0) {
				tStackA.add(stackA.get(0));
				stackA.remove(0);
				iDA--;
			}
			if ((newStack = multiplyGroups(tStackA, stackB)) == null)
				return null;
			while (stackA.size() > 0) {
				newStack.add(stackA.get(0));
				stackA.remove(0);
			}
		}

if (verbose) {
for (i=0; i < newStack.size(); i++) {
	System.out.print("   End multiplyGroups Operand " + i + ": " + newStack.get(i).value + newStack.get(i).unit + "^" +
			newStack.get(i).unitPower + ", " + newStack.get(i).operation + " (" + newStack.get(i).nestLevel + ", ");
	for (j=newStack.get(i).nestLevel; j >= 0; j--)
		System.out.print(">" + newStack.get(i).nestGroup[j]);
	System.out.println(")");
}
}
		multiplyUnits(newStack);
		return newStack;
	} /* end multiplyGroups */

/**
 * Divide two groups and return the reduced result.
 * 
 * @param stackA	The first group in the operation.
 * @param stackB	The second group in the operation.
 * 
 * @return ArrayList<CvtrOperand>	The resulting group.
 */
	private ArrayList<CvtrOperand> divideGroups(ArrayList<CvtrOperand> stackA, ArrayList<CvtrOperand> stackB) {
		int i, idx;
		int opA, opB, iDA, iDB;
		CvtrOperand oper;
		ArrayList<CvtrOperand> newStack = new ArrayList<CvtrOperand>(), tStackA, tStackB;
if (verbose)
System.out.println("Divide groups: " + stackA.size() + ", " + stackB.size());

		// Groups are single operands
		if (stackA.size() == 1 && stackB.size() == 1) {
			syncOperands(stackA.get(0), stackB.get(0));
			newStack = stackA;
			// No units in denominator
			if (stackB.get(0).indexType == -1) {
				newStack.get(0).value /= stackB.get(0).value;
			// Numerator and denominator units are the same
			} else if (stackB.get(0).indexType == newStack.get(0).indexType) {
				newStack.get(0).value /= stackB.get(0).value;
				// Unit power same, cancels units
				if (newStack.get(0).unitPower == stackB.get(0).unitPower) {
					newStack.get(0).clearUnit();
				// Denominator unit power reduces numerator power
				} else if (newStack.get(0).unitPower > stackB.get(0).unitPower) {
					newStack.get(0).unitPower -= stackB.get(0).unitPower;
				// Numerator unit power reduces denominator power
				} else {
					stackB.get(0).unitPower -= newStack.get(0).unitPower;
					newStack.get(0).clearUnit();
					newStack.add(stackB.get(0));
				}
			// Numerator and denominator units are different
			} else {
				newStack.add(stackB.get(0));
			}
			if ((newStack = reduceEquation(newStack)) == null)
				return null;
			return newStack;
		}

		// Get group types
		if ((opA = groupType(stackA)) < 0) {
			return null;
		} else if ((opB = groupType(stackB)) < 0) {
			return null;
		}
if (verbose)
System.out.println(" == OpA :: OpB: " + opA + " :: " + opB);

		// (+|-|N) / (N)
		if (opA <= addsub && opB == none) {
			for (idx=0; idx < stackA.size(); idx++) {
				if (!stackA.get(idx).groupUnit)
					stackA.get(idx).value /= stackB.get(0).value;
				newStack.add(stackA.get(idx));
			}
			stackB.get(0).value = 1;
			for (idx=0; idx < stackB.size(); idx++) {
				newStack.add(stackB.get(idx));
			}
		// (+|-|N) / (+|-)
		} else if (opA <= addsub && opB == addsub) {
			newStack = stackA;
			for (idx=0; idx < stackB.size(); idx++) {
				newStack.add(stackB.get(idx));
			}
		// (*|N) / (*|N)
		} else if ((opA == none || opA == mult) && (opB == none || opB == mult)) {
			// Determine if group units are the same
			for (idx=0; idx < stackA.size(); idx++) {
				for (i=0; i < stackB.size(); i++) {
					if (stackA.get(idx).unit.equals(stackB.get(i).unit))
						break;
				}
				if (i == stackB.size())
					break;
			}
			newStack = stackA;
			// Units are the same and the values can be divided
			if (idx == stackA.size()) {
					newStack.get(0).value /= stackB.get(0).value;
			// Units are different, merge the two groups
			} else {
				while (stackB.size() > 0) {
					syncOperands(newStack.get(0), stackB.get(0));
					newStack.add(stackB.get(0));
					stackB.remove(0);
				}
			}
		// (/) / (N)
		} else if (opA == div && opB <= addsub) {
			// Get first group numerator
			tStackA = new ArrayList<CvtrOperand>();
			while (stackA.size() > 0 && !stackA.get(0).operation.equals("/")) {
				tStackA.add(stackA.get(0));
				stackA.remove(0);
			}
			// Multiply first group denominator and second group
			stackB.get(0).operation = "*";
			if ((tStackB = multiplyGroups(stackA, stackB)) == null)
				return null;
			// New denominator becomes new group denominator
			newStack = tStackA;
			while (tStackB.size() > 0) {
				newStack.add(tStackB.get(0));
				tStackB.remove(0);
			}
		// (+|-|N) / (/)
		} else if (opA <= addsub && opB == div) {
			// Get second group numerator
			tStackB = new ArrayList<CvtrOperand>();
			tStackB.add(stackB.get(0));
			stackB.remove(0);
			while (stackB.size() > 0 && !stackB.get(0).operation.equals("/")) {
				tStackB.add(stackB.get(0));
				stackB.remove(0);
			}
			if (stackB.size() == 0 || tStackB.size() == 0) {
				calcError = "?? / Null";
				return null;
			}
			stackB.get(0).operation = "*";
			// Multiply first group and second group denominator
			if ((newStack = multiplyGroups(stackA, stackB)) == null)
				return null;
			// Second group numerator becomes new group denominator
			while (tStackB.size() > 0) {
				newStack.add(tStackB.get(0));
				tStackB.remove(0);
			}
		// (/) / (/)
		} else if (opA <= div && opB == div) {
			// Flip second group numerator and denominator
			for (iDB=1; iDB < stackB.size(); iDB++) {
				if (stackB.get(iDB).operation.equals("/"))
					break;
			}
			if (iDB == stackB.size()) {
				calcError = "?? / Null";
				return null;
			}
			tStackB = new ArrayList<CvtrOperand>();
			for (idx=iDB; idx < stackB.size(); idx++) {
				oper = stackB.get(idx).dupOperand();
				if (tStackB.size() == 0) {
					oper.operation = "*";
				}
				syncOperands(stackA.get(0), oper);
				tStackB.add(oper);
			}
			for (iDA=0; iDA < stackB.size(); iDA++) {
				if (stackA.get(iDA).operation.equals("/"))
					break;
			}
			if (iDA == stackA.size()) {
				calcError = "?? / Null";
				return null;
			}
			for (idx=0; idx < iDB; idx++) {
				syncOperands(stackA.get(iDA), stackB.get(idx));
				tStackB.add(stackB.get(idx));
			}
			// (a/b) / (c/d) ==> (a/b) * (d/c)
			if ((newStack = multiplyGroups(stackA, tStackB)) == null)
				return null;
		}

if (verbose) {
for (i=0; i < newStack.size(); i++) {
	System.out.print("   End divideGroups Operand " + i + ": " + newStack.get(i).value + newStack.get(i).unit + "^" +
			newStack.get(i).unitPower + ", " + newStack.get(i).operation + " (" + newStack.get(i).nestLevel + ", ");
	int j;
	for (j=newStack.get(i).nestLevel; j >= 0; j--)
		System.out.print(">" + newStack.get(i).nestGroup[j]);
	System.out.println(")");
}
}
		return newStack;
	} /* end divideGroups */

/**
 * Perform logic operations on two groups and return the reduced result.
 * At least one group must be a single operand, and logical operations
 * may not be performed between two operands with different units.
 * Logical operations are only performed on integers, and the result is
 * always an integer, regardless of the original operand values.
 * 
 * @param stackA	The first group in the operation.
 * @param stackB	The second group in the operation.
 * 
 * @return ArrayList<CvtrOperand>	The resulting group.
 */
	private ArrayList<CvtrOperand> logicalGroups(ArrayList<CvtrOperand> stackA, ArrayList<CvtrOperand> stackB) {
		int i, type, opGroup;
		long l1, l2;
		String opUnit;
		ArrayList<CvtrOperand> newStack = new ArrayList<CvtrOperand>();
if (verbose)
System.out.println("Logical groups: " + stackA.size() + ", " + stackB.size());

		// At least one group must be single operand
		if (stackA.size() > 1 && stackB.size() > 1) {
			calcError = "?? ()L()";
			return null;
		}
		// Do not allow logical operations on different units
		type = -1;
		opUnit = "";
		for (i=0; i < stackA.size(); i++) {
			if (stackA.get(i).indexType != type) {
				if (type < 0) {
					type = stackA.get(i).indexType;
					opUnit = stackA.get(i).unit;
				} else {
					calcError = "?? " + opUnit + " L " + stackA.get(i).unit;
					return null;
				}
			}
		}
		for (i=0; i < stackB.size(); i++) {
			if (stackB.get(i).indexType != type) {
				if (type < 0) {
					type = stackB.get(i).indexType;
					opUnit = stackB.get(i).unit;
				} else {
					calcError = "?? " + opUnit + " L " + stackB.get(i).unit;
					return null;
				}
			}
		}

		// Perform logical operations on operands of one group
		if (stackA.size() == 1) {
			if ((opGroup = groupType(stackB)) < 0) {
				return null;
			}
			newStack = stackB;
			l1 = (long) stackA.get(0).value;
			for (i=0; i < newStack.size(); i++) {
				if (!newStack.get(i).groupUnit) {
					l2 = (long) newStack.get(i).value;
					if (stackB.get(0).operation.equals("&")) {
						newStack.get(i).value = l1 & l2;
					} else if (stackB.get(0).operation.equals("|")) {
						newStack.get(i).value = l1 | l2;
					} else if (stackB.get(0).operation.equals("#")) {
						newStack.get(i).value = l1 ^ l2;
					}
if (verbose)
System.out.println(" == " + l1 + stackB.get(0).operation + l2 + " = " + newStack.get(i).value);
				}
			}
		} else {
			if ((opGroup = groupType(stackA)) < 0) {
				return null;
			}
			newStack = stackA;
			l2 = (long) stackB.get(0).value;
			for (i=0; i < newStack.size(); i++) {
				if (!newStack.get(i).groupUnit) {
					l1 = (long) newStack.get(i).value;
					if (stackB.get(0).operation.equals("&")) {
						newStack.get(i).value = l1 & l2;
					} else if (stackB.get(0).operation.equals("|")) {
						newStack.get(i).value = l1 | l2;
					} else if (stackB.get(0).operation.equals("#")) {
						newStack.get(i).value = l1 ^ l2;
					}
if (verbose)
System.out.println(" == " + l1 + stackB.get(0).operation + l2 + " = " + newStack.get(i).value);
				}
			}
		}
if (verbose) {
System.out.println(" == Single value L Op: " + opGroup);

for (i=0; i < newStack.size(); i++) {
	System.out.print("   End logicalGroups Operand " + i + ": " + newStack.get(i).value + newStack.get(i).unit + "^" +
			newStack.get(i).unitPower + ", " + newStack.get(i).operation + " (" + newStack.get(i).nestLevel + ", ");
	int j;
	for (j=newStack.get(i).nestLevel; j >= 0; j--)
		System.out.print(">" + newStack.get(i).nestGroup[j]);
	System.out.println(")");
}
}
		return newStack;
	} /* end logicalGroups */

/**
 * Perform modulo arithmetic on two groups and return the reduced result.  The
 * second group must be a single unit, and modulo arithmetic may not be performed
 * between two operands with different units.  Note that modulo arithmetic is
 * normally performed on integers, but Convertator requires the modulus to be
 * an integer, and allows the operand to be a floating point number.
 * 
 * @param stackA	The first group in the operation.
 * @param stackB	The second group in the operation.
 * 
 * @return ArrayList<CvtrOperand>	The resulting group.
 */
	private ArrayList<CvtrOperand> moduloGroup(ArrayList<CvtrOperand> stackA, ArrayList<CvtrOperand> stackB) {
		int i, op, opGroup;
		long l1, l2;
		double d;
		ArrayList<CvtrOperand> newStack = new ArrayList<CvtrOperand>(), tStackA, tStackB;
if (verbose)
System.out.println("Modulo groups: " + stackA.size() + ", " + stackB.size());

		// Second group must be single operand
		if (stackB.size() > 1) {
			// Attempt to reduce the modulo operand group
			tStackB = new ArrayList<CvtrOperand>();
			for (i=0; i < stackB.size(); i++) {
				tStackB.add(stackB.get(i).dupOperand());
				tStackB.get(i).nestLevel = 0;
				tStackB.get(i).function = false;
			}
			reduceEquation(tStackB);
			if (tStackB.size() > 1) {
				calcError = "?? ()%()";
				return null;
			}
			tStackB.get(0).nestLevel = stackB.get(0).nestLevel;
			stackB = tStackB;
		}
		// Modulo Divisor must be an integer
		l1 = (long) stackB.get(0).value;
		d = stackB.get(0).value - l1;
		if (d != 0) {
			calcError = "?? ()%" + stackB.get(0).value;
			return null;
		}

		// Do not allow modulo operation on different units
		for (i=0; i < stackA.size(); i++) {
			if (stackB.get(0).indexType >= 0 && stackA.get(i).indexType != stackB.get(0).indexType) {
				calcError = "?? " + stackA.get(i).unit + "%" + stackB.get(0).unit;
				return null;
			}
		}
		if ((opGroup = groupType(stackA)) < 0) {
			return null;
		}

		// Handle division
		if (groupType(stackA) == div) {
			// Reduce division inline
			if (stackA.size() == 2 && (stackA.get(0).unit.equals(stackB.get(0).unit) &&
					stackA.get(0).unitPower == stackB.get(0).unitPower)) {
				stackA.get(0).value = stackA.get(0).value / stackA.get(1).value;
				stackA.remove(1);
			// Attempt to reduce group division
			} else {
				for (op=1; op < stackA.size(); op++) {
					if (stackA.get(op).operation.equals("/"))
						break;
				}
				tStackA = new ArrayList<CvtrOperand>();
				for (i=0; i < op; i++)
					tStackA.add(stackA.get(i));
				tStackB = new ArrayList<CvtrOperand>();
				for (i=op; i < stackA.size(); i++)
					tStackB.add(stackA.get(i));
				stackA = divideGroups(tStackA, tStackB);
			}
		}
		newStack = stackA;
		l2 = (long) stackB.get(0).value;
		for (i=0; i < newStack.size(); i++) {
			if (!newStack.get(i).groupUnit) {
				if (newStack.get(i).operation.equals("/"))
					break;
				l1 = (long) newStack.get(i).value;
				d = newStack.get(i).value - l1;
				newStack.get(i).value = (l1 % l2) + d;
if (verbose)
System.out.println(" == " + l1 + "." + d + " % " + l2 + " = " + newStack.get(i).value);
			}
		}
if (verbose) {
System.out.println(" == Single value % Op: " + opGroup);

for (i=0; i < newStack.size(); i++) {
	System.out.print("   End moduloGroup Operand " + i + ": " + newStack.get(i).value + newStack.get(i).unit + "^" +
			newStack.get(i).unitPower + ", " + newStack.get(i).operation + " (" + newStack.get(i).nestLevel + ", ");
	int j;
	for (j=newStack.get(i).nestLevel; j >= 0; j--)
		System.out.print(">" + newStack.get(i).nestGroup[j]);
	System.out.println(")");
}
}
		return newStack;
	} /* end moduloGroup */

/**
 * Calculate the power of a group.  The power must be a single operand with no unit
 * and it's value must be an integer.
 * 
 * @param stackA	The group of operands to be raised to a power.
 * @param stackB	The power of the group.
 * 
 * @return ArrayList<CvtrOperand>	The resulting stack of operands.
 */
	private ArrayList<CvtrOperand> powerGroup(ArrayList<CvtrOperand> stackA, ArrayList<CvtrOperand> stackB) {
		int i, op;
		long l1;
		double d;
		ArrayList<CvtrOperand> newStack = new ArrayList<CvtrOperand>(), tStackA, tStackB;
if (verbose)
System.out.println("Power group: " + stackA.size() + ", " + stackB.size());
		// Power must be single operand
		if (stackB.size() > 1) {
			// Attempt to reduce the power operand group
			tStackB = new ArrayList<CvtrOperand>();
			for (i=0; i < stackB.size(); i++) {
				tStackB.add(stackB.get(i).dupOperand());
				tStackB.get(i).nestLevel = 0;
				tStackB.get(i).function = false;
			}
			reduceEquation(tStackB);
			if (tStackB.size() > 1) {
				calcError = "?? ()^()";
				return null;
			}
			tStackB.get(0).nestLevel = stackB.get(0).nestLevel;
			stackB = tStackB;
		}
		// Power may not have a unit
		if (stackB.get(0).indexType >= 0) {
			calcError = "?? ()^" + stackB.get(0).unit;
			return null;
		}

		// Reduce operand group if possible
		if ((stackA = reduceEquation(stackA)) == null) {
			return null;
		}
		// Handle division
		if (groupType(stackA) == div) {
			// Reduce division inline
			if (stackA.size() == 2 && (stackA.get(0).unit.equals(stackB.get(0).unit) &&
					stackA.get(0).unitPower == stackB.get(0).unitPower)) {
				stackA.get(0).value = stackA.get(0).value / stackA.get(1).value;
				stackA.remove(1);
			// Attempt to reduce group division
			} else {
				for (op=1; op < stackA.size(); op++) {
					if (stackA.get(op).operation.equals("/"))
						break;
				}
				tStackA = new ArrayList<CvtrOperand>();
				for (i=0; i < op; i++)
					tStackA.add(stackA.get(i));
				tStackB = new ArrayList<CvtrOperand>();
				for (i=op; i < stackA.size(); i++)
					tStackB.add(stackA.get(i));
				stackA = divideGroups(tStackA, tStackB);
			}
		}

		if (stackA.size() == 1) {
			newStack.add(stackA.get(0));
			newStack.get(0).value = Math.pow(stackA.get(0).value, stackB.get(0).value);
			if (newStack.get(0).indexType >= 0)
				newStack.get(0).unitPower = (int) stackB.get(0).value;
		// Power must be an integer for groups
		} else {
			l1 = (long) stackB.get(0).value;
			d = stackB.get(0).value - l1;
			if (d != 0) {
				calcError = "?? ()^i.d";
				return null;
			}
			newStack = stackA;
			for (i=1; i < (int) stackB.get(0).value; i++) {
				newStack = multiplyGroups(newStack, stackA);
			}
		}

if (verbose) {
for (i=0; i < newStack.size(); i++) {
System.out.print("   End powerGroup Operand " + i + ": " + newStack.get(i).value + newStack.get(i).unit + "^" +
	newStack.get(i).unitPower + ", " + newStack.get(i).operation + " (" + newStack.get(i).nestLevel + ", ");
int j;
for (j=newStack.get(i).nestLevel; j >= 0; j--)
System.out.print(">" + newStack.get(i).nestGroup[j]);
System.out.println(")");
}
}
		return newStack;
	} /* end powerGroup */

/**
 * Calculate the function for the group and return the resulting group.  The base
 * of all operands in the group must be the same and must be either degrees or radians.
 * Also, the units of all operands must be the same, including the unit power.
 * 
 * @param functionStack	The group that contains the function to be calculated.
 * @param operStack	The stack of operands to be used in the function.
 * 
 * @return ArrayList <CvtrOperand>	The resulting group.
 */
	private ArrayList <CvtrOperand> calculateFunctionGroup(ArrayList <CvtrOperand> functionStack, ArrayList<CvtrOperand> operStack) {
		int idx;
		long l;
		String function;
		ArrayList <CvtrOperand> newStack = new ArrayList <CvtrOperand>();
if (verbose)
System.out.println("Calculate function group: " + functionStack.size());

		// Verify first operand is function
		if (!functionStack.get(0).function) {
			calcError = "?? Function Group";
			return null;
		}
		if ((function = functionStack.get(0).sValue) == null)
			return operStack;

		// Reduce operand group
		if ((newStack = reduceEquation(operStack)) == null) {
			return null;
		}
		for (idx=1; idx < operStack.size(); idx++) {
			// NOT each operand
			if (function.equals("!")) {
				if (!operStack.get(idx).groupUnit) {
					l = (long) operStack.get(idx).value;
					operStack.get(idx).value = ~l;
				}
			// Do not allow square root group
			} else if (function.equals("\\")) {
				if (!operStack.get(idx).groupUnit) {
					calcError = "?? " + function + "(" + operStack.get(idx).value + operStack.get(idx).unit + "^" + operStack.get(idx).unitPower + ")";
					return null;
				}
			// Only single value allowed in function
			} else {
				if (trueSize(operStack) > 1) {
					calcError = "?? " + function + "()";
					return null;
				}
			}
		}

		try {
			operStack.get(0).value = calculateFunction(function, operStack.get(0));
		} catch (IllegalArgumentException err) {
			calcError = "?? " + err.getMessage();
			return null;
		}

		return newStack;
	} /* end calculateFunction */

/**
 * Calculate the function and return its value.
 * 
 * @param function	The Function code, where:
 * <ul>
 *   <li>S = Sine</li>
 *   <li>O = Cosine</li>
 *   <li>T = Tangent</li>
 *   <li>L = Logarithm</li>
 *   <li>l = Natural logarithm</li>
 *   <li>\ = Square root</li>
 *   <li>! = NOT</li>
 * </ul>
 * @param operand	The value to be used in the calculation.
 * 
 * @return double	The calculated value.
 * 
 * @throws IllegalArgumentException	If the function or argument is invalid.  The text of
 *									the error can be used in the Convertator result field.
 */
	private double calculateFunction(String function, CvtrOperand operand) throws IllegalArgumentException {
		long l;
		double d = 0, dRad;
		IllegalArgumentException exception;
if (verbose)
System.out.println("Calculate function: " + function + " " + operand.value);

		// Verify operand is numeric
		if (operand.sValue != null) {
			exception = new IllegalArgumentException(function + " " + operand.sValue);
			throw exception;
		// Verify no units (except for unary operators)
		} else if ((!function.equals("!") && !function.equals("\\")) && operand.indexType >= 0) {
			exception = new IllegalArgumentException(function + "(" + operand.unit + ")");
			throw exception;
		}
		// Verify base of operands
		if ((function.equals("S") || function.equals("O") || function.equals("T")) &&
				(operand.base != deg && operand.base != rad && operand.base != dec)) {
			exception = new IllegalArgumentException(function + "(!g|r)");
			throw exception;
		}

// TODO: Add functionality for arc (sine/cosine/tangent)
		// Calculate arc sine for degrees and sine for radians or any other base
		if (function.equals("S")) {
			if (operand.base == rad) {
				d = Math.sin(operand.value);
			} else if (operand.base == deg) {
				dRad = Math.toRadians((operand.value % 360.0));
				d = Math.sin(dRad);
			} else {
				d = Math.sin(operand.value);
			}
			operand.base = dec;
		// Calculate arc cosine for degrees and cosine for radians or any other base
		} else if (function.equals("O")) {
			if (operand.base == rad) {
				d = Math.cos(operand.value);
			} else if (operand.base == deg) {
				dRad = Math.toRadians((operand.value % 360.0));
				d = Math.cos(dRad);
			} else {
				d = Math.cos(operand.value);
			}
			operand.base = dec;
		// Calculate arc tangent for degrees and tangent for radians or any other base
		} else if (function.equals("T")) {
			if (operand.base == rad) {
				if (operand.value == Math.toRadians(90) || operand.value == Math.toRadians(-270)) {
					exception = new IllegalArgumentException("1/0");
					throw exception;
				} else if (operand.value == Math.toRadians(-90) || operand.value == Math.toRadians(270)) {
					exception = new IllegalArgumentException("-1/0");
					throw exception;
				}
				d = Math.tan(operand.value);
			} else if (operand.base == deg) {
				d = operand.value % 360;
				if (d == 90 || d == -270) {
					exception = new IllegalArgumentException("1/0");
					throw exception;
				} else if (d == -90 || d == 270) {
					exception = new IllegalArgumentException("-1/0");
					throw exception;
				}
				dRad = Math.toRadians((operand.value % 360.0));
				d = Math.tan(dRad);
			} else {
				d = Math.tan(operand.value);
			}
			operand.base = dec;
		// Calculate the logarithm of a value
		} else if (function.equals("L")) {
			if (operand.value < 0) {
				exception = new IllegalArgumentException("L(<0)");
				throw exception;
			}
			d = Math.log10(operand.value);
		// Calculate the natural logarithm of a value
		} else if (function.equals("l")) {
			if (operand.value < 0) {
				exception = new IllegalArgumentException("l(<0)");
				throw exception;
			}
			d = Math.log(operand.value);
		// Get the operand square root
		} else if (function.equals("\\")) {
			d = Math.sqrt(operand.value);
		// NOT the operand
		} else if (function.equals("!")) {
			l = (long) operand.value;
			d = ~l;
		} else {
			exception = new IllegalArgumentException(function);
			throw exception;
		}

		return d;
	} /* end calculateFunction */

/**
 * Reduce the value of units in a final equation.  In particular, the values of numerators
 * and denominators in division are reduced as much as possible.
 * 
 * @param finalStack	The list of operands to be reduced
 * 
 * @return boolean	True if successful, otherwise false.
 */
	private boolean reduceFinal (ArrayList <CvtrOperand> finalStack) {
		int i, j, k, idx, iD, groupCount;
		int[] unitPowerDiff = new int[UnitCategories.size()];
		long l1, l2;
		double minD;
		String unit;

if (verbose) {
System.out.println("Reduce final equation: " + finalStack.size());
for (i=0; i < finalStack.size(); i++) {
	System.out.print("   Final equation Operand " + i + ": " + finalStack.get(i).value + finalStack.get(i).unit + "^" +
			finalStack.get(i).unitPower + ", " + finalStack.get(i).operation + " (" + finalStack.get(i).nestLevel + ", ");
	for (j=finalStack.get(i).nestLevel; j >= 0; j--) {
		if (finalStack.get(i).nestGroup == null)
			System.out.print("-1");
		else
			System.out.print(">" + finalStack.get(i).nestGroup[j]);
	}
	System.out.println(")" + ", groupUnit: " + finalStack.get(i).groupUnit);
}
}

		// Find the denominator if one exists
		for (iD=0; iD < finalStack.size(); iD++) {
			if (finalStack.get(iD).operation.equals("/"))
				break;
		}
if (verbose)
System.out.println("  Denom index: " + iD);
		// Equation includes division
		if (iD < finalStack.size()) {
			minD = finalStack.get(iD).value;
			for (idx=iD + 1; idx < finalStack.size(); idx++) {
				if (!finalStack.get(idx).groupUnit && minD > finalStack.get(idx).value)
					minD = finalStack.get(idx).value;
			}
if (verbose)
System.out.println("  Min common denom: " + minD);
			finalStack.get(0).value /= minD;
			for (idx=1; idx < finalStack.size(); idx++) {
				if (!finalStack.get(idx).groupUnit &&
						(finalStack.get(idx).operation.length() == 0 || finalStack.get(idx).operation.endsWith("/") ||
						finalStack.get(idx).operation.endsWith("+") || finalStack.get(idx).operation.equals("-")))
					finalStack.get(idx).value /= minD;
			}
			// Initialize unit power difference array
			for (i=0; i < UnitCategories.size(); i++)
				unitPowerDiff[i] = 0;
			// Get number of denominator groups
			i = 0;
			for (idx=iD; idx < finalStack.size(); idx++) {
				if (finalStack.get(idx).indexType >= 0 && unitPowerDiff[finalStack.get(idx).indexType] < finalStack.get(idx).unitPower)
					unitPowerDiff[finalStack.get(idx).indexType] = finalStack.get(idx).unitPower;
				if (!finalStack.get(idx).groupUnit)
					i++;
			}
if (verbose)
System.out.println(" == Denom groups: " + i);
			// Test for denominator units cancelling with numerator units
			if (i == 1) {
				// Get number of numerator unit groups
				groupCount = 0;
				for (idx=0; idx < iD; idx++) {
					if (finalStack.get(idx).indexType >= 0 && unitPowerDiff[finalStack.get(idx).indexType] < finalStack.get(idx).unitPower)
						unitPowerDiff[finalStack.get(idx).indexType] = finalStack.get(idx).unitPower;
					if (!finalStack.get(idx).groupUnit)
						groupCount++;
				}
if (verbose)
System.out.println(" == Numer groups: " + groupCount);
				// Check each denominator unit for reduction with numerator
				idx = iD;
				while (idx < finalStack.size()) {
					// Count number of matching units
					j = 0;
					for (i=0; i < iD; i++) {
						// Unit can be cancelled
if (verbose)
System.out.println(" == Unit types: N " + finalStack.get(i).indexType + ", D " + finalStack.get(idx).indexType);
						if (finalStack.get(idx).indexType >= 0 && finalStack.get(idx).indexType == finalStack.get(i).indexType) {
							 k = finalStack.get(i).unitPower - finalStack.get(idx).unitPower;							
							 if (k < unitPowerDiff[finalStack.get(idx).indexType])
							 	unitPowerDiff[finalStack.get(idx).indexType] = k;
							j++;
						}
					}
if (verbose)
System.out.println(" == Numer matches: " + j);
					// Every unit group has match with deonominator unit
					if (j == groupCount) {
						for (i=0; i < iD; i++) {
							if (finalStack.get(idx).indexType == finalStack.get(i).indexType) {
								// Denominator unit power <= all numerator unit powers
								if (unitPowerDiff[finalStack.get(idx).indexType] >= 0)
									finalStack.get(i).unitPower -= finalStack.get(idx).unitPower;
								// Denominator unit power > some numerator unit powers
								else
									finalStack.get(i).unitPower += unitPowerDiff[finalStack.get(idx).indexType];
								// Numerator unit cancelled
								if (finalStack.get(i).unitPower == 0) {
if (verbose)
System.out.println(" == Units cancel: " + finalStack.get(i).unit + "^" + finalStack.get(i).unitPower + " // " +
	finalStack.get(idx).unit + "^" + finalStack.get(idx).unitPower);
									// Single operand
									if ((i + 1) == iD) {
										finalStack.get(i).clearUnit();
									// Operand is placeholder
									} else if (finalStack.get(i).groupUnit) {
										finalStack.remove(i);
										idx--;
										iD--;
									// Operand is primary in group
									} else {
										if (finalStack.get(i+1).groupUnit) {
											finalStack.get(i+1).groupUnit = false;
											finalStack.get(i+1).value = finalStack.get(i).value;
											finalStack.get(i+1).operation = finalStack.get(i).operation;
											finalStack.remove(i);
											idx--;
											iD--;
										} else {
											finalStack.get(i).clearUnit();
										}
									}
								}
							}
						}
						// Denominator unit is cancelled
						if (unitPowerDiff[finalStack.get(idx).indexType] >= 0) {
if (verbose)
System.out.println(" == Remove denom unit(" + idx + "): " + finalStack.get(idx).unit + "^" + finalStack.get(idx).unitPower);
							if (!finalStack.get(idx).groupUnit && (idx + 1) < finalStack.size()) {
								finalStack.get(idx+1).groupUnit = false;
								finalStack.get(idx+1).value = finalStack.get(idx).value;
								finalStack.get(idx+1).operation = finalStack.get(idx).operation;
							}
							finalStack.remove(idx);
						// Denominator unit power is reduced
						} else {
							finalStack.get(idx).unitPower += unitPowerDiff[finalStack.get(idx).indexType];
						}
					} else
						idx++;
				}
			}
			// Denominator is constant, remove it
			if (iD == (finalStack.size() - 1) && finalStack.get(iD).indexType < 0 && finalStack.get(iD).value == 1D) {
				finalStack.remove(iD);
			}
		}
		// Primary operand in group must have units
		idx = 0;
		while (idx < (finalStack.size() - 1)) {
			// Primary operand has no unit
			if (finalStack.get(idx).indexType < 0 && finalStack.get(idx + 1).groupUnit) {
				// Set multiplication group value
				if (finalStack.get(idx + 1).operation.equals("*"))
					finalStack.get(idx + 1).value *= finalStack.get(idx).value;
				// Set logical group value
				else {
					l1 = (long) finalStack.get(idx).value;
					l2 = (long) finalStack.get(idx + 1).value;
					if (finalStack.get(idx + 1).operation.equals("&")) 
						 finalStack.get(idx + 1).value = l1 & l2;
					else if (finalStack.get(idx + 1).operation.equals("|"))
						 finalStack.get(idx + 1).value = l1 | l2;
					else if (finalStack.get(idx + 1).operation.equals("#"))
						 finalStack.get(idx + 1).value = l1 ^ l2;
				}
				// Make second operand in group primary
				finalStack.get(idx + 1).operation = finalStack.get(idx).operation;
				finalStack.get(idx + 1).groupUnit = false;
				finalStack.remove(idx);
			// Clean up extraneous place holders
			} else if (finalStack.get(idx + 1).indexType < 0 && finalStack.get(idx + 1).groupUnit) {
				finalStack.remove(idx + 1);
			} else
				idx++;
		}
		// Find the denominator if one exists
		for (iD=0; iD < finalStack.size(); iD++) {
			if (finalStack.get(iD).operation.equals("/"))
				break;
		}
if (verbose)
System.out.println("  Final denom index: " + iD);
		// Set final nest levels
		for (idx=0; idx < finalStack.size(); idx++) {
			finalStack.get(idx).nestLevel = 0;
			if (finalStack.get(idx).nestGroup != null) {
				if (idx < iD)
					finalStack.get(idx).nestGroup[0] = 0;
				else
					finalStack.get(idx).nestGroup[0] = 1;
			}
		}
		// Reduce final equation
		finalStack = reduceEquation(finalStack);
		// Make a final consolidation of operand units
		for (idx=0; idx < finalStack.size(); idx++) {
			if (!finalStack.get(idx).groupUnit && finalStack.get(idx).base < ddBase &&
					finalStack.get(idx).operation.equals("+") && finalStack.get(idx).value < 0) {
				finalStack.get(idx).operation = "-";
				finalStack.get(idx).value *= -1;
			}
			i = idx + 1;
			while (i < finalStack.size() && finalStack.get(i).groupUnit) {
				if (finalStack.get(idx).indexType == finalStack.get(i).indexType) {
					finalStack.get(idx).unitPower += finalStack.get(i).unitPower;
					finalStack.remove(i);
				} else
					i++;
			}
		}

		// Do not allow multiple units in logic operations
		unit = "";
		i = -1;
		for (idx=0; idx < finalStack.size(); idx++) {
			if (finalStack.get(idx).indexType != i) {
				if (i < 0) {
					i = finalStack.get(idx).indexType;
					unit = finalStack.get(idx).unit;
				} else if (finalStack.get(idx).operation.equals("&") ||
						finalStack.get(idx).operation.equals("|") ||
						finalStack.get(idx).operation.equals("#")) {
					calcError = "?? " + unit + finalStack.get(idx).operation + finalStack.get(idx).unit;
					return false;
				}
			}
		}

if (verbose) {
for (i=0; i < finalStack.size(); i++) {
	System.out.print("   End final equation Operand " + i + ": " + finalStack.get(i).value + finalStack.get(i).unit + "^" +
			finalStack.get(i).unitPower + ", " + finalStack.get(i).operation + " (" + finalStack.get(i).nestLevel + ", ");
	for (j=finalStack.get(i).nestLevel; j >= 0; j--) {
		if (finalStack.get(i).nestGroup == null)
			System.out.print("-1");
		else
			System.out.print(">" + finalStack.get(i).nestGroup[j]);
	}
	System.out.println(")" + ", groupUnit: " + finalStack.get(i).groupUnit);
}
}
		return true;
	} /* end reduceFinal */

/**
 * Get the size of the current Result array.
 * 
 * @return int	The size of the array.
 */
	public int getResultSize() {
		if (EquationResult != null)
			return EquationResult.length;
		else
			return 0;
	}

/**
 * Get the list of base and units from the final equation.
 * 
 * @return CvtrResult[]	The list of base and units.
 */
	public CvtrResult[] getResultUnits() {
		int i, j, idx;
		String temp;

		if (EquationResult != null)
			return EquationResult;
		else if (operandStack == null)
			return null;

		// Get the result information
		i = 1;
		// Get number of different unit categories
		for (idx=0; idx < UnitCategories.size(); idx++) {
			for (j=0; j < operandStack.size(); j++) {
				if (operandStack.get(j).indexType == idx) {
					i++;
					break;
				}
			}
		}
		// Create list of base and units
if (verbose)
System.out.println("Get equation result units + base: " + i);
		EquationResult = new CvtrResult[i];
		j = dec;
		for (i=0; i < operandStack.size(); i++) {
			if (operandStack.get(i).base >= 0) {
				j = operandStack.get(i).base;
				break;
			}
		}
		i = sBases[j].indexOf(" (");
		if (i > 2)
			temp = sBases[j].substring(0, i);
		else
			temp = sBases[j];
		EquationResult[0] = new CvtrResult(true, temp, 0, j, 0);
		i = 1;
		for (idx=0; idx < UnitCategories.size(); idx++) {
			for (j=0; j < operandStack.size(); j++) {
				if (operandStack.get(j).indexType == idx) {
					if (i < EquationResult.length) {
						EquationResult[i++] = new CvtrResult(false, operandStack.get(j).unit, operandStack.get(j).conversionFactor,
							operandStack.get(j).indexType, operandStack.get(j).indexUnit);
					} else {
						calcError = "?? No Unit";
						return null;
					}
					break;
				}
			}
		}
if (verbose)
System.out.println("Get equation result: " + EquationResult.length);
		return EquationResult;
	} /* end getResultUnits */

/**
 * Get the result of the calculation.
 * 
 * @param format	The base and units to be used for the answer
 * 
 * @return String	The formatted result.
 */
	public String getResult (CvtrResult[] format) {
		int idx, i, j;
		int resBase = dec;
		long l;
		double c1, c2, d, divOp;
		boolean deg2rad = false, rad2deg = false;
		String answer, decVal, stringVal, dotVal, unitVal;
		String dPattern;
		NumberFormat nf;
		DecimalFormat df;
if (verbose)
System.out.println("Get Result");

		if (operandStack.size() == 0) {
			return "?? Null";
		}
		// Parse result format
		if (format == null) {
			format = EquationResult;
		}
		divOp = 1;
		for (idx=0; idx < format.length; idx++) {
			if (format[idx].resultBase) {
				resBase = format[idx].indexType;
			} else {
				for (i=0; i < operandStack.size(); i++) {
					if (operandStack.get(i).indexType == format[idx].indexType) {
						// Unit needs to be converted
						if (operandStack.get(i).indexUnit != format[idx].indexUnit) {
							try {
								// Handle unit power
								if (operandStack.get(i).unitPower > 1) {
									c1 = Math.pow(format[idx].conversionFactor, (double) operandStack.get(i).unitPower);
									c2 = Math.pow(operandStack.get(i).conversionFactor, (double) operandStack.get(i).unitPower);
								} else {
									c1 = format[idx].conversionFactor;
									c2 = operandStack.get(i).conversionFactor;
								}
								d = (operandStack.get(i).value * c1) / c2;
								if (operandStack.get(i).operation.equals("/"))
									divOp = d;
								// Unit is placeholder, update actual value
								if (operandStack.get(i).groupUnit) {
									j = i;
									while (j >= 0 && operandStack.get(j).groupUnit)
										j--;
									if (j < 0) {
										return "?? No Value";
										}
									operandStack.get(j).value *= d;
									if (operandStack.get(j).operation.equals("/"))
										divOp *= d;
								// Operand is actual value
								} else {
									operandStack.get(i).value = d;
								}
								operandStack.get(i).indexUnit = format[idx].indexUnit;
								operandStack.get(i).unit = format[idx].resultAbbrev;
								operandStack.get(i).conversionFactor = format[idx].conversionFactor;
							} catch (ArithmeticException err) {
								return "?? Math Err";
							}
						}
					}
				}
			}
		}
		if (divOp != 1) {
			for (i=0; i < operandStack.size(); i++) {
				if (!operandStack.get(i).groupUnit)
					operandStack.get(i).value /= divOp;
			}
		}

		// Format the answer
		answer = "";
		for (idx=0; idx < operandStack.size(); idx++) {
if (verbose) {
if (resBase >= ddBase) {
	if (operandStack.get(idx).sValue == null)
		System.out.println("  Get operand: " + operandStack.get(idx).value);
	else
		System.out.println("  Get operand: " + operandStack.get(idx).sValue);
} else
System.out.println("  Get operand: " + operandStack.get(idx).value + operandStack.get(idx).unit);
}
			if (operandStack.get(idx).unit.length() > 0) {
				if (operandStack.get(idx).unitPower > 1) {
					unitVal = operandStack.get(idx).unit + "^" + operandStack.get(idx).unitPower;
				} else {
					unitVal = operandStack.get(idx).unit;
				}
			} else
				unitVal = "";
			if (resBase == bin) {
				if (idx == 0)
					answer += "n";
				else {
					if (!operandStack.get(idx).groupUnit)
						answer += " ";
					answer += operandStack.get(idx).operation;
				}
				if (operandStack.get(idx).groupUnit) {
					answer += unitVal;
				} else {
					l = (long) operandStack.get(idx).value;
					answer += " " + Long.toBinaryString(l) + " " + unitVal;
				}
			} else if (resBase == oct) {
				if (idx == 0)
					answer += "o";
				else {
					if (!operandStack.get(idx).groupUnit)
						answer += " ";
					answer += operandStack.get(idx).operation;
				}
				if (operandStack.get(idx).groupUnit) {
					answer += unitVal;
				} else {
					l = (long) operandStack.get(idx).value;
					answer += " " + Long.toOctalString(l) + " " + unitVal;
				}
			} else if (resBase == dec) {
				if (idx > 0) {
					if (!operandStack.get(idx).groupUnit)
						answer += " ";
					answer += operandStack.get(idx).operation;
				}
				decVal = "1";
				if (decVal.length() > 0) {
					if (iPrecision == 0) {
						l = (long) operandStack.get(idx).value;
						decVal = "" + l;
					} else {
						nf = NumberFormat.getInstance();
						df = (DecimalFormat) nf;
						// Handle scientific notation from preferences
						if (scientificNotation)
							dPattern = "###0.";
						else
							dPattern = "#0.0";
						for (i=1; i < iPrecision; i++)
							dPattern += "#";
						if (scientificNotation)
							dPattern += "E0";
						df.applyPattern(dPattern);
						decVal = df.format(operandStack.get(idx).value);
					}
				}
				if (decVal.length() == 0 || operandStack.get(idx).groupUnit) {
					answer += unitVal;
				} else {
					answer += " " + decVal + " " + unitVal;
				}
			} else if (resBase == hex) {
				if (idx == 0)
					answer += "x";
				else {
					if (!operandStack.get(idx).groupUnit)
						answer += " ";
					answer += operandStack.get(idx).operation;
				}
				if (operandStack.get(idx).groupUnit) {
					answer += unitVal;
				} else {
					l = (long) operandStack.get(idx).value;
					answer += " " + Long.toHexString(l) + " " + unitVal;
				}
			} else if (resBase == deg || resBase == rad) {
				if (idx == 0) {
					if (resBase == deg) {
						answer += "g";
						if (operandStack.get(idx).base == rad ||
							operandStack.get(idx).base == dec)
							rad2deg = true;
					} else {
						answer += "r";
						if (operandStack.get(idx).base == deg)
							deg2rad = true;
					}
				} else {
					if (!operandStack.get(idx).groupUnit)
						answer += " ";
					answer += operandStack.get(idx).operation;
				}
				if (deg2rad)
					d = Math.toRadians(operandStack.get(idx).value);
				else if (rad2deg)
					d = Math.toDegrees(operandStack.get(idx).value);
				else
					d = operandStack.get(idx).value;
				if (iPrecision == 0) {
					l = (long) d;
					decVal = "" + l;
				} else {
					nf = NumberFormat.getInstance();
					df = (DecimalFormat) nf;
					// Handle scientific notation from preferences
					if (scientificNotation)
						dPattern = "###0.";
					else
						dPattern = "#0.0";
					for (i=1; i < iPrecision; i++)
						dPattern += "#";
					if (scientificNotation)
						dPattern += "E0";
					df.applyPattern(dPattern);
					decVal = df.format(d);
				}
				if (operandStack.get(idx).groupUnit) {
					answer += unitVal;
				} else {
					answer += " " + decVal + " " + unitVal;
				}
			// Convert numeric value to dotted decimal string
			} else if (resBase == dot) {
				if (operandStack.get(idx).base == dot) {
					answer += " " + operandStack.get(idx).sValue;
				} else {
					l = (long) operandStack.get(idx).value;
					stringVal = decVal = dotVal = "";
					for (i=0; i < 4; i++) {
						stringVal = (l & 0xff) + dotVal + decVal;
						decVal = stringVal;
						dotVal = ".";
						l >>= 8;
					}
					answer += " " + stringVal;
				}
			// Convert numeric value to ASCII string
			} else if (resBase == asc) {
				if (operandStack.get(idx).base == asc) {
					answer += " " + operandStack.get(idx).sValue;
				} else {
					l = (long) operandStack.get(idx).value;
					stringVal = decVal = "";
					for (i=0; i < 4; i++) {
						if ((l & 0xff) != 0) {
							stringVal = (char) (l & 0xff) + decVal;
							decVal = stringVal;
						}
						l >>= 8;
					}
					answer += stringVal;
				}
			// Convert numeric value to Unicode string
			} else if (resBase == uni) {
				if (operandStack.get(idx).base == uni) {
					answer += " " + operandStack.get(idx).sValue;
				} else {
					l = (long) operandStack.get(idx).value;
					stringVal = decVal = "";
					for (i=0; i < 2; i++) {
						if ((l & 0xffff) != 0) {
							stringVal = (char) (l & 0xffff) + decVal;
							decVal = stringVal;
						}
						l >>= 16;
					}
					answer += stringVal;
				}
			}
		}


		calcProgress += answer + "\n";
if (verbose)
System.out.println("\n" + calcProgress + "\n");
		return answer;
	} /* end getResult */

/**
 * Build the string display of a list of Convertator operands.
 * 
 * @param stack	The stack to be displayed.
 * 
 * @return String	The string representation of the operands.
 */
	private String displayStack(ArrayList<CvtrOperand> stack) {
		int idx, nLevel, startLevel = 0;
		int[] nGroup = new int[maxNestLevel];
		String equation = "";

		if (stack == null || stack.size() == 0)
			return " =??=";

		for (idx=0; idx < stack.get(0).nestLevel; idx++) {
			if (stack.get(0).function)
				nGroup[idx] = 0;
			else
				nGroup[idx] = stack.get(0).nestGroup[idx];
		}

		// Get the nesting level
		if (stack.get(0).nestLevel > 0) {
			if ((startLevel = (stack.get(0).nestLevel - 1)) < 0)
				startLevel = 0;
		}
		nLevel = startLevel;

		for (idx=0; idx < stack.size(); idx++) {
			// Add parentheses if needed
			if (stack.get(idx).nestGroup != null) {
				while (nLevel > stack.get(idx).nestLevel || nGroup[nLevel] < stack.get(idx).nestGroup[nLevel]) {
					equation += ")";
					if (nLevel > stack.get(idx).nestLevel) {
						nLevel--;
					} else {
						nGroup[nLevel]++;
						nLevel--;
					}
					if (nLevel < 0)
						nLevel = 0;
				}
				// Display operation for numeric equations
				if (idx > 0 && stack.get(idx).sValue == null)
					equation += " " + stack.get(idx).operation + " ";
				// Add parentheses if needed
				while (nLevel < stack.get(idx).nestLevel) {
					equation += "(";
					nGroup[nLevel] = stack.get(idx).nestGroup[nLevel];
					nLevel++;
					if (nGroup[nLevel] < stack.get(idx).nestGroup[nLevel])
						nGroup[nLevel] = stack.get(idx).nestGroup[nLevel];
				}
			}
			// Display base value and unit, as appropriate
			if (stack.get(idx).base >= 0)
				equation += getBaseSign(sBases[stack.get(idx).base], false);
			if (stack.get(idx).sValue != null) {
				equation += "'" + stack.get(idx).sValue + "'";
			} else {
				if (stack.get(idx).groupUnit)
					equation += stack.get(idx).unit;
				else
					equation += stack.get(idx).value + stack.get(idx).unit;
			}
		}
		// Add parentheses if needed
		while (nLevel > startLevel) {
			equation += ")";
			nLevel--;
		}

		return equation;
	}

/**
 * Show the steps taken to get the result.
 * 
 * @return String	The list of start and end values of each step.
 */
	public String showWork() {
		return calcProgress;
	}

}
