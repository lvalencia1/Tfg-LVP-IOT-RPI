/*
 * This file implements methods to work with json files
 *
 * TODO: Unify the writeOperation method and clear it
 *
 *
 */

package first.attempt.raspberry.api.util;

import com.google.gson.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import first.attempt.raspberry.api.util.deviceOperationJson;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;



public class JsonUtils {
	private static File fileJson = new File("repository/deployment/server/jaggeryapps/devicemgt/sampleData.json");
	public static void writeOperation ( String operationId, String deviceId ){
		deviceOperationJson deviceOperations = new deviceOperationJson();
		Gson gson = new Gson();
		deviceOperations.setName(operationId);
		deviceOperations.setDate();
		String json = gson.toJson(deviceOperations);
		if (fileJson.exists())	 
		{        
			try {
				Path path = new File("repository/deployment/server/jaggeryapps/devicemgt/sampleData.json").toPath();						          
				Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
				//deviceOperationJson[] operationArray = gson.fromJson(reader, deviceOperationJson[].class);
				//ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>(Arrays.asList( operationArray ));
				JsonParser parser = new JsonParser();
				JsonElement jsonElement = parser.parse(new FileReader("repository/deployment/server/jaggeryapps/devicemgt/sampleData.json"));
				JsonObject jsonObject = new JsonObject();	
				jsonObject = jsonElement.getAsJsonObject(); //
				JsonObject object = new JsonObject();
				if ( !jsonObject.has(deviceId) )
				{
				FileWriter writer = new FileWriter("repository/deployment/server/jaggeryapps/devicemgt/sampleData.json");
					ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>( );
				operationsArrayList.add(deviceOperations);
				jsonObject.add(deviceId, gson.toJsonTree(operationsArrayList));
				String jsonArray = gson.toJson(jsonObject);
		      		writer.write(jsonArray);
	      			writer.close();


				}
				else
				{
				deviceOperationJson[] operationArray = gson.fromJson(jsonObject.get(deviceId),deviceOperationJson[].class );
				ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>(Arrays.asList( operationArray ));
				//
				operationsArrayList.add(deviceOperations);
				jsonObject.add(deviceId, gson.toJsonTree(operationsArrayList));
				String jsonArray = gson.toJson(jsonObject);
				FileWriter writer = new FileWriter("repository/deployment/server/jaggeryapps/devicemgt/sampleData.json");
				writer.write(jsonArray);
	      			writer.close();
				}
				//
				//
				//

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		else 
		{
			try 
			{
				createJsonStructure();
				JsonObject object = new JsonObject();
				FileWriter writer = new FileWriter("repository/deployment/server/jaggeryapps/devicemgt/sampleData.json");
				ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>( );
				operationsArrayList.add(deviceOperations);
				object.add(deviceId, gson.toJsonTree(operationsArrayList));
//				String jsonArray = gson.toJson(operationsArrayList);
				String jsonArray = gson.toJson(object);
		      		writer.write(jsonArray);
	      			writer.close();
			}
			catch (Exception e) {
				//System.out.println("ERROR JSON" + e.toString());
				e.printStackTrace();
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
