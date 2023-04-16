
# Backend API for Access Point

## Authentication

Authentication for an Access Point requires:

- The "User-Agent"-Header needs to be specified and have the Value "AccessPoint".
- The "Authorization"-Header has to be specified and have a JSON-Value containing the Access Point's Token:
```json
{
    "token": "[INSERT-TOKEN-HERE]"
}
```

> This is done to be consistent with the User Authentication which is also sent in JSON-Format.

## Response Status Code Behaviour

Getting a 401 or 403 HTTP Status Code from any Endpoint indicates to the Access Point that it was locked.  
Afterwards it should only keep trying to get it's Configuration in a specified Interval.
This should keep up until it gets a 200 HTTP Status Code from the Backend indicating that it is unlocked again.  

After getting a 5XX HTTP Status Code Response the Access Point should once retry querying.  
If this Query also results in 5XX then it should behave like it just got a 401 HTTP Status Code. 

# Endpoints

## Register Access Point

> Should be queried by the Access Point after Startup.  
> The Access Point should create an ID for itself the first Time it starts, which it then stores permanently.
>
> If the Access Point was successfully registered the Token is stored permanently on the Access Point.

- Endpoint: /ap/register
- Methods: 
    - POST
    - PUT
- No additional Headers
- No Parameters
- Request Body:
    ```json
        {
            "accessPointId": "[INSERT-ACCESS-POINT-ID-HERE]",
            "room_name": "[INSERT-ACCESS-POINT-ROOM-NAME-HERE]"
        }
    ```
- Responses: 
    - If Access Point is unlocked:
        - 200 HTTP Status Code
        - Body: 
        ```json
            {
                "token": "[INSERT-TOKEN-HERE]"
            }
        ```
    - If Access Point is locked:
        - 401 HTTP Status Code
        - No Body
    - If an Error occured on the Backend:
        - 5XX HTTP Status Code
        - No Body

## Get Configuration Endpoint

> Should be queried in a regular Interval by the Access Point.

- Endpoint: /ap/get-config
- Type: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Responses:
    - If the Sensor Station is unlocked, the Configuration is returned:
        - 200 HTTP Status Code
        - Response Body:
            ```json
                {
                    
                    "room-name": "[INSERT-ROOM-NAME-HERE]", (optional)
                    "pairingMode": [true or false],
                    "transferInterval": [INSERT-TRANSFER-INTERVAL-HERE]
                    
                    "sensorStations": [
                        {
                            "bdAddress": "[INSERT-SENSOR-STATION-ID-HERE]",
                            "sensors": [
                                {
                                    "sensorName": "[INSERT-SENSOR-NAME-HERE]",
                                    "limits": { 
                                        "upperLimit": [INSERT-UPPER-LIMIT-HERE],
                                        "lowerLimit": [INSERT-LOWER-LIMIT-HERE]
                                    },
                                    "thresholdTime": "[INSERT-THRESHOLD-TIME-HERE]"
                                },{
                                    "sensor-name": "[INSERT-SENSOR-NAME-HERE]",
                                    "limits": { 
                                        "upperLimit": [INSERT-UPPER-LIMIT-HERE],
                                        "lowerLimit": [INSERT-LOWER-LIMIT-HERE]
                                    },
                                    "thresholdTime": "[INSERT-THRESHOLD-TIME-HERE]"
                                },
                                ...
                            ]
                        },
                        {
                            "bdAddress": "[INSERT-SENSOR-STATION-ID-HERE]",
                            "sensors": [
                                ...   
                            ]
                        },
                        ...
                    ]
                }
            ```
    - If the Access Point is locked:
        - 401 HTTP Status Code
        - No Body
    - If an Error occured on the Backend:
        - 5XX HTTP Status Code
        - No Body

## Remarks

The Sensor Stations that are returned in the "sensor-stations"-Array are the only ones that the Access Point should connect to.  
Any other Sensor Stations that the Access Point may see are to be looked upon as "locked".  

Sensor Stations that disconnected stay unlocked.  
As long as they continue being sent in the Config and do not need re-confirmation by an Admin.  
If the Access Point finds a disconnected Sensor Station by scanning, Communication is immidiately resumed.

##### Transfer Sensor Data

> Called by the Access Point after every Transfer Interval.
> 
> Transfer all Sensor Data that was collected by the Access Point from the Sensor Stations to the Backend.  
> The Access Point can safely delete the Sensor Data after getting the Response from the Backend.

- Endpoint: /transfer-data
- Type: 
    - POST
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "sensorStations": [
                {
                    "bdAddress": "[INSERT-SENSOR-STATION-1-ID]",
                    "connectionAlive": [true or false],
                    "dipSwitchId": "[INSERT-DIP-SWITCH-ID-HERE]",
                    "values": [
                        {
                            "timestamp": "[INSERT_TIME_STAMP_HERE]",
                            "sensors": [
                                {
                                    "sensor": "[INSERT_SENSOR_NAME-HERE]",
                                    "value": [INSERT_SENSOR_VALUE_HERE],
                                    "unit": "[INSERT-UNIT-HERE]" (opt),
                                    "alarm": 'n' or 'h' or 'l'
                                },
                                {
                                    "sensor": "[INSERT_SENSOR_NAME-HERE]",
                                    "value": [INSERT_SENSOR_VALUE_HERE],
                                    "unit": "[INSERT-UNIT-HERE]" (opt),
                                    "alarm": 'n' or 'h' or 'l'
                                },
                                ...
                            ]
                        },
                        {
                            "timestamp": "[INSERT_TIME_STAMP_HERE]",
                            "sensors": [
                                ...
                            ]
                        },
                        ...
                    ]
                },
                {
                    "bdAddress": "[INSERT-SENSOR-STATION-2-ID]",
                    "connectionAlive": [true or false],
                    "dipSwitch": "[INSERT-DIP-SWITCH-ID-HERE]",
                    "values": [
                        ...
                    ],
                },
                ...
            ]
        }
    ```
- Responses:
    - If the Operation was successful:
        - 2XX HTTP Status Code
        - No Body
    - If the Access Point is locked:
        - 403 HTTP Status Code
        - No Body
    - If an Error occured on the Backend:
        - 5XX HTTP Status Code
        - No Body

## Remarks

The Data stored locally on the Access Point can safely be deleted  after a Response with a 2XX HTTP Status Code was received.    

##### Found Sensor Station

> Called by the Access Point in Pairing Mode after finding one or more Sensor Stations.  

- Endpoint: /found-sensor-stations
- Type: 
    - PUT
- No additional Headers
- No Parameters
- Body: 
    ```json
        {
            "sensorStations": [
                {
                    "bdAddress": "[INSERT-FIRST-SENSOR-STATION-UUID-HERE]",
                    "dipSwitchId": "[INSERT-DIP-SWITCH-ID-HERE]",
                },
                {
                    "bdAddress": "[INSERT-FIRST-SENSOR-STATION-UUID-HERE]",
                    "dipSwitch": "[INSERT-DIP-SWITCH-ID-HERE]",
                },
                ...
            ]
        }
    ```
- Response:
    - If the Operation was successful:
        - 200 HTTP Status Code
        - No Body
    - If the Access Point is locked:
        - 403 HTTP Status Code
        - No Body
    - If an Error occured on the Backend:
        - 5XX HTTP Status Code
        - No Body
