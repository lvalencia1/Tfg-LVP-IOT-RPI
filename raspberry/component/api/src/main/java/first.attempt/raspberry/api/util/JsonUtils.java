/*
 * This file implements methods to work with json files
 *
 *
 *
 *
 */

package first.attempt.raspberry.api.util;

import com.google.gson.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import first.attempt.raspberry.api.util.deviceOperationJson;
import java.io.FileOutputStream;

public class JsonUtils {
	public static void writeOperation ( String operationId, String deviceId ){
		File output = new File ("sampleData.json");
		deviceOperationJson deviceOperations = new deviceOperationJson();
		Gson gson = new Gson();
		if (output.exists())	 
		{

		}
		else 
		{
			deviceOperations.setName(operationId);
			deviceOperations.setDate();
			String json = gson.toJson(deviceOperations);
			try 
			{
				FileWriter writer = new FileWriter("sampleData.json");
		      		writer.write(json);
	      			writer.close();
			}
			catch (Exception e) {
				System.out.println("ERROR JSON" + e.toString());
			} 
		}
	}
}
