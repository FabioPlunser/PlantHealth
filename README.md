# Plant Health

[![Pipeline Status](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/badges/main/pipeline.svg)](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/commits/main)
[![Coverage](https://qe-sonarqube.uibk.ac.at/api/project_badges/measure?project=SWESS23_G1T1&metric=coverage&token=sqb_e89c28cb541c824fa55ab0dd3a5581255c4a05a4)](https://qe-sonarqube.uibk.ac.at/dashboard?id=SWESS23_G1T1)
[![Quality Gate Status](https://qe-sonarqube.uibk.ac.at/api/project_badges/measure?project=SWESS23_G1T1&metric=alert_status&token=sqb_e89c28cb541c824fa55ab0dd3a5581255c4a05a4)](https://qe-sonarqube.uibk.ac.at/dashboard?id=SWESS23_G1T1)

[![Latest Release](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/badges/release.svg)](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/releases)

A system for keeping track of the Health of various Plants.


A plant is placed inside a sensor station container, the sensor station has a number of sensors that measure the humidity, air pressure, air humidity, air quality, temperature and light intensity of the environment.
The sensor station then sends the data to the access point, which then sends the data to the backend server, which then stores the data in a database.
The data is then displayed on a web server.

## Overview

The System is built from 5 different Components:

- Central Backend Server managing all the data using a REST-API (named Backend).
- A persistent Database connected to the Backend for persisting the Sensor data.
- Web Server for displaying the data (named Frontend).
- Accesspoints that communicate with the Sensorstations and report data back to the central Backend.
- Sensorstations that record Earth Humidity, Air Pressure, Air Humidity, Air Quality, Temparature and Light Intensity for a given Plant.

![Component Diagram](https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/raw/media/Diagrams/component-diagram.drawio.png)

## Usage

### Docker Compose
The Backend, Frontend and Database can be deployed using `docker compose up`.  
See the `docker-compose.yml` to see the exposed ports.  
If the Profile `test` is specified, then an `Adminer`-Instance is started that can be used to manually edit the database.

### Development
- Pre-commit hooks:
    This project makes use of [pre-commit](https://pre-commit.com) to ensure a uniform coding style.

    For this to work you have to install `pre-commit` on your system (with pip) and then execute `pre-commit install` in the root of this repo. After that the pre-commit hooks should automatically run before you commit.
    

- Backend 
    The Backend is a Spring Boot Application, for which gradle as well as maven are configured. 
    To run the application execute `gradle bootRun` or `mvn spring-boot:run` in the `backend` directory.
	The Backend requires a MySQL Database to be reachable using the Credentials given in the `application.yml`. 

- Frontend 
    The Frontend is a Svelte.js Application, that uses npm. **pnpm is recommended**
    To start the dev server execute `pnpm dev` in the `frontend` directory.

- Accesspoint
    The Accesspoint is a Raspberry Pi that is run using a Python Script. 

- Sensorstation
    The Sensorstation is an Arduino 33 BLE that is programmed using [Platform IO](https://platformio.org/).
	
## Setup instructions for acceptance test

### Frontend + Backend

TODO

### Accesspoint

TODO

### Sensorstation

TODO

## License

This project is licensed under the [GPLv3 license].

[GPLv3 License]: https://git.uibk.ac.at/informatik/qe/swess23/group1/g1t1/-/blob/main/LICENSE

## Contributors:
- Lukas Kirchmair
- Fabian Margreiter
- Emanuel Prader
- Fabio Plunser 
- David Rieser
