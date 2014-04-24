package org.scripps.util;

import java.io.FileWriter;
import java.io.IOException;
 
public class GenerateCSV
{

public void generateCsvFile(String sFileName, String data)
   {
	try
	{
	    FileWriter writer = new FileWriter(sFileName, true);
	    writer.append(data+"\n");
	    writer.flush();
	    writer.close();
	}
	catch(IOException e)
	{
	     e.printStackTrace();
	} 
    }
}