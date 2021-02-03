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
 * The Convertator Group class parses an equation stack to separate it into
 * groups of operations which are enclosed in parentheses.  These may then be
 * reduced to the single values if possible.
 */
public class CvtrGroup
{
	static final boolean verbose = false;
//	 The maximum depth of nested parentheses (or a very large number)
	static final int maxNesting = 0x100000;

// The list of nested operation groups
	public ArrayList <ArrayList<CvtrOperand>> groupList = null;

// Global values for calculations
	private ArrayList <CvtrOperand> groupStack;
	private int nLevel;

	public String calcError = null;
	public String calcProgress = null;
	
/**
 * The Convertator Group constructor sets the parent class, which must provide the
 * following Dialog(JFrame parent, String title, String message) methods:
 * <ul>
 *   <li>infoDialog</li>
 *   <li>errorDialog</li>
 *   <li>yesnoDialog</li>
 * </ul>
 * <p/>
 * The Convertator File class must be created before the Group, because it is
 * called to initialize the Group data.
 */
	public CvtrGroup(ArrayList<CvtrOperand> newStack, int level) {

		groupStack = newStack;
		nLevel = level;
		if (groupStack != null)
			initGroup();
	}

/**
 * Collect groups of operands into group stacks.  This method is called recursively
 * until all groups in the stack have been reduced to their simplest form.  The
 * types of groups are:
 * <ul>
 *   <li>Operands with different unit categories, such as distance (miles) and time (hour)</li>
 *   <li>Operands with the same unit categories but to different powers, such as seconds and seconds ^ 2</li>
 * </ul>
 */
	private void initGroup() {
		int i, idx;
		int gLevel, sLevel;

		for (idx=0; idx < groupStack.size(); idx++) {
			if (groupStack.get(idx).nestLevel >= nLevel) {
				i = 1;
				break;
			}
		}
		// Group cannot be reduced
		if (idx == groupStack.size())
			return;
		groupList = new ArrayList <ArrayList<CvtrOperand>>();

if (verbose) {
System.out.println("Get groups: " + " (" + groupStack.size() + "): " + nLevel);
for (i=0; i < groupStack.size(); i++) {
	System.out.print("  Get groups Operand " + i + ": " + groupStack.get(i).value + groupStack.get(i).unit + "^" +
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
			return;
		} else if (groupStack.size() == 1) {
			return;
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
				gLevel = maxNesting;
				if (groupList.get(idx).size() > 0) {
					groupList.add(new ArrayList<CvtrOperand>());
					idx++;
if (verbose)
System.out.println("  Add group list(" + idx + ")null: " + groupList.size());
				}
			// i.nestLevel < nLevel && (i.nestLevel != i+1.nestLevel || i.nestGroup != i+1.nestGroup)
			} else if (sLevel >= 0) {
				if (groupStack.get(0).nestLevel != sLevel || groupStack.get(0).nestGroup[sLevel] != gLevel) {
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
					groupList.add(new ArrayList<CvtrOperand>());
					idx++;
if (verbose)
System.out.println("  Add group list(" + idx + ")NL: " + groupList.size());
				}
			// i.nestGroup != i+1.nestGroup
			} else if (gLevel != groupStack.get(0).nestGroup[nLevel]) {
				gLevel = groupStack.get(0).nestGroup[nLevel];
				if (groupList.get(idx).size() > 0) {
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

		return;
	} /* end reduceGroups */

}
