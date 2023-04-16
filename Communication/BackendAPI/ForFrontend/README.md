
# Backend API for Frontend

## Authentication

Authentication for a User requires:

- The "User-Agent"-Header needs to be specified but can have any Value.
- The "Authorization"-Header has to be specified and have a JSON-Value containing the User's Username and Token:
```json
{
    "username": "[INSERT-USERNAME-HERE]",
    "token": "[INSERT-TOKEN-HERE]"
}
```

## Standard Error Resonses

The following Responses can happen on any Endpoint if the given Condition is met:

- If an Error occured inside the Backend during the Processing of the Request:
    - 5XX HTTP Status Code
    - Body:
        ```json
            {
                "message": "[INSERT-ERROR-MESSAGE-HERE]"
            }
        ```  

- For non-public Endpoints 
    - If the Authentication of the User failed:
        - 401 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 
    - If the User does not have sufficient Permission to access the Endpoint:
        - 403 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```   

# Public Endpoints

> **No Authentication is needed for Public Endpoints.**

## Login User

- Endpoint: /login
- Methods: 
    - GET
    - POST
- No additional Headers
- Parameters:
    - If Method is GET:
        - "username" = "[INSERT-USERNAME-HERE]"
        - "password" = "[INSERT-PASSWORD-HERE]"
    - If Method is POST:
        - No Parameters
- Body:
    - If Method is GET:
        - No Body
    - If Method is POST:
        ```json
            {
                "username": "[INSERT-USERNAME-HERE]",
                "password": "[INSERT-PASSWORD-HERE]"
            }
        ```
- Responses:
    - If the Authentication was successful:
        - 200 HTTP Status Code
        - Body:
            ```json
                {
                    "token": "[INSERT-TOKEN-HERE]",
                    "permissions": [
                        "[PERMISSION-1]",
                        "[PERMISSION-2]",
                        ...
                    ]
                }
            ```
    - If the Authentication failed:
        - 401 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

## Register

- Endpoint: /register
- Methods: 
    - GET
    - POST
- No additional Headers
- Parameters:
    - If Method is GET:
        - "username" = "[INSERT-USERNAME-HERE]"
        - "password" = "[INSERT-PASSWORD-HERE]"
        - "email" = "[INSERT-EMAIL-HERE]"
    - If Method is POST:
        - No Parameters
- Body:
    - If Method is GET:
        - No Body
    - If Method is POST:
        ```json
            {
                "username": "[INSERT-USERNAME-HERE]",
                "password": "[INSERT-PASSWORD-HERE]"
            }
        ```
- Responses:
    - If the User Creation was successful:
        - 200 HTTP Status Code
        - Body:
            ```json
                {
                    "token": "[INSERT-TOKEN-HERE]",
                    "permissions": [
                        "[PERMISSION-1]",
                        "[PERMISSION-2]",
                        ...
                    ]
                }
            ```
    - If the Creation of the User failed:
        - 409 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```

## Scan QR Code

> Used by Frontend to get Plant Details when Guest scans a QR-Code on a Plant.  
> The Frontend also calls the "/get-pictures"-Endpoint to get the Pictures of the Plant.

- Endpoint: /scan-qr-code
- Methods: 
    - GET
- No additional Headers
- Parameters:
    - "qr-code" = "[INSERT-QR-CODE-ID-HERE]"
- No Body
- Responses:
    - If the Plant with the given Id was found:
        - 200 HTTP Status Code
        - Response Body:
            ```json
                {
                    "plant": {
                        "name": "[INSERT-PLANT-NAME-HERE]",
                        "room-name": "[INSERT-ROOM-NAME-HERE]",
                        "pictures": [
                            "[INSERT-PICTURE-ID-HERE]",
                            "[INSERT-PICTURE-ID-HERE]",
                            ...
                        ]
                    }
                }
            ```
    - If the Plant could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```    

## Get Plant Pictures

<!-- TODO: Is it better to return Pictures one by one? -->
<!-- TODO: Should the Pictures really be Base64 encoded? -->

- Endpoint: /get-pictures
- Methods: 
    - GET
- No additional Headers
- No Parameters:
- Request Body:
    ```json
        [
            {
                "plant-id": "[INSERT-PLANT-ID-HERE]",
                "pictures": [
                    "[INSERT-PICTURE-ID-HERE]",
                    "[INSERT-PICTURE-ID-HERE]",
                    ...
                ]
            },
            {
                "plant-id": "[INSERT-PLANT-ID-HERE]",
                "pictures": [
                    "[INSERT-PICTURE-ID-HERE]",
                    "[INSERT-PICTURE-ID-HERE]",
                    ...
                ]
            },
            ...
        ]
    ```
- Responses:
    - If the Plant with the given Id was found:
        - 200 HTTP Status Code
        - Response Body:
            ```json
                {
                    "encoding": "base64",
                    "pictures": [
                        {
                            "plant-id": "[INSERT-PLANT-ID-HERE]",
                            "pictures": [
                                "[INSERT-ENCODED-PICTURE-HERE]",
                                "[INSERT-ENCODED-PICTURE-HERE]",
                                ...
                            ]
                        },
                        {
                            "plant-id": "[INSERT-PLANT-ID-HERE]",
                            "pictures": [
                                "[INSERT-ENCODED-PICTURE-HERE]",
                                "[INSERT-ENCODED-PICTURE-HERE]",
                                ...
                            ]
                        },
                        ...
                    ]
                }
            ```
    - If the Plant could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```  

