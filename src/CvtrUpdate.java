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

import java.util.ArrayList;

/**
 * The Convertator Update class maintains information about Convertator updates used
 * for downloads from a website.  The website must maintain the following information:
 * <ul>
 *   <li>Site update file: The file, site.cvu, must contain the site update information
 *     (see parseSiteUpdate for its format)</li>
 *   <li>Site data file: The file, data.cvu, must contain the locations of Convertator
 *     data files (see parseSiteData for its format)</li>
 *   <li></li>
 * </ul> 
 */
public class CvtrUpdate
{
	private String primaryLanguage;
	private String secondaryLanguage;
	public String errorMessage = null;
	public ArrayList<String> SiteNames = new ArrayList<String>();
	public ArrayList<String> SiteDomains = new ArrayList<String>();
	public ArrayList<String> SitePaths = new ArrayList<String>();
	public ArrayList<String> SiteDates = new ArrayList<String>();
	public ArrayList<ArrayList<String>> UnitGroups = new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<ArrayList<String>>> Units = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<ArrayList<String>>> UnitPaths = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<ArrayList<String>>> UnitDates = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<String>> ConstantGroups = new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<ArrayList<String>>> Constants = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<ArrayList<String>>> ConstantPaths = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<ArrayList<String>>> ConstantDates = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<String>> EquationGroups = new ArrayList<ArrayList<String>>();
	public ArrayList<ArrayList<ArrayList<String>>> Equations = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<ArrayList<String>>> EquationPaths = new ArrayList<ArrayList<ArrayList<String>>>();
	public ArrayList<ArrayList<ArrayList<String>>> EquationDates = new ArrayList<ArrayList<ArrayList<String>>>();

