/*
 * This file implements methods to work with json files
 *
 * TODO: Unify the writeOperation method and clear it
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
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class JsonUtils {
	private static File fileJson = new File("sampleData.json");
	public static void writeOperation ( String operationId, String deviceId ){
		deviceOperationJson deviceOperations = new deviceOperationJson();
		Gson gson = new Gson();
		deviceOperations.setName(operationId);
		deviceOperations.setDate();
		String json = gson.toJson(deviceOperations);
		if (fileJson.exists())	 
		{        
			try {
				Path path = new File("sampleData.json").toPath();						          
				Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
				deviceOperationJson[] operationArray = gson.fromJson(reader, deviceOperationJson[].class);
				ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>(Arrays.asList( operationArray ));
				operationsArrayList.add(deviceOperations);
				operationsArrayList.add(deviceOperations);
				String jsonArray = gson.toJson(operationsArrayList);
				FileWriter writer = new FileWriter("sampleData.json");
				writer.write(jsonArray);
	      			writer.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		else 
		{
			try 
			{
				createJsonStructure();
				FileWriter writer = new FileWriter("sampleData.json");
				ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>( );
				operationsArrayList.add(deviceOperations);
				String jsonArray = gson.toJson(operationsArrayList);
		      		writer.write(jsonArray);
	      			writer.close();
			}
			catch (Exception e) {
				System.out.println("ERROR JSON" + e.toString());
			} 
		}
	}
	public static void createJsonStructure(){
    		if(!fileJson.exists()){         
			try {
				fileJson.createNewFile();
			} catch (IOException e) {								
				e.printStackTrace();
			}

		}
	}

}
