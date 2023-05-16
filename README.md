# Plant Health

<table>
    <td>
        <tr>
            [![Pipeline Status](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/badges/main/pipeline.svg)](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/commits/main)
        </tr>
        <tr>
            [![Latest Release](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/badges/release.svg)](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/releases)
        </tr>
    </td>
</table>

## System Overview

For an overview of the system see the corresponding [Wiki](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/wikis/home).

## Setup Instructions for Acceptance Test

### Frontend + Backend

1. Make sure that [Docker](https://www.docker.com/) is running on your machine. See [https://docs.docker.com/desktop/](https://docs.docker.com/desktop/) for installation instructions.
2. Open a terminal and navigate to the root directory of the repository.
3. Backend, frontend and database can be deployed by running the command `docker compose up --build`.  
4. If not changed, the webapp can be reached via [http://localhost:3000/](http://localhost:3000/). See `docker-compose.yml` to see the exposed ports.

### Accesspoint

1. Setup the Raspberry Pi, configure it so that it has access to the network in which also frontend and backend are available and enable SSH.
2. Copy the whole directory `access_point` to the Raspberry Pi.
3. SSH into the Raspberrry Pi.
4. CD into the previously copied `access_point` directory.
5. Run `./configure`. The script will guide you through the accesspoint initialization and provide all necessary pre-requisites. Use `http://<IP address of the machine running the backend>:8443` as your backend URL/IP. Confirm all other except the last dialogue with just pressing ENTER to keep default values. In the end, the script will ask you if you prefer to automatically start the access point. Enter `y` and the the main routine will automatically start (and even restart after reboots) after max. 1 minute. You can check the file `main.log` for log output. This file will only be created once the main routine is started. A command like `clear && tail -50 main.log` has proven helpful to analyze the log file.
6. Once you don't need a running accesspoint anymore, execute `./reset`. Main routine execution will be stopped and default state will be restored. Be aware that the database file, configuration file and all logfiles will be removed. Create copies before running `./reset` if you need backups of these files. You will have to re-execute step 5 to restart the accesspoint.

### Sensorstation

TODO: Add directory of schematics and parts list
1. Build the Arduino 33 BLE sensorstation using the schematics in the "Software Concept" found in the wiki.
2. Setup the Arduino 33 BLE, connect it to your computer and upload the code from `sensorstation/sensorstation.ino` to the Arduino with PlatformIO. If you don't have PlatformIO installed, you can use the Arduino IDE, but you will have to install the `ArduinoBLE` library manually. If special configuration is needed, you can change the values in 'sensor_station/include/Defines.h'.
3. Place the plant in the sensorstation and close the lid.
4. Once the Arduino gets connected to power, it will immediately go into the pairing mode, resulting in the led blinking in the color defined in the Software Concept. The pairing mode will be active for 5 minutes. During this time, you can connect to the Arduino using the Plant Health App. If the pairing mode is over, you can restart it by pressing the button on the Arduino for 1 seconds. The pairing mode will be active for 5 minutes again.
5. Once the Arduino is conneted to the accesspoint, it will send data to the backend and signal over the LED and piezo buzzer if a problem is detected, if not defined otherwise in the 'sensor_station/include/Defines.h' file.
6. To reset the sensorstation one can just power it off or press the reset button for 5 seconds. The sensorstation will then restart and go into pairing mode again without having a paired device.


## Development
- Pre-commit hooks
    This project makes use of [pre-commit](https://pre-commit.com) to ensure a uniform coding style.

    For this to work you have to install `pre-commit` on your system (with pip) and then execute `pre-commit install` in the root of this repo. After that the pre-commit hooks should automatically run before you commit.

- Backend 
    The backend is a Spring Boot application, for which gradle as well as maven are configured. 
    To run the application execute `gradle bootRun` or `mvn spring-boot:run` in the `backend` directory.
	The backend requires a MySQL database to be reachable using the credentials given in the `application.yml`. 

- Frontend 
    The frontend is a Svelte.js application, that uses npm. **pnpm is recommended**
    To start the dev server execute `pnpm dev` in the `frontend` directory.

- Accesspoint
    The accesspoint is a Raspberry Pi that runs a single Python script. 

- Sensorstation
    The sensorstation is an Arduino 33 BLE that is programmed using [Platform IO](https://platformio.org/).
	

## License

This project is licensed under the [GPLv3 license].

[GPLv3 License]: https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/blob/main/LICENSE

## Contributors:
- Lukas Kirchmair
- Fabian Margreiter
- Emanuel Prader
- Fabio Plunser 
- David Rieser
