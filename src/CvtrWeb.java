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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/* import sun.net.www.protocol.file.FileURLConnection;*/

/**
 * The Convertator Web class downloads files from a web address.
 */
public class CvtrWeb
{
	static final boolean verbose = false;
	// Home directory
	private String homeDir = null;
	// HTTP data types
	private final static String dataTypeText = "text/plain";
	private final static String dataTypeBinary = "application/octet-stream";
	// Error information
	public String errorMessage = null;

/**
 * The CvtrWeb constructor initializes the class.
 * 
 * @param homeDirectory	The home directory path for storing downloaded data
 */
	public CvtrWeb(String homeDirectory) {
		homeDir = homeDirectory;
	}

/**
 * Download a file from the supplied address.  The data is saved in the
 * supplied filename.  The local file is first opened in the user's home
 * directory.  If that fails, it is opened in the application directory.
 * 
 * @param address	The HTTP URL of the file to be downloaded.
 * @param filename	The local name of the file to be saved.
 * @param datatype	The type of data to be downloaded, where the supported types are:
 * 		<ul>
 * 		  <li>text</li>
 * 		  <li>binary</li>
 * 		<ul>
 * 
 * @return String	If successful, the name of the file.
 * 					Otherwise, the reason is save in errorMessage and null is returned.
 */
	public String downloadFile(String address, String filename, String dataType) {
		int len;
		String acceptProperty, filePath = homeDir + filename;
		URL dlURL;
		HttpURLConnection dlConnect = null;
		FileURLConnection dlFileConn = null;
		boolean addrNet = true;
		File dlFile = null;
		InputStream dlData = null;
		FileOutputStream fileData = null;
		byte[] streamData = new byte[4096];

		// Validate the address
		try {
			dlURL = new URL(address);
			if (address.indexOf("file://") == 0 && address.substring(7, 8).indexOf("/") != 0) {
				errorMessage = "Error in URL:\n  " + address;
				return null;
			}
		} catch (MalformedURLException err) {
			errorMessage = "Error in URL:\n  " + address;
			return null;
		}

		// Create the temporary file in the home directory
		try {
			dlFile = new File(filePath);
			fileData = new FileOutputStream(dlFile);
		} catch (IOException err) {
			dlFile = null;
		}
		if (dlFile == null) {
			// Create the temporary file in the application directory
			filePath = filename;
			try {
				dlFile = new File(filePath);
				fileData = new FileOutputStream(dlFile);
			} catch (IOException err) {
				if (dlFile != null)
					dlFile.delete();
				errorMessage = "Error opening file:\n  " + filename + "\n  " + err.getMessage();
				return null;
			}
		}
		// Establish connection and download requested file
		try {
			// Set up the request
			if (address.indexOf("http:/") == 0)
				addrNet = true;
			else if (verbose && address.indexOf("file:/") == 0)
				addrNet = false;
			else {
				errorMessage = "Invalid location: " + address;
				return null;
			}
			if (addrNet) {
				dlConnect = (HttpURLConnection) dlURL.openConnection();
				dlConnect.setRequestMethod("GET");
				if (dataType.equalsIgnoreCase("binary"))
					acceptProperty = dataTypeBinary;
				else
					acceptProperty = dataTypeText;
				dlConnect.setRequestProperty("Accept", acceptProperty);
				// Read the data into the requested file
				dlConnect.connect();
			} else {
				dlFileConn = (FileURLConnection) dlURL.openConnection();
				dlFileConn.connect();
			}
			dlData = dlURL.openStream();
			while ((len = dlData.read(streamData)) > 0) {
				try {
					fileData.write(streamData, 0 , len);
				} catch (IOException err) {
					if (addrNet)
						dlConnect.disconnect();
					else
						dlFileConn.close();
					dlFile.delete();
					errorMessage = "Error writing to file:\n  " + filename + "\n  " + err.getMessage();
					return null;
				}
			}
			// Close the connection and the file
			if (addrNet)
				dlConnect.disconnect();
			else
				dlFileConn.close();
			dlData.close();
		} catch (ProtocolException err) {
			errorMessage = "Invalid protocol method: " + err.getMessage();
			return null;
		// Close the connection and delete the file
		} catch (IOException err) {
			if (addrNet & dlConnect != null)
				dlConnect.disconnect();
			else if (!addrNet & dlFileConn != null)
				dlFileConn.close();
			try {
				if (dlData != null)
					dlData.close();
			} catch (IOException e) {
				; // Ignore errors
			}
			if (dlFile != null)
				dlFile.delete();
			errorMessage = "Error downloading :\n  " + address + "\n  to " + filename + "\n  " + err.getMessage();
			return null;
		}

		return filePath;
	}

}
