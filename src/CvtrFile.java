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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The Convertator File class manages input and output of data in files.  This
 * includes configuration information and conversion values.  The three data types,
 * Units, Constants, and Equations, are stored in Convertator format file.  See
 * getCvtrFile for the description of the file format.
 */
public class CvtrFile
{
//	private CvtrWindow parentWindow = null;
	public String homeDir = null;
	private CvtrData cvtrData = null;
	public String errorMessage = null;

	final private static int fileVersion = 1;
	final private static int flagNoInvert = 0x80;
	final private static int flagVersion = 0x60;	// Provides for versions 1 - 3
	final private static int versionShift = 5;
	final private static int flagType = 0x1C;		// Type of definitions in the file
	final private static int flagUnits = 0x4;		// Definitions of units
	final private static int flagConstants = 0x8;	// Definitions of constants
	final private static int flagEquations = 0xC;	// Definitions of equations
	final private static int flagUnicode = 0x2;		// If 0, text is UTF-8, else text is UTF-16
	final private static int flagInvert = 0x1;
/**
 * The CvtrFile constructor sets the parent class, which must provide the following
 * Dialog(JFrame parent, String title, String message) methods:
 * <ul>
 *   <li>infoDialog</li>
 *   <li>errorDialog</li>
 *   <li>yesnoDialog</li>
 * </ul>
 * 
 * @throws IllegalArgumentException	If the Convertator directory cannot be created
 * 									in the home sub-directory.
 */
	public CvtrFile() throws Exception {
		IllegalArgumentException exception;
		File home_dir;

		// Set home directory
		homeDir = System.getProperty("user.home") + File.separator + "Convertator";
		home_dir = new File(homeDir);
		if (!home_dir.exists()) {
			boolean newdir = (new File(homeDir)).mkdir();
			if (!newdir) {
				String errMsg = "The Convertator sub-directory could not be created in your home directory.";
				exception = new IllegalArgumentException(errMsg);
				throw exception;
			}
		}
		homeDir += File.separator;
		// Set data directories
		home_dir = new File(homeDir + "data");
		if (!home_dir.exists()) {
			boolean newdir = (new File(homeDir + "data")).mkdir();
			if (!newdir) {
				String errMsg = "The Convertator data directory could not be created in your home directory.";
				exception = new IllegalArgumentException(errMsg);
				throw exception;
			}
		}
		home_dir = new File(homeDir + "data" + File.separator + "Units");
		if (!home_dir.exists()) {
			boolean newdir = (new File(homeDir + "data" + File.separator + "Units")).mkdir();
			if (!newdir) {
				String errMsg = "The Convertator Units directory could not be created in your home directory.";
				exception = new IllegalArgumentException(errMsg);
				throw exception;
			}
		}
		home_dir = new File(homeDir + "data" + File.separator + "Constants");
		if (!home_dir.exists()) {
			boolean newdir = (new File(homeDir + "data" + File.separator + "Constants")).mkdir();
			if (!newdir) {
				String errMsg = "The Convertator Constants directory could not be created in your home directory.";
				exception = new IllegalArgumentException(errMsg);
				throw exception;
			}
		}
		home_dir = new File(homeDir + "data" + File.separator + "Equations");
		if (!home_dir.exists()) {
			boolean newdir = (new File(homeDir + "data" + File.separator + "Equations")).mkdir();
			if (!newdir) {
				String errMsg = "The Convertator Equations directory could not be created in your home directory.";
				exception = new IllegalArgumentException(errMsg);
				throw exception;
			}
		}

	}

/**
 * Display a message in a window with an OK button.
 * 
 * @param parent	The parent window.
 * @param title	The window title.
 * @param message	The message to be displayed.
 * 
 * @return int	The value of the selection.  See javax.swing.JOptionPane for the list
 * 				of selection values.
 */
	private int yesnoDialog(String title, String message) {
		return JOptionPane.showConfirmDialog(new JFrame(), message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	}

/**
 * Get the text from the selected file.
 * 
 * @param filename	The name of the file to read.
 * 
 * @return String	If there is an error, a message is saved in errorMessage and null is returned.
 * 					Otherwise the data from the file is returned.
 */
	public String getTextFile(String filename) {
		int bufLength;
		char[] fileBuffer = new char[8192];
		String fileData, textData = "";
		File tFile = new File(filename);
		FileReader fileReader;

		try {
			fileReader = new FileReader (tFile);
			while ((bufLength = fileReader.read(fileBuffer, 0, 8192)) != -1) {
				fileData = new String(fileBuffer);
				if (bufLength < 8192)
					textData += fileData.substring(0, bufLength);
				else
					textData += fileData;
			}
			fileReader.close();
		} catch (FileNotFoundException err) {
			errorMessage = "Error opening file " + filename + ":\n  " + err.getMessage();
			return null;
		} catch (IOException err) {
			errorMessage = "Error with file " + filename + ":\n  " + err.getMessage();
			return null;
		} catch (SecurityException err) {
			errorMessage = "Error with file " + filename + ":n  " + err.getMessage();
			return null;
		}

		return textData;
	} /* end getTextFile */

/**
 * Save text data to a file in the user's Convertator home directory.
 * 
 * @param textData	The data to be saved.
 * @param filename	The file where the data is to be saved.
 * 
 * @return boolean	True if successful.
 * 					Otherwise, a message is saved in errorMessage and false is returned.
 */
	public boolean saveTextFile(String textData, String filename) {
		byte[] fileBuffer;
		File dataFile = new File(homeDir + filename);
		FileOutputStream fileWriter;

		fileBuffer = textData.getBytes();
		try {
			fileWriter = new FileOutputStream(dataFile, false);
			fileWriter.write(fileBuffer, 0, fileBuffer.length);
 		} catch (FileNotFoundException err) {
			errorMessage = "Error opening file " + filename + ":\n  " + err.getMessage();
			return false;
		} catch (IOException err) {
			errorMessage = "Error with file " + filename + ":\n  " + err.getMessage();
			return false;
		} catch (SecurityException err) {
			errorMessage = "Error with file " + filename + ":n  " + err.getMessage();
			return false;
		}

		return true;
	} /* end saveTextFile */

/**
 * Delete a file.  The user's Convertator home directory, is checked and if not found,
 * then the application directory is checked.  If the file is found, an 'Are You Sure'
 * message is displayed if supplied.  If there is an error, the reason is saved in
 * errorMessage for the caller to display.
 * 
 * @param filename	The file where the data is to be saved.
 * @param aysMessage	If not null, an 'Are You Sure' message to be displayed.
 * 
 * @return boolean	True if successful.
 * 					Otherwise, a message is saved in errorMessage and false is returned.
 */
	public boolean deleteFile(String filename, String aysMessage) {
		String tFile = homeDir + filename;
		String aysMsg = aysMessage;
		File delFile = new File(tFile);

		// Verify file delete
		if (!delFile.exists()) {
			tFile = filename;
			delFile = new File(tFile);
			if (!delFile.exists()) {
				errorMessage = "File not found";
				return false;
			} else if (aysMessage != null) {
				aysMsg = "The file will be deleted from the application directory.\n" + aysMessage;
			}
		}
		if (aysMsg != null && yesnoDialog("DELETE", aysMsg) != JOptionPane.YES_OPTION) {
			errorMessage = "File not deleted.";
			return false;
		}

		try {
			// Find the file in the home Convertator directory
			if (delFile.exists()) {
				if (!delFile.delete()) {
					errorMessage = "File " + filename + " not deleted";
					return false;
				}
			} else {
				errorMessage = "File " + filename + " not found in Home directory";
				return false;
			}
		} catch (SecurityException err) {
			errorMessage = "Error with file " + filename + ":\n\n  " + err.getMessage();
			return false;
		}

		return true;
	} /* end deleteFile */

/**
 * Get all Convertator data files (extension of .cvd), starting in the home directory,
 * then in the application directory.  If files in the application directory duplicate
 * any from the home directory, a warning message is issued that they are ignored.
 * 
 * @param ce	The Convertator engine where the data is stored.
 * 
 * @return boolean	True if successful.  If errorMessage is not null, there are warnings.
 * 					Otherwise, a message is saved in errorMessage and false is returned.
 */
	public boolean getDataFiles(CvtrEngine ce) {
		int i, j;
		String curDir = System.getProperty("user.dir"), warnings = "", dType;
		String fType = "data" + File.separator + "Units";
		File dir;
		String[] children, allFiles = null;
		CvtrData cf;

		// Get data files from home directory
		// ~/Convertator/data/(Units | Constants | Equations)
		dir = new File(homeDir + fType);
		children = dir.list();
		while (fType.length() > 0) {
			if (children != null) {
				Arrays.sort(children);
				for (i=0; i < children.length; i++) {
					if (children[i].indexOf(".cvd") > 0 && children[i].indexOf(".cvd") == (children[i].length() - 4) &&
						(cf = getCvtrFile(homeDir + fType + File.separator + children[i])) != null) {
						dType = cf.getDataType();
						if (dType.equals("U")) {
							ce.UnitFiles.add(children[i]);
							ce.UnitCategories.add(cf.elementCategory);
							ce.UnitNames.add(cf.elementNames);
							ce.UnitValues.add(cf.elementValues);
						} else if (dType.equals("C")) {
							ce.ConstantFiles.add(children[i]);
							ce.ConstantCategories.add(cf.elementCategory);
							ce.ConstantNames.add(cf.elementNames);
							ce.ConstantValues.add(cf.elementValues);
						} else if (dType.equals("E")) {
							ce.EquationFiles.add(children[i]);
							ce.EquationCategories.add(cf.elementCategory);
							ce.EquationNames.add(cf.elementNames);
							ce.EquationValues.add(cf.elementValues);
						}
					} else {
						errorMessage = "Invalid file in home data directory";
						return false;
					}
				}
			}
			if (fType.equals("data" + File.separator + "Units")) {
				fType = "data" + File.separator + "Constants";
				dir = new File(homeDir + fType);
				children = dir.list();
			} else if (fType.equals("data" + File.separator + "Constants")) {
				fType = "data" + File.separator + "Equations";
				dir = new File(homeDir + fType);
				children = dir.list();
			} else
				fType = "";
		}

		// Get list of data files from home directory
		allFiles = new String[ce.UnitFiles.size() + ce.ConstantFiles.size() + ce.EquationFiles.size()];
		j = 0;
		for (i=0; i < allFiles.length; i++) {
			if (i < ce.UnitFiles.size()) {
				allFiles[i] = ce.UnitFiles.get(j++);
				if (j == ce.UnitFiles.size())
					j = 0;
			} else if (i < (ce.UnitFiles.size() + ce.ConstantFiles.size())) {
				allFiles[i] = ce.ConstantFiles.get(j++);
				if (j == ce.ConstantFiles.size())
					j = 0;
			} else {
				allFiles[i] = ce.EquationFiles.get(j++);
			}
		}

		// Get data files from application directory
		// ./Convertator/data/(Units | Constants | Equations)
		dir = new File(curDir + File.separator + "data");
		children = dir.list();
		if (children != null) {
			Arrays.sort(children);
			for (i=0; i < children.length; i++) {
				if (children[i].indexOf(".cvd") > 0 && children[i].indexOf(".cvd") == (children[i].length() - 4)) {
					// Do not load duplicate files
					for (j=0; j < allFiles.length; j++) {
						if (allFiles[j].equalsIgnoreCase(children[i]))
							break;
					}
					if (j < allFiles.length) {
						warnings += "  " + children[i];
					} else if ((cf = getCvtrFile("data" + File.separator + children[i])) != null) {
						if (cf.getDataType().equals("U")) {
							ce.UnitFiles.add(children[i]);
							ce.UnitCategories.add(cf.elementCategory);
							ce.UnitNames.add(cf.elementNames);
							ce.UnitValues.add(cf.elementValues);
						} else if (cf.getDataType().equals("C")) {
							ce.ConstantFiles.add(children[i]);
							ce.ConstantCategories.add(cf.elementCategory);
							ce.ConstantNames.add(cf.elementNames);
							ce.ConstantValues.add(cf.elementValues);
						} else if (cf.getDataType().equals("E")) {
							ce.EquationFiles.add(children[i]);
							ce.EquationCategories.add(cf.elementCategory);
							ce.EquationNames.add(cf.elementNames);
							ce.EquationValues.add(cf.elementValues);
						}
					} else {
						errorMessage = "Invalid file in application data directory";
						return false;
					}
				}
			}
		}

		// Display warning if duplicate files were found
		if (warnings.length() > 0) {
			errorMessage = "The following duplicate files were found in your home directory\n" +
					"and used instead of the equivalent application data files:\n\n" + warnings;
		} else
			errorMessage = null;

		return true;
	} /* end getDataFiles */

/**
 * Read a Convertator format file.  The format is:
 * <p/>
 * <ul>
 *   <li>Format flag: One octet of the form <i>nvvvttui</i> where:
 *	 <ul>
 *	   <li>n   = No invert bit (always 0)</li>
 *	   <li>vvv = File format version (1 - 7)</li>
 *	   <li>tt  = Type (1 = Units, 2 = Constants, 3 = Equations)</li>
 *	   <li>u   = Unicode strings flag (0 = ASCII, 1 = Unicode)</li>
 *	   <li>i   = Invert bit (always 1)</li>
 *	 </ul>
 *	 If the No invert bit is 1, then the file was saved on a platform with the
 *	 opposite Endian direction of the current one, and each octet in the file
 *	 must be reversed.
 *   </li>
 *   <li>Size: Four octets that contain two size values of the form <i>eeettttt</i> where:</li>
 *	 <ul>
 *	   <li>eee   = 12 bits indicating the number of elements in the file</li>
 *	   <li>ttttt = 20 bits indicating the total size of the file</li>
 *	 </ul>
 *   <li>Copyright: Two octets indicating size followed by that number of octets of text</li>
 *   <li>Category name: One octet size value followed by that number of octets of text</li>
 *   <li>Values:  The number of elements indicated in the Size field where each element is:</li>
 *	 <ul>
 *	   <li>Units: One octet size value followed by that number of octets of text, which is
 *		 in the format <i>name (abbreviation)</i>, followed the 8 octet conversion factor, where:
 *		 <ul>
 *		   <li>4 octets = integer portion</li>
 *		   <li>4 octets = fractional portion, which is divided by 2,000,000,000</li>
 *		 </ul>
 *	   </li>
 *	   <li>Constants: One octet size value followed by that number of octets of text, which is
 *		 in the constant name, followed the 8 octet constant value, where:
 *		 <ul>
 *		   <li>4 octets = integer portion</li>
 *		   <li>4 octets = fractional portion, which is divided by 2,000,000,000</li>
 *		 </ul>
 *	   </li>
 *	   <li>Equations: One octet size value followed by that number of octets of text, which is
 * 		 in the constant name.  The value is a one octet size value followed by that number of
 * 		 octets of text, which is the equation.  The equation may include text placeholders, but
 * 		 should be syntactically correct, estpecially regarding parentheses.</li>
 *	 </ul>
 * </ul>
 * <p/>
 * Note that all strings except copyright must have a length of less than 256 octets,
 * which is 128 characters in Unicode.  The copyright length is limited to 8192 characters
 * by the CvtrData class.
 * 
 * @param filename	The file to be read.
 * 
 * @return CvtrData	If successful, the Convertator Data structure that represents the file.
 * 					Otherwise, the reason is saved in errorMessage and null is returned.
 */
	public CvtrData getCvtrFile(String filename) {
		int i, j, k, idx, idxE;
		int  bufLength, flagFormat = 0, sizeFile, numElements;
		long l1, l2;
		byte[] fileBuffer = new byte[16384], stringBuf = new byte[258];
		String elementType = "Unknown";
		boolean isInvert = false;
		File cvtrFile = new File(filename);
		FileInputStream fileReader;

		cvtrData = new CvtrData();
		errorMessage = "";

		try {
			fileReader = new FileInputStream(cvtrFile);
			if ((bufLength = fileReader.read(fileBuffer)) < 32) {
				errorMessage = "Size of " + filename + " less than minimum required";
				return null;
			}
			// Get Format flag
			flagFormat |= fileBuffer[0];
			if ((flagFormat & (flagInvert | flagNoInvert)) == (flagInvert | flagNoInvert) ||
					(flagFormat & (flagInvert | flagNoInvert)) == 0) {
				errorMessage = "Invalid Covertator format in " + filename;
				return null;
			}
			if ((flagFormat & flagNoInvert) == flagNoInvert) {
				isInvert = true;
				flagFormat = dataInvert(fileBuffer[0]);
			}
			i = (flagFormat & flagVersion) >> versionShift;
			if (i > fileVersion) {
				errorMessage = "Version of " + filename + " not supported:  " + i;
				return null;
			}
			if ((flagFormat & flagType) == flagUnits) {
				cvtrData.setDataType("U");
				elementType = "Unit";
			} else if ((flagFormat & flagType) == flagConstants) {
				cvtrData.setDataType("C");
				elementType = "Constant";
			} else if ((flagFormat & flagType) == flagEquations) {
				cvtrData.setDataType("E");
				elementType = "Equation";
			} else {
				errorMessage = "Invalid data type in " + filename;
				return null;
			}
			if ((flagFormat & flagUnicode) == flagUnicode)
				cvtrData.isUnicode = true;
				
			// Get total size and number of elements
			i = 0;
			for (idx=1; idx < 5; idx++) {
				if (isInvert) {
					i <<= 8;
					i |= dataInvert(fileBuffer[idx]);
				} else {
					i <<= 8;
					i |= fileBuffer[idx] & 0xff;
				}
			}
			sizeFile = i & 0xfffff;
			sizeFile -= bufLength;
			if (sizeFile < 0) {
				errorMessage = "Invalid file size field (init)";
				return null;
			}
			numElements = i >> 20;

			// Get cvtrData.dataCopyright
			i = 0;
			for (j=5; j < 7; j++) {
				if (isInvert) {
					i <<= 8;
					i |= dataInvert(fileBuffer[j]);
				} else {
					i <<= 8;
					i |= (int) fileBuffer[j] & 0xff;
				}
			}
			idx = 7;
			if ((idx + i) < bufLength) {
// TODO: Test Unicode: Create String and test for maximum size < 8192
				if (isInvert) {
					k = 0;
					for (j=idx; j < (idx + i); j++) {
						stringBuf[k] = 0;
						stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
					}
					cvtrData.setCopyright(new String(stringBuf, 0, k));
				} else {
					cvtrData.setCopyright(new String(fileBuffer, 7, i));
				}
				idx += i;
			} else {
				errorMessage = "Copyright length too long";
				return null;
			}

			// Get category
			i = 0;
			if (isInvert) {
				i |= dataInvert(fileBuffer[idx]);
			} else {
				i |= (int) fileBuffer[idx] & 0xff;
			}
			idx++;
			if ((idx + i) < bufLength) {
// TODO: Test Unicode
				if (isInvert) {
					k = 0;
					for (j=idx; j < (idx + i); j++) {
						stringBuf[k] = 0;
						stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
					}
					cvtrData.elementCategory = new String(stringBuf, 0, k);
				} else {
					cvtrData.elementCategory = new String(fileBuffer, idx, i);
				}
				idx += i;
			} else {
				k = 0;
				for (j=idx; j < bufLength; j++) {
					if (isInvert) {
						stringBuf[k] = 0;
						stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
					} else {
						stringBuf[k++] = fileBuffer[j];
					}
				}
				if ((bufLength = fileReader.read(fileBuffer)) < (i - k)) {
					errorMessage = "Category truncated in " + filename;
					return null;
				}
				sizeFile -= bufLength;
				if (sizeFile < 0) {
					errorMessage = "Invalid file size field (category)";
					return null;
				}
				i -= k;
				if (i > bufLength) {
					errorMessage = "Category truncated in " + filename;
					return null;
				}
				for (j=0; j < i; j++) {
// TODO: Test Unicode
					if (isInvert) {
						stringBuf[k] = 0;
						stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
					} else {
						stringBuf[k++] = fileBuffer[j];
					}
					cvtrData.elementCategory = new String(stringBuf, 0, k);
				}
				idx = i;
			}

			// Get elements
			cvtrData.elementNames = new String[numElements];
			cvtrData.elementValues = new String[numElements];
			idxE = 0;
			while (idxE < numElements) {
				if (idx >= bufLength) {
					if ((bufLength = fileReader.read(fileBuffer)) <= 0) {
						errorMessage = "Invalid number of " + elementType + "s in " + filename;
						return null;
					}
					idx = 0;
					sizeFile -= bufLength;
					if (sizeFile < 0) {
						errorMessage = "Invalid file size field (elements)";
						return null;
					}
				}
				// Get Name of Element
				i = 0;
				if (isInvert) {
					i |= dataInvert(fileBuffer[idx]);
				} else {
					i |= (int) fileBuffer[idx] & 0xff;
				}
				idx++;
				if ((idx + i) < bufLength) {
// TODO: Test Unicode
					if (isInvert) {
						k = 0;
						for (j=idx; j < (idx + i); j++) {
							stringBuf[k] = 0;
							stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
						}
						cvtrData.elementNames[idxE] = new String(stringBuf, 0, k);
					} else {
						cvtrData.elementNames[idxE] = new String(fileBuffer, idx, i);
					}
					idx += i;
				} else {
					k = 0;
					for (j=idx; j < bufLength; j++) {
						if (isInvert) {
							stringBuf[k] = 0;
							stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
						} else {
							stringBuf[k++] = fileBuffer[j];
						}
					}
					if ((bufLength = fileReader.read(fileBuffer)) < (i - k)) {
						errorMessage = elementType + " truncated in " + filename;
						return null;
					}
					sizeFile -= bufLength;
					if (sizeFile < 0) {
						errorMessage = "Invalid file size field (names)";
						return null;
					}
					i -= k;
					if (i > bufLength) {
						errorMessage = elementType + " truncated in " + filename;
						return null;
					}
					for (j=0; j < i; j++) {
// TODO: Test Unicode
						if (isInvert) {
							stringBuf[k] = 0;
							stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
						} else {
							stringBuf[k++] = fileBuffer[j];
						}
					}
					cvtrData.elementNames[idxE] = new String(stringBuf, 0, k);
					idx = i;
				}
				// Get unit or constant floating point value
				if (cvtrData.getDataType().equals("U")) {
					l1 = 0;
					l2 = 0;
					for (i=0; i < 8; i++) {
						if (idx >= bufLength) {
							if  ((bufLength = fileReader.read(fileBuffer)) <= 0) {
								errorMessage = "Invalid number of " + elementType + "s in " + filename;
								return null;
							}
							idx = 0;
							sizeFile -= bufLength;
							if (sizeFile < 0) {
								errorMessage = "Invalid file size field (double value)";
								return null;
							}
						}
						if (isInvert) {
							if (i < 4) {
								l1 <<= 8;
								l1 |= dataInvert(fileBuffer[idx++]);
							} else {
								l2 <<= 8;
								l2 |= dataInvert(fileBuffer[idx++]);
							}
						} else {
							if (i < 4) {
								l1 <<= 8;
								l1 |= (int) fileBuffer[idx++] & 0xff;
							} else {
								l2 <<= 8;
								l2 |= (int) fileBuffer[idx++] & 0xff;
							}
						}
					}
					NumberFormat nf = NumberFormat.getInstance();
					DecimalFormat df = (DecimalFormat) nf;
					df.applyPattern("#0.0##########");
					Double d = (double)l1 + ((double) l2 / 0xffffffffL);
					cvtrData.elementValues[idxE] = df.format(d);
				// Get equation string
				} else {
					i = 0;
					if (isInvert) {
						i |= dataInvert(fileBuffer[idx]);
					} else {
						i |= (int) fileBuffer[idx] & 0xff;
					}
					idx++;
					if ((idx + i) <= bufLength) {
// TODO: Test Unicode
						if (isInvert) {
							k = 0;
							for (j=idx; j < (idx + i); j++) {
								stringBuf[k] = 0;
								stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
							}
							cvtrData.elementValues[idxE] = new String(stringBuf, 0, k);
						} else {
							cvtrData.elementValues[idxE] = new String(fileBuffer, idx, i);
						}
						idx += i;
					} else {
						k = 0;
						for (j=idx; j < bufLength; j++) {
							if (isInvert) {
								stringBuf[k] = 0;
								stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
							} else {
								stringBuf[k++] = fileBuffer[j];
							}
						}
						if ((bufLength = fileReader.read(fileBuffer)) < (i - k)) {
							errorMessage = elementType + " truncated in " + filename;
							return null;
						}
						sizeFile -= bufLength;
						if (sizeFile < 0) {
							errorMessage = "Invalid file size field (string value)";
							return null;
						}
						i -= k;
						if (i > bufLength) {
							errorMessage = elementType + " truncated in " + filename;
							return null;
						}
						for (j=0; j < i; j++) {
// TODO: Test Unicode
							if (isInvert) {
								stringBuf[k] = 0;
								stringBuf[k++] |= (byte) dataInvert(fileBuffer[j]);
							} else {
								stringBuf[k++] = fileBuffer[j];
							}
						}
						cvtrData.elementValues[idxE] = new String(stringBuf, 0, k);
						idx = i;
					}
				}
				idxE++;
			}
			
			// Finished
			fileReader.close();
		} catch (IOException err) {
			errorMessage = "Error with file " + filename + ":\n\n  " + err.getMessage();
			return null;
		}
		return cvtrData;
	} /* end getCvtrFile */

/**
 * Invert the bits of a single octet.
 * 
 * @param octet	The data to be inverted.
 * 
 * @return int	The inverted data.
 */
	private int dataInvert(byte octet) {
		int i = 0, j, result = 0;

		i |= (int) octet & 0xff;
		for (j=0; j < 8; j++) {
			result <<= 1;
			result |= i & 1;
			i >>= 1;
		}

		return result;
	} /* end dataInvert */

/**
 * Save data to a Convertator format file.  See getCvtrFile for the description of
 * the file format.  The fractional portion of Unit or Constant values is
 * multiplied by 2,000,000,000 before saving.
 * 
 * @param cData	The data to be saved.
 * @param filename	The filename to use.
 * 
 * @return boolean	True if the file is saved successfully.  Otherwise
 * 					the reason is saved in errorMessage and false is returned.
 */
	public boolean saveCvtrFile(CvtrData cData, String filename) {
		int i, j, k, idxE;
		int  bufLength, flagFormat, sizeField, sizeFile;
		long l;
		String elementType = "Unknown", temp;
		byte[] fileBuffer, textBuffer;
		File cvtrFile = new File(filename);
		FileOutputStream fileWriter;

		errorMessage = "";

		// Validate cData
		if ((temp = cData.validateData()) != null) {
			errorMessage = "The following errors were found in the data for " + filename + ":\n\n  " + temp;
			return false;
		}

		if (cvtrFile.exists()) {
			i = yesnoDialog("WARNING", "The file " + filename + " exists.  Overwrite it?");
			if (i == JOptionPane.NO_OPTION) {
				errorMessage = "File not saved.";
				return false;
			}
		}

		// Initialize size field and get estimated buffer size
		if ((sizeField = cData.elementNames.length) > 0xfff) {
			errorMessage = "Too many entries in the data for " + filename;
			return false;
		}
		bufLength = cData.elementCategory.length() + cData.getCopyright().length();
		if (cData.isUnicode) {
			bufLength <<= 1;
		}
		bufLength += 8;
		i = 0;
		for (j=0; j < sizeField; j++)
			i += cData.elementNames[j].length();
		if (cData.getDataType().equals("U")) {
			j = sizeField * 9;
		} else {
			for (j=0; j < sizeField; j++)
				i += cData.elementValues[j].length();
			j = 0;
		}
		// Account for double byte Unicode, and add fudge factor
		if (cData.isUnicode) {
			i *= 5;
			i >>= 1;
		} else {
			i *= 3;
			i >>= 1;
		}
		bufLength += i + j;
		fileBuffer = new byte[bufLength];
		sizeFile = 7;

		// Set the Format flag
		flagFormat = (fileVersion << versionShift) | flagInvert;
		if (cData.getDataType().equals("U")) {
			flagFormat |= flagUnits;
			elementType = "Unit";
		} else if (cData.getDataType().equals("C")) {
			flagFormat |= flagConstants;
			elementType = "Constant";
		} else if (cData.getDataType().equals("E")) {
			flagFormat |= flagEquations;
			elementType = "Equation";
		} else {
			errorMessage = "Invalid data type in file " + filename;
			return false;
		}
		if (cData.isUnicode)
			flagFormat |= flagUnicode;
		fileBuffer[0] = 0; fileBuffer[0] |= (byte) flagFormat;

		// Add Copyright to buffer
		textBuffer = cData.getCopyright().getBytes();
		if ((i = textBuffer.length) > 16384) {
			errorMessage = "Copyright is too large for " + filename;
			return false;
		}
		sizeFile += i;
		if (sizeFile > bufLength) {
			errorMessage = "Copyright is too large for " + filename;
			return false;
		}
		fileBuffer[6] = 0; fileBuffer[6] |= (byte) i;
		i >>= 8;
		fileBuffer[5] = 0; fileBuffer[5] |= (byte) i;
		k = 0;
		for (j=7; j < sizeFile; j++)
			fileBuffer[j] = textBuffer[k++];

		// Add Category name to buffer
		textBuffer = cData.elementCategory.getBytes();
		if ((i = textBuffer.length) > 256) {
			errorMessage = "Category is too long for " + filename;
			return false;
		}
		j = sizeFile;
		sizeFile += i + 1;
		if (sizeFile > bufLength) {
			errorMessage = "Category is too long for " + filename;
			return false;
		}
		fileBuffer[j] = 0; fileBuffer[j++] |= (byte) i;
		k = 0;
		for (; j < sizeFile; j++)
			fileBuffer[j] = textBuffer[k++];

		// Add data
		idxE = 0;
		while (idxE < sizeField) {
			// Add data name
			textBuffer = cData.elementNames[idxE].getBytes();
			if ((i = textBuffer.length) > 256) {
				errorMessage = elementType + " name is too long for " + filename;
				return false;
			}
			j = sizeFile;
			sizeFile += i + 1;
			if (sizeFile > bufLength) {
				errorMessage = elementType + " name is too long for " + filename;
				return false;
			}
			fileBuffer[j] = 0; fileBuffer[j++] |= (byte) i;
			k = 0;
			for (; j < sizeFile; j++)
				fileBuffer[j] = textBuffer[k++];
			// Add unit or constant value
			if (cData.getDataType().equals("U")) {
				sizeFile += 8;
				double d = Double.parseDouble(cData.elementValues[idxE]);
				l = (long) d;
				if (l > 0xffffffffL) {
					errorMessage = elementType + " integer value " + l + " is too large";
					return false;
				}
				double dInt = l;
				// Store integer portion of floating point value
				fileBuffer[j + 3] = 0; fileBuffer[j + 3] |= (byte) l;
				l >>= 8;
				fileBuffer[j + 2] = 0; fileBuffer[j + 2] |= (byte) l;
				l >>= 8;
				fileBuffer[j + 1] = 0; fileBuffer[j + 1] |= (byte) l;
				l >>= 8;
				fileBuffer[j] = 0; fileBuffer[j] |= (byte) l;
				j += 4;
				// Store decimal portion of floating point value
				l = (long) ((d - dInt) * 0xffffffffL);
				fileBuffer[j + 3] = 0; fileBuffer[j + 3] |= (byte) l;
				l >>= 8;
				fileBuffer[j + 2] = 0; fileBuffer[j + 2] |= (byte) l;
				l >>= 8;
				fileBuffer[j + 1] = 0; fileBuffer[j + 1] |= (byte) l;
				l >>= 8;
				fileBuffer[j] = 0; fileBuffer[j] |= (byte) l;
				j += 4;
			// Add equation
			} else {
				textBuffer = cData.elementValues[idxE].getBytes();
				if ((i = textBuffer.length) > 256) {
					errorMessage = elementType + " value is too long for " + filename;
					return false;
				}
				j = sizeFile;
				sizeFile += i + 1;
				if (sizeFile > bufLength) {
					errorMessage = elementType + " value is too long for " + filename;
					return false;
				}
				fileBuffer[j] = 0; fileBuffer[j++] |= (byte) i;
				k = 0;
				for (; j < sizeFile; j++)
					fileBuffer[j] = textBuffer[k++];
			}

			idxE++;
		}

		// Insert file size at beginning of buffer
		if (sizeFile < 0 || sizeFile > 0xfffff) {
			errorMessage = "Data length exceeds maximum for " + filename;
			return false;
		}
		sizeField <<= 20;
		sizeField |= sizeFile;
		fileBuffer[4] = 0; fileBuffer[4] |= (byte) sizeField & 0xff;
		sizeField >>= 8;
		fileBuffer[3] = 0; fileBuffer[3] |= (byte) sizeField & 0xff;
		sizeField >>= 8;
		fileBuffer[2] = 0; fileBuffer[2] |= (byte) sizeField & 0xff;
		sizeField >>= 8;
		fileBuffer[1] = 0; fileBuffer[1] |= (byte) sizeField & 0xff;

		// Write data to Convertator file
		try {
			fileWriter = new FileOutputStream(cvtrFile, false);
			fileWriter.write(fileBuffer, 0, sizeFile);
		} catch (FileNotFoundException err) {
			errorMessage = "Error opening file " + filename + ":\n\n  " + err.getMessage();
			return false;
		} catch (IOException err) {
			errorMessage = "Error with file " + filename + ":\n\n  " + err.getMessage();
			return false;
		} catch (SecurityException err) {
			errorMessage = "Error with file " + filename + ":\n\n  " + err.getMessage();
			return false;
		}

		return true;
	} /* end saveCvtrFile */

}