 /**
  * The Convertator Unit constructor sets the primary and secondary languages.
  * 
  *  @param langPrimary	The primary language abbreviation.
  *  @param langSecondary	The seconary language abbreviation.
  */
	public CvtrUpdate(String langPrimary, String langSecondary) {

		primaryLanguage = langPrimary;
		secondaryLanguage = langSecondary;

	}

/**
 * Parse one or more lines of site update information, which is contain in the site.cvu.
 * As the data is parsed, the arrays containing the site data are checked for the site,
 * and if it exists, the information is updated.  Otherwise, the site is added.
 * 
 * Each line of site data must be in the format:
 * <p/>
 * <ul>Name;Domain;Path;Last update <i>where</i></ul>
 * <ul>
 *   <li>Name: The name displayed to users</li>
 *   <li>Domain: The web site where the updates are located, in the form of 'http://www.abc.com/'</li>
 *   <li>Path: The path to the updates.  The files, site.cvu and data.cvu, must be found by
 *     concatenating the Domain and the Path.</li>
 *   <li>Last update: The date and time of the last update to any Convertator data.  The format is
 *     undefined, but must be consistent for the every update, because it is simply compared against
 *     the date from the last update, and if different, the data is assumed to have been updated</li>
 * </ul>
 * 
 * @param siteData	The string that contains the site data to be parsed.
 * @param errorExit	If true, exit on any error and return false.
 * 					Otherwise, complete parsing ignoring errors and return true.
 * 
 * @return boolean	False if an error was found and errorExit is true, otherwise true.
 */
	public boolean parseSiteUpdate(String siteData, boolean errorExit) {
		int i, j, idx = 0;
		String psName, psDomain, psPath, psDate;

		while (idx < siteData.length()) {
			psName = psDomain = psPath = psDate = null;
			// Get Name field
			i = siteData.indexOf(";", idx);
			if (idx < i && i < siteData.length()) {
				psName = siteData.substring(idx, i);
				if ((idx = i + 1) >= siteData.length()) {
					if (errorExit)
						return false;
					else
						break;
				}
			}
			// Get Domain field
			i = siteData.indexOf(";", idx);
			if (idx < i && i < siteData.length()) {
				psDomain = siteData.substring(idx, i);
				if ((idx = i + 1) >= siteData.length()) {
					if (errorExit)
						return false;
					else
						break;
				}
				i = psDomain.length();
				if (!psDomain.substring(i - 1, i).equals("/"))
					psDomain += "/";
			}
			// Get Path field
			i = siteData.indexOf(";", idx);
			if (idx < i && i < siteData.length()) {
				psPath = siteData.substring(idx, i);
				if ((idx = i + 1) >= siteData.length()) {
					if (errorExit)
						return false;
					else
						break;
				}
			}
			// Get end of line, which might be CR, LF, or both
			j = siteData.length();
			i = siteData.indexOf("\n", idx);
			if (idx < i && i < j)
				j = i;
			i = siteData.indexOf("\r", idx);
			if (idx < i && i < j)
				j = i;
			// Get Date field
			i = j;
			if (idx < i && i <= siteData.length()) {
				psDate = siteData.substring(idx, i);
				idx = i;
				while (idx < siteData.length() && (siteData.substring(idx, idx + 1).equals("\n") ||
					siteData.substring(idx, idx + 1).equals("\r")))
					idx++;
			}
			if (psName != null && psDate != null) {
				for (i=0; i < SiteNames.size(); i++) {
					if (SiteNames.get(i).equalsIgnoreCase(psName))
						break;
				}
				if (i == SiteNames.size()) {
					SiteNames.add(psName);
					SiteDomains.add(psDomain);
					SitePaths.add(psPath);
					SiteDates.add(psDate);
				} else {
					SiteNames.remove(i);
					SiteNames.add(i, psName);
					SiteDomains.remove(i);
					SiteDomains.add(i, psDomain);
					SitePaths.remove(i);
					SitePaths.add(i, psPath);
					SiteDates.remove(i);
					SiteDates.add(i, psDate);
				}
			} else if (errorExit) {
				return false;
			}
		}

		return true;
	}

/**
 * Parse a data.cvu file for the site Convertator data files.  As the data is parsed,
 * the arrays containing the file data are checked.  If it exists and the date is
 * different, the information is marked as new.  Otherwise, the file data is added.
 * 
 * Each line of file data must be in the format:
 * <p/>
 * <ul>Path;Last update <i>where</i></ul>
 * <ul>
 *   <li>Path: The path to the data file.  The file, named 'filename.cvd', must be found by
 *     concatenating the Path to the web location for the site, and must be in the format:
 *     <ul>
 *       <li>/language/type/[group]/filename.cvd <i>where</i></li>
 *       <li>language: A 2 letter abbreviation, such as en (English), fr (French), de (German),
 *         or jp (Japanese).</li>
 *       <li>type: One of 'units', 'constants', or 'equations', in lowercase.</li>
 *       <li>group: If the file contains constants or equations, there should be a group, such as
 *         Astronomy for constants, to identify it.</li>
 *       <li>filename: The descriptive name of the Convertator data, such as Time for units, or
 *         Capacity for equations.  The first letter should be in upper case, because the name
 *         is displayed as-is in popup menus.</li>
 *     </ul>
 *     </li>
 *   <li>Last update: The date and time of the last update to any Convertator data file.  The
 *     format is undefined, but must be consistent for the every update, because it is simply
 *     compared against the date from the last update, and if different, the file is assumed
 *     to have been updated</li>
 * </ul>
 * 
 * @param siteIndex	The index of the site to which the data belongs.
 * @param siteData	The string that contains the site data to be parsed.
 * @param errorExit	If true, exit on any error and return false.
 * 					Otherwise, complete parsing ignoring errors and return true.
 * 
 * @return boolean	False if an error was found and errorExit is true, otherwise true.
 */
	public boolean parseSiteData(int siteIndex, String siteData, boolean errorExit) {
		int i, j, idx = 0, line = 0;
		String psPath, psDate;
		String psType, psGroup, psName;

		while (idx < siteData.length()) {
			line++;
			psPath = psDate = null;
			// Get Path field
			i = siteData.indexOf(";", idx);
			if (idx < i && i < siteData.length()) {
				psPath = siteData.substring(idx, i);
				if ((idx = i + 1) >= siteData.length()) {
					if (errorExit) {
						errorMessage = "Could not read date for:\n  " + psPath;
						return false;
					} else
						break;
				}
			}
			// Get end of line, which might be CR, LF, or both
			j = siteData.length();
			i = siteData.indexOf("\n", idx);
			if (idx < i && i < j)
				j = i;
			i = siteData.indexOf("\r", idx);
			if (idx < i && i < j)
				j = i;
			// Get Date field
			i = j;
			if (idx < i && i < siteData.length()) {
				psDate = siteData.substring(idx, i);
				idx = i;
				while (idx < siteData.length() && (siteData.substring(idx, idx + 1).equals("\n") ||
					siteData.substring(idx, idx + 1).equals("\r")))
					idx++;
			}
			 // Parse path: /language/type/[group]/filename.cvd
			if (psPath != null && psDate != null) {
				// Only display data for selected languages
//				if ((i = psPath.indexOf("/")) < 0 || (!psPath.substring(0, i).equalsIgnoreCase(primaryLanguage) &&
//						!psPath.substring(0, i).equalsIgnoreCase(secondaryLanguage)))
//					continue;
				if ((i = psPath.indexOf("/")) == 0)
					i++;
				else
					i = 0;
				if ((j = psPath.indexOf("/", i)) < 0)
					continue;
				psType = psPath.substring(i, j);
				i = j + 1;
				// Get data group
				if ((j = psPath.indexOf("/", i)) < 0)
					continue;
				psGroup = psPath.substring(i, j);
				i = j + 1;
				// Use filename in popup menu
				if ((j = psPath.indexOf(".cvd", i)) < 0)
					continue;
				psName = psPath.substring(i, j);
				// Set values
				if (psType.equalsIgnoreCase("units")) {
					while (Units.size() < (siteIndex + 1)) {
						Units.add(new ArrayList<ArrayList<String>>());
						UnitGroups.add(new ArrayList<String>());
						UnitPaths.add(new ArrayList<ArrayList<String>>());
						UnitDates.add(new ArrayList<ArrayList<String>>());
					}
					// Check for existing unitt group
					for (i=0; i < UnitGroups.get(siteIndex).size(); i++) {
						if (UnitGroups.get(siteIndex).get(i).equals(psGroup)) {
							break;
						}
					}
					// Add new unit group
					if (i == UnitGroups.get(siteIndex).size()) {
						UnitGroups.get(siteIndex).add(psGroup);
						Units.get(siteIndex).add(new ArrayList<String>());
						UnitPaths.get(siteIndex).add(new ArrayList<String>());
						UnitDates.get(siteIndex).add(new ArrayList<String>());
					}
					Units.get(siteIndex).get(i).add(psName);
					UnitPaths.get(siteIndex).get(i).add(psPath);
					UnitDates.get(siteIndex).get(i).add(psDate);
				} else if (psType.equalsIgnoreCase("constants")) {
					while (Constants.size() < (siteIndex + 1)) {
						Constants.add(new ArrayList<ArrayList<String>>());
						ConstantGroups.add(new ArrayList<String>());
						ConstantPaths.add(new ArrayList<ArrayList<String>>());
						ConstantDates.add(new ArrayList<ArrayList<String>>());
					}
					// Check for existing constant group
					for (i=0; i < ConstantGroups.get(siteIndex).size(); i++) {
						if (ConstantGroups.get(siteIndex).get(i).equals(psGroup)) {
							break;
						}
					}
					// Add new constant group
					if (i == ConstantGroups.get(siteIndex).size()) {
						ConstantGroups.get(siteIndex).add(psGroup);
						Constants.get(siteIndex).add(new ArrayList<String>());
						ConstantPaths.get(siteIndex).add(new ArrayList<String>());
						ConstantDates.get(siteIndex).add(new ArrayList<String>());
					}
					Constants.get(siteIndex).get(i).add(psName);
					ConstantPaths.get(siteIndex).get(i).add(psPath);
					ConstantDates.get(siteIndex).get(i).add(psDate);
				} else if (psType.equalsIgnoreCase("equations")) {
					while (Equations.size() < (siteIndex + 1)) {
						Equations.add(new ArrayList<ArrayList<String>>());
						EquationGroups.add(new ArrayList<String>());
						EquationPaths.add(new ArrayList<ArrayList<String>>());
						EquationDates.add(new ArrayList<ArrayList<String>>());
					}
					// Check for existing equation group
					for (i=0; i < EquationGroups.get(siteIndex).size(); i++) {
						if (EquationGroups.get(siteIndex).get(i).equals(psGroup)) {
							break;
						}
					}
					// Add new constant group
					if (i == EquationGroups.get(siteIndex).size()) {
						EquationGroups.get(siteIndex).add(psGroup);
						Equations.get(siteIndex).add(new ArrayList<String>());
						EquationPaths.get(siteIndex).add(new ArrayList<String>());
						EquationDates.get(siteIndex).add(new ArrayList<String>());
					}
					Equations.get(siteIndex).get(i).add(psName);
					EquationPaths.get(siteIndex).get(i).add(psPath);
					EquationDates.get(siteIndex).get(i).add(psDate);
				} else if (errorExit) {
					errorMessage = "Invalid data type: " + psType;
					return false;
				}
			} else if (errorExit) {
				errorMessage = "Error in download information at line " + line;
				return false;
			} 
		}

		return true;
	}

}
