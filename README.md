# Plant Health

[![Pipeline Status](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/badges/main/pipeline.svg)](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/commits/main)
[![Coverage](https://qe-sonarqube.uibk.ac.at/api/project_badges/measure?project=SWESS23_G1T1&metric=coverage&token=sqb_e89c28cb541c824fa55ab0dd3a5581255c4a05a4)](https://qe-sonarqube.uibk.ac.at/dashboard?id=SWESS23_G1T1)
[![Quality Gate Status](https://qe-sonarqube.uibk.ac.at/api/project_badges/measure?project=SWESS23_G1T1&metric=alert_status&token=sqb_e89c28cb541c824fa55ab0dd3a5581255c4a05a4)](https://qe-sonarqube.uibk.ac.at/dashboard?id=SWESS23_G1T1)

[![Latest Release](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/badges/release.svg)](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/releases)

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

1. TODO
2. ...
3. ...

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