## Upload Plant Picture

- Endpoint: /upload-picture
- Methods: 
    - POST
    - PUT
- No additional Headers
- Parameters:
    - "plant-id" = "[INSERT-PLANT-ID-HERE]"
- Body:
    ```json
        {
            "picture": "[INSERT-PICTURE-HERE]"
        }
    ```
- Responses:
    - If the Picture was saved:
        - 201 HTTP Status Code
        - No Body
    - If the Plant could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```  





### User Endpoints

> **User Endpoints require the querying User to have the Permission "USER"**

### Logout

- Endpoint: /logout
- Methods: 
    - GET
    - POST
- No additional Headers
- No Parameters
- No Body
- Responses:
    - If successful:
        - 200 HTTP Status Code
        - No Body

### Update settings

- Endpoint: /update-settings
- Methods: 
    - POST
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "password": "[INSERT-PASSWORD-HERE]", (opt)
            "email": "[INSERT-EMAIL-HERE]"
        }
    ```
- Response:
    - If the Update was successful:
        - 200 HTTP Status Code
        - No Response Body
    - If the specified User did not exist:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```
    - If the Update was not possible:
        - 409 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```

### Get Dashboard Data

- Endpoint: /get-dashboard-data
- Methods: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Responses:
    - If successful:
        - 200 HTTP Status Code
        - Response Body:
            ```json
                {
                    "plants": [
                        {
                            "plant-name": "[INSERT-PLANT-NAME-HERE]",
                            "values": [
                                {
                                    "timestamp": "[INSERT_TIME_STAMP_HERE]",
                                    "sensors": [
                                        {
                                            "sensor": "[INSERT_SENSOR_NAME-HERE]",
                                            "value": "[INSERT_SENSOR_VALUE_HERE]",
                                            "unit": "[INSERT-UNIT-HERE]", (opt)
                                            "alarm": 'n' or 'h' or 'l'
                                        },
                                        {
                                            "sensor": "[INSERT_SENSOR_NAME-HERE]",
                                            "value": "[INSERT_SENSOR_VALUE_HERE]",
                                            "unit": "[INSERT-UNIT-HERE]", (opt)
                                            "alarm": 'n' or 'h' or 'l'
                                        },
                                        ...
                                    ]
                                    ...
                                },
                                {
                                    "timestamp": "[INSERT_TIME_STAMP_HERE]",
                                    "sensors": [
                                        {
                                            "sensor": "[INSERT_SENSOR_NAME-HERE]",
                                            "value": "[INSERT_SENSOR_VALUE_HERE]",
                                            "unit": "[INSERT-UNIT-HERE]", (opt)
                                            "alarm": 'n' or 'h' or 'l'
                                        },
                                        {
                                            "sensor": "[INSERT_SENSOR_NAME-HERE]",
                                            "value": "[INSERT_SENSOR_VALUE_HERE]",
                                            "unit": "[INSERT-UNIT-HERE]", (opt)
                                            "alarm": 'n' or 'h' or 'l'
                                        },
                                        ...
                                    ]
                                    ...
                                },
                                ...
                            ]
                        },
                        ...
                    ]
                }
            ``` 

## Get all Plants

- Endpoint: /get-all-plants
- Methods: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Responses:
    - On Success:
        - 200 HTTP Status Code
        - Response Body:
            ```json
                {
                    "plants": [
                        {
                            "id": "[INSERT-PLANT-ID-HERE]",
                            "plant-name": "[INSERT-PLANT-NAME-HERE]",
                            "room-name": "[INSERT-ROOM-NAME-HERE]"
                        },
                        {
                            "id": "[INSERT-PLANT-ID-HERE]",
                            "plant-name": "[INSERT-PLANT-NAME-HERE]",
                            "room-name": "[INSERT-ROOM-NAME-HERE]"
                        },
                        ...
                    ]
                }
            ```

## Add Plant to Dashboard

