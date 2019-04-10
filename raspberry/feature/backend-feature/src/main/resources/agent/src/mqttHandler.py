#!/usr/bin/env python

"""
/**
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
**/
"""

import paho.mqtt.client as mqtt
import time
import logging
import iotUtils
import os

global mqttClient
mqttClient = mqtt.Client(client_id='myRaspberry_client')


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when the client receives a CONNACK response from the server.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_connect(mqttClient, userdata, flags, rc):
    print("MQTT_LISTENER: Connected with result code " + str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    print ("MQTT_LISTENER: Subscribing with topic " + TOPIC_TO_SUBSCRIBE)
    mqttClient.subscribe(TOPIC_TO_SUBSCRIBE)


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when a PUBLISH message is received from the server.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_message(mqttClient, userdata, msg):
    print("MQTT_LISTENER: " + msg.topic + " " + str(msg.payload))
    #Let's split the message content for further operations
    method,parameter=str(msg.payload).split(":",1);

    #What kind of message is it and doing what it should
    if method == "ledsRequest" :
        print("Received a request to turn ON/OFF the led matrix")
        iotUtils.LEDS_STATE = iotUtils.setLedsValue(parameter)
        #Matrix operations
    elif  method == 'timeRequest':
        print("Received a request to change the sensor push time")
        iotUtils.PUSH_INTERVAL = iotUtils.setPushValue(parameter)

    elif  method == "reboot" :
        print("Received a request to reboot the device")
        os.system("sleep "+parameter)
        os.system("reboot")

    elif  method == "shutdown" :
        print("Received a request to shutdown the device")
        os.system("sleep "+parameter)
        os.system("shutdown now")

    elif  method == "bash":
        print("Received a bash command")
        if "_" in parameter:
            command, arguments = str( parameter ).split("_", 1)
            if "_" in arguments:
                parsedArguments = arguments.replace("_", " ")
            else:
                parsedArguments = arguments

            os.system( command + " " + parsedArguments )
        else:
            os.system( command )
        #TODO: Filter which commands?
    else:
        print("Unknown message received")

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when a PUBLISH message to the server when door is open or close
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_publish(mqttClient, stream1PlayLoad):
    mqttClient.publish(TOPIC_TO_PUBLISH_STREAM1, stream1PlayLoad)


def sendSensorValue(stream1PlayLoad):
    global mqttClient
    on_publish(mqttClient, stream1PlayLoad)


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the server script
#			This method is invoked from Agent.py on a new thread
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    MQTT_ENDPOINT = iotUtils.MQTT_EP.split(":")
    MQTT_IP = MQTT_ENDPOINT[1].replace('//', '')
    MQTT_PORT = int(MQTT_ENDPOINT[2])

    DEV_OWNER = iotUtils.DEVICE_OWNER
    DEV_ID = iotUtils.DEVICE_ID
    DEV_TYPE = iotUtils.DEVICE_TYPE
    TANENT_DOMAIN = iotUtils.SERVER_NAME
    global TOPIC_TO_SUBSCRIBE
    TOPIC_TO_SUBSCRIBE = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/command"
    global TOPIC_TO_PUBLISH_STREAM1
    TOPIC_TO_PUBLISH_STREAM1 = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/temperature"

    print ("MQTT_LISTENER: MQTT_ENDPOINT is " + str(MQTT_ENDPOINT))
    print ("MQTT_LISTENER: MQTT_TOPIC is " + TOPIC_TO_SUBSCRIBE)
    global mqttClient
    mqttClient.username_pw_set(iotUtils.AUTH_TOKEN, password="")
    mqttClient.on_connect = on_connect
    mqttClient.on_message = on_message
    while True:
        try:
            mqttClient.connect(MQTT_IP, MQTT_PORT, 60)
            print "MQTT_LISTENER: " + time.asctime(), "Connected to MQTT Broker - %s:%s" % (
            MQTT_IP, MQTT_PORT)
            mqttClient.loop_forever()

        except (KeyboardInterrupt, Exception) as e:
            print "MQTT_LISTENER: Exception in MQTTServerThread (either KeyboardInterrupt or Other)"
            print ("MQTT_LISTENER: " + str(e))

            mqttClient.disconnect()
            print "MQTT_LISTENER: " + time.asctime(), "Connection to Broker closed - %s:%s" % (
            MQTT_IP, MQTT_PORT)
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            pass


if __name__ == '__main__':
    main()
