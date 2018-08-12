/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package first.attempt.raspberry.plugin.impl.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;

import java.util.List;

/**
 * Device type specific feature management server
 */
public class DeviceTypeFeatureManager implements FeatureManager {

    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String CONTENT_TYPE = "contentType";
    private static final String PATH_PARAMS = "pathParams";
    private static final String QUERY_PARAMS = "queryParams";
    private static final String FORM_PARAMS = "formParams";
    private static int featureCount = 1;
    //Lista de las features que va a tener
    private static List<Feature> features = new ArrayList<Feature>();

    public DeviceTypeFeatureManager() {
        /*
        * Hemos modificado el constructor método para que solamente cree las
        * operaciones al llamar a la función que hemos creado para que lo
        * gestione, llamada createFeature, tenemos que tener en cuenta que las
        * operaciones deben estar definidas y configuradas (como los iconos) en
        * type-view/private/config.json
        */
	Feature deviceFeature =	createFeature("change-time","tiempo",
  "Cambiar Tiempo del Sensor","Cambia el tiempo de muestreo del sensor de temperatura");
	try {
		addFeature(deviceFeature);
	}catch (DeviceManagementException e) {
		e.printStackTrace();
	}
	deviceFeature =	createFeature("change-leds","estado","Matriz leds: on/off",
  "Enciende/Apaga la matriz de leds, acepta solo las opciones on y off");
	try {
		addFeature(deviceFeature);
	}catch (DeviceManagementException e) {
		e.printStackTrace();
	}
	deviceFeature =	createFeature("send-command","parametros","Mandar Orden",
  "Manda una orden de las listadas a la raspberry");
	try {
	//deviceFeature = getFeature("buzz");
		addFeature(deviceFeature);
	}catch (DeviceManagementException e) {
		e.printStackTrace();
	}

    }
    //TODO: Hay que hacer que en vez de string, acepte una lista de strings
    private Feature createFeature(String feature, String queryParam,
    String name, String description)
    {
	Feature newOperation = new Feature();
	Map<String, Object> apiParams = new HashMap<>();
        List<String> pathParams = new ArrayList<>();
        List<String> queryParams = new ArrayList<>();
        List<String> formParams = new ArrayList<>();
        List<Feature.MetadataEntry> metadataEntries = new ArrayList<>();
        Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();

	newOperation.setCode(feature);
        newOperation.setName(name);
        newOperation.setDescription(description);
        apiParams.put(METHOD, "POST");
        apiParams.put(URI, "/tempsensor/device/{deviceId}/" + feature);
        pathParams.add("deviceId");
        apiParams.put(PATH_PARAMS, pathParams);
        // Si existe queryParam suministrado a la función, lo añadimos.
        if(queryParam != null)
          queryParams.add(queryParam);
        apiParams.put(QUERY_PARAMS, queryParams);
        apiParams.put(FORM_PARAMS, formParams);
        metadataEntry.setId(featureCount);
        metadataEntry.setValue(apiParams);
        metadataEntries.add(metadataEntry);
	      featureCount++; //Aumentamos el contador de featureCount, para los Id
        newOperation.setMetadataEntries(metadataEntries);
	return newOperation;
    }
    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
	     features.add(feature);
        return true;
    }

    @Override
    public boolean addFeatures(List<Feature> features) throws DeviceManagementException {
        return false;
    }
    //TODO: Make it work
    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
      Feature tmpFeature = null;
      //iteramos hasta encontrar el index del que debemos borrar
      for (int i=0; i < features.size(); i++){
        Feature row = (Feature) features.get(i);
        if (row.getName().equals(name))
          tmpFeature = row;
      }
        return tmpFeature;
    }
    //Ahora no devuelve una feature, devuelve la lista de features existente
    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
//        List<Feature> features = new ArrayList<>();
//        features.add(feature);
        return features;
    }
    //TODO: ¿Funciona?
    @Override
    public boolean removeFeature(String name) throws DeviceManagementException {
      boolean removeStatus = false;
      for (int i=0; i < features.size(); i++){
        Feature row = (Feature) features.get(i);
        if (row.getName().equals(name))
        {
          features.remove(i);
          removeStatus = true;
        }
      }
        return removeStatus;
    }

    @Override
    public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
        return false;
    }
}
