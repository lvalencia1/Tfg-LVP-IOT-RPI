/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package first.attempt.raspberry.api;

import first.attempt.raspberry.api.dto.DeviceJSON;
import first.attempt.raspberry.api.dto.SensorRecord;
import first.attempt.raspberry.api.util.APIUtil;
import first.attempt.raspberry.api.util.ZipUtil;
import first.attempt.raspberry.api.util.ZipArchive;
import first.attempt.raspberry.api.util.deviceOperationJson;
import first.attempt.raspberry.api.util.JsonUtils;
import first.attempt.raspberry.plugin.constants.DeviceTypeConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ConfigOperation;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;

/*import java.io.FileOutputStream;
*import javax.json.Json;
*import javax.json.JsonArray;
*import javax.json.JsonWriter;
*/
/*
 * For Json parsing
 */
import com.google.gson.*;
/**
 * This is the API which is used to control and manage device type functionality
 */
public class DeviceTypeServiceImpl implements DeviceTypeService {

    private static final String KEY_TYPE = "PRODUCTION";
    private static Log log = LogFactory.getLog(DeviceTypeService.class);
    private static ApiApplicationKey apiApplicationKey;

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }
    /**
     * @param agentInfo device owner,id
     * @return true if device instance is added to map
     */
    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDevice(final DeviceJSON agentInfo) {
        String deviceId = agentInfo.deviceId;
        if ((agentInfo.deviceId != null) && (agentInfo.owner != null)) {
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }

    /**
     * @param deviceId unique identifier for given device type instance
     * @param time    new value for the device to send sensor records
     */
    @Path("device/{deviceId}/change-time")
    @POST
    public Response changeTime(@PathParam("deviceId") String deviceId,
                                 @QueryParam("tiempo") int time,
                                 @Context HttpServletResponse response) {//throws Exception {
        try {
		        //Vemos si está autorizado el dispositivo
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DeviceTypeConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
	           //Vemos si el string introducido por el usuario está vacío o no.
             if( time < 0 || time == 0 ) {
               log.error("The requested time value for the sensor retrieving must be >0");
               return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
             }
            //Configuremos la operación para mandarla
            Map<String, String> dynamicProperties = new HashMap<>();
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + DeviceTypeConstants.DEVICE_TYPE + "/" + deviceId + "/command";
            dynamicProperties.put(DeviceTypeConstants.ADAPTER_TOPIC_PROPERTY, publishTopic);
	          //Almacenamos la operación para mandarla a
            Operation commandOp = new CommandOperation();
            commandOp.setCode("change-time");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(Integer.toString(time));
	          //Método propio que almacena los comandos
            try {
              JsonUtils.saveOperation("Cambiar Tiempo del Sensor",deviceId,"Tiempo introducido:" + Integer.toString(time));
            }catch(Exception e) {
              System.out.println("ERROR" + e.toString());
            }
            //Configuramos MQTT
            Properties props = new Properties();
            props.setProperty("mqtt.adapter.topic", publishTopic);
            commandOp.setProperties(props);

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, DeviceTypeConstants.DEVICE_TYPE));
	          //Aqui metemos la operación a la lista de operaciones para ser usadas, a través de git hub podemos
	          //ver las distintas clases involucradas en https://github.com/wso2/carbon-device-mgt/
            APIUtil.getDeviceManagementService().addOperation(DeviceTypeConstants.DEVICE_TYPE, commandOp,
                    deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while executing command operation upon changing the sensor time";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (InvalidDeviceException e) {
            String msg = "Error occurred while executing command operation upon changing the sensor time";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    /**
     * @param deviceId unique identifier for given device type instance
     * @param state    change status of sensor: on/off
     */
    @Path("device/{deviceId}/change-leds")
    @POST
    public Response changeLeds(@PathParam("deviceId") String deviceId,
                                 @QueryParam("estado") String state,
                                 @Context HttpServletResponse response) {//throws Exception {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DeviceTypeConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String sensorState = state.toUpperCase();
            if (!sensorState.equals(DeviceTypeConstants.STATE_ON) && !sensorState.equals(
                    DeviceTypeConstants.STATE_OFF)) {
                log.error("The requested state change should be either - 'ON' or 'OFF'");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
            }
            Map<String, String> dynamicProperties = new HashMap<>();
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + DeviceTypeConstants.DEVICE_TYPE + "/" + deviceId + "/command";
            dynamicProperties.put(DeviceTypeConstants.ADAPTER_TOPIC_PROPERTY, publishTopic);
            Operation commandOp = new CommandOperation();
            commandOp.setCode("change-leds");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(state);
	          try {
              JsonUtils.saveOperation("Matriz Leds on/off",deviceId,"Valor introducido:"+state);
	          }catch(Exception e) {
              System.out.println("ERROR" + e.toString());
            }
            Properties props = new Properties();
            props.setProperty("mqtt.adapter.topic", publishTopic);
            commandOp.setProperties(props);

            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, DeviceTypeConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(DeviceTypeConstants.DEVICE_TYPE, commandOp,
                    deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while executing command operation upon ringing the buzzer";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (InvalidDeviceException e) {
            String msg = "Error occurred while executing command operation to send keywords";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("device/{deviceId}/send-command")
    public Response sendCommand(@PathParam("deviceId") String deviceId,  @FormParam("orden") String command,
    @QueryParam("parametros") String parameters, @Context HttpServletResponse response) {
      String[] stringList = parameters.split("\\s+");
      int stringCount = stringList.length;
          log.error("STRING COUNT: "+Integer.toString(stringCount));
      //Vemos si la variable está vacía o no, no debería estar vacía.
      if (command == null || command.isEmpty()) {
		    log.error("The \"orden\" field is not defined for operations");
		    return Response.status(Response.Status.BAD_REQUEST).build();
	    }
      //Veamos que el campo parametros sea válido segun que orden se mande
      switch(command){
        case "reboot":
        // Vemos que no se haya metido mas de un número y además que sea un numero y no una cadena
        if ( stringCount > 1){
          log.error("The parameter field, if used, should contain only one value if the command is to reboot");
          return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else if ( stringCount == 1) {
          boolean isValidInteger = false;
          try{
            int time = Integer.parseInt(parameters);
            isValidInteger = true;
            if( !isValidInteger || time < 0 )
            {
              log.error("The parameter field, if used, should contain only an integer higher than 0, if the command is to reboot");
              return Response.status(Response.Status.BAD_REQUEST).build();
            }
          }
          catch (NumberFormatException ex)
          {  ex.printStackTrace();
            log.error("The parameter field is not a number, when trying to execute the reboot operation");
            return Response.status(Response.Status.BAD_REQUEST).build();
          }
        }
          break;
        case "shutdown":
          // Vemos que no se haya metido mas de un número y además que sea un numero y no una cadena
          if ( stringCount > 1){
            log.error("The parameter field, if used, should contain only one value if the command is to shutdown");
            return Response.status(Response.Status.BAD_REQUEST).build();
          }
          else if ( stringCount == 1) {
            boolean isValidInteger = false;
          //Vamos a comprobar si hemos metido un numero o no TODO: Falta ver que solo si se mete
            try{
              int time = Integer.parseInt(parameters);
              isValidInteger = true;
              if( !isValidInteger || time < 0 )
              {
                log.error("The parameter field, if used, should contain only an integer higher than 0, if the command is to shutdown");
                return Response.status(Response.Status.BAD_REQUEST).build();
              }
            }
            catch (NumberFormatException ex)
            {  ex.printStackTrace();
              log.error("The parameter field is not a number, when trying to execute the shutdown operation");
              return Response.status(Response.Status.BAD_REQUEST).build();
            }
          }
          break;
        case "bash":
          if( parameters == null || stringCount < 1 || parameters.isEmpty()){
            log.error("The parameter field should contain at least the bash command");
    		    return Response.status(Response.Status.BAD_REQUEST).build();
            }
          break;
        default:
          log.error("The requested command doesn't have a valid format");
		      return Response.status(Response.Status.BAD_REQUEST).build();
      }
      /*
	    if (!switchToState.equals(DeviceTypeConstants.STATE_ON) && !switchToState.equals(
				    DeviceTypeConstants.STATE_OFF)) {
		    log.error("The requested state change shoud be either - 'ON' or 'OFF'");
		    return Response.status(Response.Status.BAD_REQUEST).build();
      }*/
	    try {
		    if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(
					    new DeviceIdentifier(deviceId, DeviceTypeConstants.DEVICE_TYPE),
					    DeviceGroupConstants.Permissions.DEFAULT_OPERATOR_PERMISSIONS)) {
			    return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
					    }
		    String resource = command;
		    String actualMessage = resource + ":" + parameters;
		    String publishTopic = APIUtil.getTenantDomainOftheUser() + "/"
			    + DeviceTypeConstants.DEVICE_TYPE + "/" + deviceId;

		    ConfigOperation commandOp = new ConfigOperation();
		    commandOp.setCode("send-command");
		    commandOp.setEnabled(true);
		    commandOp.setPayLoad(actualMessage);

        try {
          JsonUtils.saveOperation("Mandar Orden",deviceId,"Comando:"+command+"\t Parametros:"+parameters);
        }catch(Exception e) {
          System.out.println("ERROR" + e.toString());
        }

		    Properties props = new Properties();
            		props.setProperty("mqtt.adapter.topic", publishTopic);
		    commandOp.setProperties(props);
		    List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
		    deviceIdentifiers.add(new DeviceIdentifier(deviceId,DeviceTypeConstants.DEVICE_TYPE));
		    APIUtil.getDeviceManagementService().addOperation(DeviceTypeConstants.DEVICE_TYPE, commandOp,
				    deviceIdentifiers);
		    return Response.ok().build();
	    }  catch (InvalidDeviceException e) {
		    String msg = "Error occurred while executing command operation to send keywords";
		    log.error(msg, e);
		    return Response.status(Response.Status.BAD_REQUEST).build();
	    } catch (DeviceAccessAuthorizationException e) {
		    log.error(e.getErrorMessage(), e);
		    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    } catch (OperationManagementException e) {
		    String msg = "Error occurred while executing command operation upon ringing the buzzer";
		    log.error(msg, e);
		    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	    }
    }


    /**
     * Retrieve Sensor data for the given time period
     *
     * @param deviceId unique identifier for given device type instance
     * @param from     starting time
     * @param to       ending time
     * @return response with List<SensorRecord> object which includes sensor data which is requested
     */
    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")

    //Sospecho que es invocado cuando lo envía el agente
    public Response getSensorStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                   @QueryParam("to") long to, @QueryParam("sensorType") String sensorType) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "meta_deviceId:" + deviceId + " AND meta_deviceType:" +
                DeviceTypeConstants.DEVICE_TYPE + " AND meta_time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = null;
        if (sensorType.equals(DeviceTypeConstants.SENSOR_TYPE1)) {
            sensorTableName = DeviceTypeConstants.SENSOR_TYPE1_EVENT_TABLE;
        } else if (sensorType.equals(DeviceTypeConstants.SENSOR_TYPE2)) {
            sensorTableName = DeviceTypeConstants.SENSOR_TYPE2_EVENT_TABLE;
        }
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DeviceTypeConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            if (sensorTableName != null) {
                List<SortByField> sortByFields = new ArrayList<>();
                SortByField sortByField = new SortByField("meta_time", SortType.ASC);
                sortByFields.add(sortByField);
                List<SensorRecord> sensorRecords = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
                return Response.status(Response.Status.OK.getStatusCode()).entity(sensorRecords).build();
            }
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /**
     * To download device type agent source code as zip file
     *
     * @param deviceName name for the device type instance
     * @param sketchType folder name where device type agent was installed into server
     * @return Agent source code as zip file
     */
    @Path("/device/download")
    @GET
    @Produces("application/zip")
    public Response downloadSketch(@QueryParam("deviceName") String deviceName,
                                   @QueryParam("sketchType") String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), deviceName, sketchType);
            Response.ResponseBuilder response = Response.ok(FileUtils.readFileToByteArray(zipFile.getZipFile()));
            response.status(Response.Status.OK);
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
            Response resp = response.build();
            zipFile.getZipFile().delete();
            return resp;
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (JWTClientException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (APIManagerException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (UserStoreException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }

    /**
     * Register device into device management service
     *
     * @param deviceId unique identifier for given device type instance
     * @param name     name for the device type instance
     * @return check whether device is installed into cdmf
     */
    private boolean register(String deviceId, String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DeviceTypeConstants.DEVICE_TYPE);
            if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                return false;
            }
            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            device.setName(name);
            device.setType(DeviceTypeConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
            device.setEnrolmentInfo(enrolmentInfo);
            boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);
            return added;
        } catch (DeviceManagementException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private ZipArchive createDownloadFile(String owner, String deviceName, String sketchType)
            throws DeviceManagementException, JWTClientException, APIManagerException,
            UserStoreException {
        //create new device id
        String deviceId = shortUUID();
        if (apiApplicationKey == null) {
            String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getRealmConfiguration().getAdminUserName();
            applicationUsername = applicationUsername + "@" + APIUtil.getAuthenticatedUserTenantDomain();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String[] tags = {DeviceTypeConstants.DEVICE_TYPE};
            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    DeviceTypeConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true,
                    "3600");
        }
        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
        String scopes = "device_type_" + DeviceTypeConstants.DEVICE_TYPE + " device_" + deviceId;
        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                apiApplicationKey.getConsumerSecret(), owner + "@" + APIUtil.getAuthenticatedUserTenantDomain(), scopes);

        //create token
        String accessToken = accessTokenInfo.getAccessToken();
        String refreshToken = accessTokenInfo.getRefreshToken();
        boolean status = register(deviceId, deviceName);
        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }
        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile = ziputil.createZipFile(owner, APIUtil.getTenantDomainOftheUser(), sketchType,
                deviceId, deviceName, accessToken, refreshToken, apiApplicationKey.toString());
        return zipFile;
    }
}
