/*
 * This file implements methods to work with json files
 *
 * TODO: Unify the writeOperation method and clear it
 *
 *
 */

package sensorboard.raspberry.api.util;

import com.google.gson.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import sensorboard.raspberry.api.util.deviceOperationJson;
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


/**
 * <h1>JsonUtils</h1>
 * La clase JsonUtils da métodos relacionados con la creación
 * del fichero json donde almacenar las distintas operaciones
 * que se mandan al dispositivo relacionado por deviceID
 * 
 * @author  Luis Valencia Pichardo
 * @version 1.0
 *
 */
public class JsonUtils {
	private static String jsonPath = "repository/deployment/server/jaggeryapps/devicemgt/sampleData.json";
	private static File fileJson = new File( jsonPath  );
	private static JsonObject jsonObject = new JsonObject();
	/*
	 *
	 */
	public static void saveOperation ( String operationId, String deviceId, String payload ){
	/*
	 *  Vamos a configurar la variable pendingDeviceOperation la cual va a almacenar los datos
	 *  de la última operación enviada al dispositivo identificado por deviceId
	 *
	 */
		deviceOperationJson pendingDeviceOperation = new deviceOperationJson();
		Gson gson = new Gson();
		String jsonString;
		
		pendingDeviceOperation.setName(operationId);
		pendingDeviceOperation.setDate();
		pendingDeviceOperation.setPayload(payload);
		// Si el fichero json no existe lo inicializamos y metemos la nueva (y unica) operación, 
		// si el fichero existe, tenemos que parsear las operaciones existente y almacenar
		// en la del deviceId		
		if (fileJson.exists())	 
		{        
			try {
				//Creamos un parser, que nos va a servir para leer el fichero JSON existente,
				//con el vamos a leer como un jsonElement, sin determinar que tipo json va a ser
				Path path = new File(jsonPath).toPath();
				Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
				JsonParser parser = new JsonParser();
				JsonElement jsonElement = parser.parse(new FileReader( jsonPath  ));
				//Pasamos todo el contenido del fichero json (en jsonElement) a JsonObject, 
				//el cual nos da métodos para poder usarlo.
				jsonObject = jsonElement.getAsJsonObject(); 
				//Comprobamos si la estructura json tiene un miembro ya existente del 
				//deviceId que deseamos añadir.
				if ( !jsonObject.has(deviceId) )
				{
					//Si no tiene un miembro del dispositivo que ha ejecutado la operación,
					//entonces necesitamos crear y añadir el nuevo miembro y luego añadirle 
					//la operación.
					FileWriter writer = new FileWriter(jsonPath);
					//Las operaciones se guardan y se leen del fichero como JsonArray
					ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>( );
					operationsArrayList.add(pendingDeviceOperation);
					//Pasamos el array a la estructura JSON y lo añadimos al fichero leido y
					//almacenado en jsonObject
					jsonObject.add(deviceId, gson.toJsonTree(operationsArrayList));
					//Gson nos proporciona el siguiente método para pasar de estructura JSON
					//a un string y así poder escribirlo en el fichero
					jsonString = gson.toJson(jsonObject);
					writer.write(jsonString);
      					writer.close();
				}
				else
				{
					//Hemos comprobado que el dispositivo ya tiene una estructura creada
					//en el fichero JSON, lo cual nos evita tener que crearla de nuevo,
					//solo tenemos que seleccionar en un array el campo de JSON que tenga 
					//por nombre el deviceId.
					deviceOperationJson[] operationArray = gson.fromJson(jsonObject.get(deviceId),deviceOperationJson[].class );
					//Pasamos a un arraylist, ya que el array nos limita a un tamaño fijo.
					ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>(Arrays.asList( operationArray ));
					//Al ArrayList de todas las operaciones del dispositivo que queremos
					//le añadimos la operación nueva que queremos almacenar y sobreescribimos
					//el campo del dispositivo en el fichero JSON.
					operationsArrayList.add(pendingDeviceOperation);
					jsonObject.add(deviceId, gson.toJsonTree(operationsArrayList));
					jsonString = gson.toJson(jsonObject);
					FileWriter writer = new FileWriter(jsonPath);
					writer.write(jsonString);
      					writer.close();
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		//Si el fichero no existe, debemos llamar a la función que lo crea y luego añadirle el 
		//dispositivo, esto ocurre solamente la primera vez, cuando se inicia todo el sistema,
		//tras enrolar un dispositivo y ejecutar una operación.
		else 
		{
			try 
			{
				fileJson.createNewFile();
				//JsonObject object = new JsonObject();
				FileWriter writer = new FileWriter(jsonPath);
				ArrayList<deviceOperationJson> operationsArrayList	= new ArrayList<deviceOperationJson>( );
				operationsArrayList.add(pendingDeviceOperation);
				//object.add(deviceId, gson.toJsonTree(operationsArrayList));
				jsonObject.add(deviceId, gson.toJsonTree(operationsArrayList));
				//String jsonArray = gson.toJson(jsonObject);
				jsonString = gson.toJson(jsonObject);
		      		writer.write(jsonString);
	      			writer.close();
			}catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
/*	public static void createJsonStructure(){
 *   		if(!fileJson.exists()){         
*			try {
*			} catch (IOException e) {								
*				e.printStackTrace();
*			}
*
*		}
*	}
*/
}