- Endpoint: /add-to-dashboard
- Methods: 
    - PUT
    - POST
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "id": "[INSERT-PLANT-ID-HERE]"
        }
    ```
- Responses:
    - If successful:
        - 200 HTTP Status Code
        - No Body
    - If the Plant could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

## Remove Plant from Dashboard

- Endpoint: /remove-from-dashboard
- Methods: 
    - UPDATE
    - DELETE
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "id": "[INSERT-PLANT-ID-HERE]"
        }
    ```
- Response:
    - If successful:
        - 200 HTTP Status Code
        - No Body
    - If the Plant could not be found or was not contained in the User's Dashboard:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 
    







### Gardener Endpoints

> **Gardener Endpoints require the querying User to have the Permission "GARDENER" and to be assigned to the Plant that they want to modify**

### Set Sensor Limits

- Endpoint: /set-sensor-limits
- Methods: 
    - PUT
    - POST
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "id": "[INSERT-PLANT-ID-HERE]",
            "limits": [
                {
                    "sensor": "[INSERT-SENSOR-NAME-HERE]",
                    "uppper-limit": "[INSERT-UPPER-LIMIT-HERE]",
                    "lower-limit": "[INSERT-LOWER-LIMIT-HERE]",
                },
                {
                    "sensor": "[INSERT-SENSOR-NAME-HERE]",
                    "uppper-limit": "[INSERT-UPPER-LIMIT-HERE]",
                    "lower-limit": "[INSERT-LOWER-LIMIT-HERE]",
                },
                ...
            ]
        }
    ```
- Responses:
    - If successful:
        - 200 HTTP Status Code
        - No Body
    - If the Plant could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

### Set Transfer Interval

- Endpoint: /set-transfer-interval
- Methods: 
    - PUT
    - POST
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "id": "[INSERT-PLANT-ID-HERE]",
            "transfer-interval": [INSERT-TRANSFER-INTERVAL-HERE]
        }
    ```
- Responses:
    - If successful:
        - 200 HTTP Status Code
        - No Body
    - If the Plant could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

### Add Plant Picture

See "Upload Plant Picture" (/upload-picture)

### Delete Plant Picture

- Endpoint: /delete-picture
- Methods: 
    - POST
    - PUT
- No additional Headers
- Parameters:
    - "plant-id" = "[INSERT-PLANT-ID-HERE]"
    - "picture-id" = "[INSERT-PICTURE-ID-HERE]"
- Body:
- Responses:
    - If the Picture was saved:
        - 201 HTTP Status Code
        - No Body
    - If the Plant or Picture could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 






### Admin Endpoints

> **Admin Endpoints require the querying User to have the Permission "ADMIN"**

### Get Access Points

- Endpoint: /get-access-points
- Methods: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Response:
    - If successful:
        - 200 Status Code
        - Response Body:
            ```json
                {
                    "access-points": [
                        {
                            "id": "[INSERT-ACCESS-POINT-ID-HERE]",
                            "room-name": "[INSERT-ACCESS-POINT-ROOM-NAME-HERE]",
                            "locked": [true of false]
                        },
                        {
                            "id": "[INSERT-ACCESS-POINT-ID-HERE]",
                            "room-name": "[INSERT-ACCESS-POINT-ROOM-NAME-HERE]",
                            "locked": [true of false]
                        },
                        ...
                    ]
                }
            ```

### Get Sensor Stations

- Endpoint: /get-sensor-stations
- Methods: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Response:
    - If successful:
        - 200 Status Code
        - Response Body:
            ```json
                {
                    "sensor-stations": [
                        {
                            "id": "[INSERT-SENSOR-STATION-ID-HERE]",
                            "dip-switch": 0 - 255
                            "locked": [true of false]
                        },
                        {
                            "id": "[INSERT-SENSOR-STATION-ID-HERE]",
                            "dip-switch": 0 - 255
                            "locked": [true of false]
                        },
                        ...
                    ]
                }
            ```

### Scan for SensorStations

> This Endpoint might take a while to finish.  
> First the "pairing-mode"-Flag in the given Access Point's Config is set.  
> Then the Access Point gets the Config and starts to scan for Sensor Stations.  
> Then it reports it's findings back to the Backend.  
> Only at this point can the Sensor Stations be displayed.  
>
> This Endpoint only returns locked (or newly found) Sensor Stations.

- Endpoint: /scan-for-sensor-stations
- Methods: 
    - POST
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "access-point-id": "[INSERT-ACCESS-POINT-ID-HERE]"
        }
    ```
- Response:
    - If successful:
        - 200 Status Code
        - Response Body:
            ```json
                {
                    "sensor-stations": [
                        {
                            "id": "[INSERT-SENSOR-STATION-ID-HERE]",
                            "dip-switch": 0 - 255
                        },
                        {
                            "id": "[INSERT-SENSOR-STATION-ID-HERE]",
                            "dip-switch": 0 - 255
                        },
                        ...
                    ]
                }
            ```
    - If the Access Point could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

### Lock/Unlock Access Point

- Endpoint: /set-lock-access-point
- Methods: 
    - POST
    - PUT
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "access-point-id": "[INSERT-ACCESS-POINT-ID-HERE]",
            "locked": [true or false]
        }
    ```
