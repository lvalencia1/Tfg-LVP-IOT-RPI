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

package sensorboard.raspberry.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sensorboard.raspberry.plugin.constants.myRaspberryConstants;
import sensorboard.raspberry.plugin.exception.DeviceMgtPluginException;
import sensorboard.raspberry.plugin.impl.dao.myRaspberryDAO;
import sensorboard.raspberry.plugin.impl.util.myRaspberryUtils;

import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements IotDeviceDAO for myRaspberry Devices.
 */
public class myRaspberryDAOImpl {

    private static final Log log = LogFactory.getLog(myRaspberryDAOImpl.class);

    public Device getDevice(String deviceId) throws DeviceMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        Device iotDevice = null;
        ResultSet resultSet = null;
        try {
            conn = myRaspberryDAO.getConnection();
            String selectDBQuery =
                    "SELECT myRaspberry_DEVICE_ID, DEVICE_NAME" +
                            " FROM myRaspberry_DEVICE WHERE myRaspberry_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                iotDevice = new Device();
                iotDevice.setName(resultSet.getString(
                        myRaspberryConstants.DEVICE_PLUGIN_DEVICE_NAME));
                if (log.isDebugEnabled()) {
                    log.debug("myRaspberry device " + deviceId + " data has been fetched from " +
                            "myRaspberry database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching myRaspberry device : '" + deviceId + "'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            myRaspberryUtils.cleanupResources(stmt, resultSet);
            myRaspberryDAO.closeConnection();
        }
        return iotDevice;
    }

    public boolean addDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = myRaspberryDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO myRaspberry_DEVICE(myRaspberry_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, device.getDeviceIdentifier());
            stmt.setString(2, device.getName());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("myRaspberry device " + device.getDeviceIdentifier() + " data has been" +
                            " added to the myRaspberry database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the myRaspberry device '" +
                    device.getDeviceIdentifier() + "' to the myRaspberry db.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            myRaspberryUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = myRaspberryDAO.getConnection();
            String updateDBQuery =
                    "UPDATE myRaspberry_DEVICE SET  DEVICE_NAME = ? WHERE myRaspberry_DEVICE_ID = ?";
            stmt = conn.prepareStatement(updateDBQuery);
            if (device.getProperties() == null) {
                device.setProperties(new ArrayList<Device.Property>());
            }
            stmt.setString(1, device.getName());
            stmt.setString(2, device.getDeviceIdentifier());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("myRaspberry device " + device.getDeviceIdentifier() + " data has been" +
                            " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the myRaspberry device '" +
                    device.getDeviceIdentifier() + "' data.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            myRaspberryUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean deleteDevice(String deviceId) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = myRaspberryDAO.getConnection();
            String deleteDBQuery =
                    "DELETE FROM myRaspberry_DEVICE WHERE myRaspberry_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("myRaspberry device " + deviceId + " data has deleted" +
                            " from the myRaspberry database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting myRaspberry device " + deviceId;
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            myRaspberryUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public List<Device> getAllDevices() throws DeviceMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Device device;
        List<Device> iotDevices = new ArrayList<>();
        try {
            conn = myRaspberryDAO.getConnection();
            String selectDBQuery =
                    "SELECT myRaspberry_DEVICE_ID, DEVICE_NAME " +
                            "FROM myRaspberry_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                device = new Device();
                device.setDeviceIdentifier(resultSet.getString(myRaspberryConstants.DEVICE_PLUGIN_DEVICE_ID));
                device.setName(resultSet.getString(myRaspberryConstants.DEVICE_PLUGIN_DEVICE_NAME));
                List<Device.Property> propertyList = new ArrayList<>();
                device.setProperties(propertyList);
            }
            if (log.isDebugEnabled()) {
                log.debug("All myRaspberry device details have fetched from myRaspberry database.");
            }
            return iotDevices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all myRaspberry device data'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            myRaspberryUtils.cleanupResources(stmt, resultSet);
            myRaspberryDAO.closeConnection();
        }
    }
}
