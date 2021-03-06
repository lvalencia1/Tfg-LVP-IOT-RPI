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

package sensorboard.raspberry.api;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;

import sensorboard.raspberry.plugin.constants.myRaspberryConstants;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.FormParam;

/**
 * This is the API which is used to control and manage device type functionality.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = myRaspberryConstants.DEVICE_TYPE),
                                @ExtensionProperty(name = "context", value = "/"+myRaspberryConstants.DEVICE_TYPE),
                        })
                }
        ),
        tags = {
                @Tag(name = myRaspberryConstants.DEVICE_TYPE+",device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Enroll device",
                        description = "",
                        key = "perm:" + myRaspberryConstants.DEVICE_TYPE + ":enroll",
                        permissions = {"/device-mgt/devices/enroll/" + myRaspberryConstants.DEVICE_TYPE }
                )
        }
)
@SuppressWarnings("NonJaxWsWebServices")
public interface myRaspberryService {
    String SCOPE = "scope";

    /**
     * @param deviceId unique identifier for given device type instance
     * @param state    change status of sensor: on/off
     */
    @Path("device/{deviceId}/sensor")
    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Cambia el tiempo del sensor",
            notes = "",
            response = Response.class,
            tags = myRaspberryConstants.DEVICE_TYPE ,
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:" + myRaspberryConstants.DEVICE_TYPE + ":enroll")
                    })
            }
    )
    Response changeTime(@PathParam("deviceId") String deviceId,
                          @QueryParam("tiempo") int state,
                          @Context HttpServletResponse response);
    @Path("device/{deviceId}/leds")
    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Enciende/Apaga la matriz de leds",
            notes = "",
            response = Response.class,
            tags = myRaspberryConstants.DEVICE_TYPE ,
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:" + myRaspberryConstants.DEVICE_TYPE + ":enroll")
                    })
            }
    )
    Response changeLeds(@PathParam("deviceId") String deviceId,
                          @QueryParam("estado") String state,
                          @Context HttpServletResponse response);

    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Enciende/Apaga la matriz de leds",
            notes = "",
            response = Response.class,
            tags = myRaspberryConstants.DEVICE_TYPE ,
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:" + myRaspberryConstants.DEVICE_TYPE + ":enroll")
                    })
            }
    )
    public Response registerDevice(final DeviceJSON agentInfo);
    
    @POST
    @Path("device/{deviceId}/command")
    @ApiOperation(
    consumes = MediaType.APPLICATION_JSON,
    httpMethod = "PUT",
    value = "Manda una orden a la Raspberry ",
    notes = "",
    response = Response.class,
    tags = myRaspberryConstants.DEVICE_TYPE,
    extensions = {
      @Extension(properties = {
                 @ExtensionProperty(name = SCOPE, value = "perm:" + myRaspberryConstants.DEVICE_TYPE + ":enroll")
               })
             }
             )
    Response sendCommand(@PathParam("deviceId") String deviceId,  @FormParam("orden") String state,
    @QueryParam("parametros") String parameters, @Context HttpServletResponse response);

    /**
     * Retrieve Sensor data for the given time period.
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
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Sensor Stats",
            notes = "",
            response = Response.class,
            tags = myRaspberryConstants.DEVICE_TYPE,
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:" + myRaspberryConstants.DEVICE_TYPE + ":enroll")
                    })
            }
    )
    Response getSensorStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                            @QueryParam("to") long to, @QueryParam("sensorType") String sensorType);

    /**
     * To download device type agent source code as zip file.
     *
     * @param deviceName name for the device type instance
     * @param sketchType folder name where device type agent was installed into server
     * @return Agent source code as zip file
     */
    @Path("/device/download")
    @GET
    @Produces("application/zip")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Download agent",
            notes = "",
            response = Response.class,
            tags = myRaspberryConstants.DEVICE_TYPE ,
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:" + myRaspberryConstants.DEVICE_TYPE + ":enroll")
                    })
            }
    )
    Response downloadSketch(@QueryParam("deviceName") String deviceName, @QueryParam("sketchType") String sketchType);
}