- Response:
    - If successful:
        - 200 Status Code
        - No Response Body
    - If the Access Point could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

### Lock/Unlock SensorStation

- Endpoint: /set-lock-sensor-station
- Methods: 
    - POST
    - PUT
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "sensor-station-id": "[INSERT-SENSOR-STATION-ID-HERE]",
            "locked": [true or false]
        }
    ```
- Response:
    - If successful:
        - 200 Status Code
        - No Response Body
    - If the Sensor Station could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

### Create Plant QR-Code

- Endpoint: /create-plant-qr-code
- Methods: 
    - GET
    - POST
    - PUT
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "plant-id": "[INSERT-PLANT-ID-HERE]",
            "qr-code-id": "[INSERT-QR-CODE-ID-HERE]"
        }
    ```
- Responses:
    - If successful:
        - 200 Status Code
        - No Response Body
    - If the Plant could not be found:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ``` 

### Get all Users

- Endpoint: /get-all-users
- Methods: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Response:
    ```json
        [
            {
                "id": "[INSERT-USER-UUID-HERE]",
                "username": "[INSERT-USER-USERNAME-HERE]",
                "email": "[INSERT-USER-EMAIL-HERE]",
                "permissions": [
                    "[INSERT-PERMISSION-HERE]",
                    "[INSERT-PERMISSION-HERE]",
                    ...
                ]
            },
            {
                "id": "[INSERT-USER-UUID-HERE]",
                "username": "[INSERT-USER-USERNAME-HERE]",
                "email": "[INSERT-USER-EMAIL-HERE]",
                "permissions": [
                    "[INSERT-PERMISSION-HERE]",
                    "[INSERT-PERMISSION-HERE]",
                    ...
                ]
            },
            ...
        ]
    ```

### Get all Permissions

> Returns a List of all known Permissions

- Endpoint: /get-all-permissions
- Methods: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Response:
    ```json
        [
            "[INSERT-PERMISSION-HERE]",
            "[INSERT-PERMISSION-HERE]",
            ...
        ]
    ```

### Add User

- Endpoint: /create-user
- Methods: 
    - POST
    - PUT
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "username": "[INSERT-USERNAME-HERE]",
            "password": "[INSERT-PASSWORD-HERE]",
            "email": "[INSERT-EMAIL-HERE]",
            "permissions": [
                "[INSERT-PERMISSION-HERE]",
                "[INSERT-PERMISSION-HERE]",
                ...
            ]
        }
    ```
- Response:
    - If the User Creation was successful:
        - 200 HTTP Status Code
        - No Response Body
    - If the Creation of the User failed:
        - 409 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```

### Update User

> NOTE: The Username cannot be updated.

- Endpoint: /update-user
- Methods: 
    - POST
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "id": "[INSERT-USER-ID-HERE]",
            "password": "[INSERT-PASSWORD-HERE]", (opt)
            "email": "[INSERT-EMAIL-HERE]",       (opt)
            "permissions": [                      (opt)
                "[INSERT-PERMISSION-HERE]",
                "[INSERT-PERMISSION-HERE]",
                ...
            ]
        }
    ```
- Response:
    - If the Update was successful:
        - 200 HTTP Status Code
        - No Response Body
    - If the specified User did not exist:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```
    - If the Update was not possible:
        - 409 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```

### Delete User

- Endpoint: /delete-user
- Methods: 
    - DELETE
- No additional Headers
- No Parameters
- Body:
    ```json
        {
            "id": "[INSERT-USER-ID-HERE]"
        }
    ```
- Responses:
    - If the Deletion was successful:
        - 200 HTTP Status Code
        - No Response Body
    - If the specified User did not exist:
        - 404 HTTP Status Code
        - Body:
            ```json
                {
                    "message": "[INSERT-ERROR-MESSAGE-HERE]"
                }
            ```

### Get Logs
- Endpoint: /get-logs
- Methods: 
    - GET
- No additional Headers
- No Parameters
- No Body
- Response:
    ```json
        {
            "logs": [
                {
                    "severity": "[INSERT-SEVERITY-HERE]",
                    "message": "[INSERT-MESSAGE-HERE]",
                    "timestamp": "[INSERT-TIMESTAMP-HERE]"
                },
                {
                    "severity": "[INSERT-SEVERITY-HERE]",
                    "message": "[INSERT-MESSAGE-HERE]",
                    "timestamp": "[INSERT-TIMESTAMP-HERE]"
                },
                ...
            ]
        }
    ```

