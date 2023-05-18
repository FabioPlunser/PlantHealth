import requests
import random
import string
import uuid
from termcolor import colored
import json
from datetime import datetime
import logging

import randomGenerator

logging.basicConfig(
    filename="log.log",
    level=logging.INFO,
    format="%(asctime)s %(levelname)s: %(message)s",
)
url = "http://localhost:8080"
# ----------------------------------------------
# ----------------------------------------------


class User:
    def __init__(self, username, password, email):
        self.username = username
        self.password = password
        self.email = email
        self.token = ""
        self.personId = ""
        self.permissions = ""

    def setToken(self, token):
        self.token = token

    def setPersonId(self, personId):
        self.personId = personId

    def setPermissions(self, permissions):
        self.permissions = permissions

    def __str__(self):
        return f"User: Username: {self.username}, Password: {self.password}, Email: {self.email}, \nToken: {self.token}, \nPersonId: {self.personId}, \nPermissions: {self.permissions}"


# ----------------------------------------------
# ----------------------------------------------


class AccessPoint:
    def __init__(
        self,
        accessPointId,
        selfAssignedId,
        roomName,
        unlocked,
        scanActive,
        connected,
        transferInterval,
        sensorStations,
    ):
        self.accessPointId = accessPointId
        self.selfAssignedId = selfAssignedId
        self.roomName = roomName
        self.unlocked = unlocked
        self.scanActive = scanActive
        self.connected = connected
        self.transferInterval = transferInterval
        self.token = None
        self.sensorStations = sensorStations

    def foundSensorStations(self, sensorStations):
        self.sensorStations = sensorStations

    def __str__(self):
        return f"AccessPoint: accessPointId: {self.accessPointId}, selfAssignedId: {self.selfAssignedId}, roomName: {self.roomName}, unlocked: {self.unlocked}, scanActive: {self.scanActive}, connected: {self.connected}, transferInterval: {self.transferInterval}, token: {self.token}, sensorStations: {self.sensorStations}"


# ----------------------------------------------
# ----------------------------------------------


class SensorStation:
    def __init__(self, bdAddress, dipSwitchId, accessPoint):
        self.sensorStationId = None
        self.bdAddress = bdAddress
        self.dipSwitchId = dipSwitchId
        self.roomName = None
        self.name = None
        self.unlocked = False
        self.connected = False
        self.deleted = False
        self.accessPoint = accessPoint
        self.sensorData = []
        self.sensorLimit = []

    def setDipSwitchId(self, dipSwitchId):
        self.dipSwitchId = dipSwitchId

    def __str__(self):
        return f"SensorStation: sensorStationId: {self.sensorStationId}, bdAddress: {self.bdAddress}, dipSwitchId: {self.dipSwitchId}, roomName: {self.roomName}, name: {self.name}, unlocked: {self.unlocked}, connected: {self.connected}, deleted: {self.deleted}, accessPoint: {self.accessPoint}, sensorData: {self.sensorData}, sensorLimit: {self.sensorLimit}"


# ----------------------------------------------
# ----------------------------------------------


class SensorData:
    pass


# ----------------------------------------------
# ----------------------------------------------


class SensorLimit:
    pass


# ----------------------------------------------
# ----------------------------------------------


class Sensor:
    def __init__(self, type, unit):
        self.type = type
        self.unit = unit


# ----------------------------------------------
# ----------------------------------------------


class SensorStationPicture:
    pass


# ----------------------------------------------
# ----------------------------------------------
Admin = User("admin", "password", "admin@admin.com")
Users = []
AccessPoints = []
SensorStations = []
SensorData = []
SensorLimit = []

standardSensors = [
    {
        "type": "TEMPERATURE",
        "unit": "C",
    },
    {
        "type": "HUMIDITY",
        "unit": "%",
    },
    {
        "type": "PRESSURE",
        "unit": "hpa",
    },
    {
        "type": "SOILHUMIDITY",
        "unit": "%",
    },
    {
        "type": "LIGHTINTENSITY",
        "unit": "lux",
    },
    {
        "type": "GASPRESSURE",
        "unit": "ppm",
    },
]
userCount = 10
howOftenGardener = 2
accessPointsCount = 6
howOftenUnlockAccessPoint = 1
howManySensorStations = 6
howOftenUnlockSensorStation = 1
howManyDataPointsPerSensor = 50

# ----------------------------------------------
# ----------------------------------------------


def updateUser(newUser):
    for i, user in enumerate(Users):
        if user.personId == newUser.personId:
            Users[i] = newUser
            return


# ----------------------------------------------
# ----------------------------------------------


def createAdmin():
    print(colored("Creating admin", "green"))
    name = randomGenerator.generate_name()
    params = {"username": "admin", "password": "password", "email": "admin@admin.com"}
    # register admin
    response = requests.post(f"{url}/register", data=params)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info("Admin created")
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Admin could not be created")
        return None


# ----------------------------------------------
# ----------------------------------------------


def login(user):
    print(colored("Logging in", "green"))
    params = {"username": user.username, "password": user.password}
    response = requests.post(f"{url}/login", data=params)
    if response.status_code == 200:
        user.setToken(response.json()["token"])
        user.setPersonId(response.json()["personId"])
        user.setPermissions(response.json()["permissions"])
        updateUser(user)
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"User login in: {user}")
        return user
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("User could not be loging in")
        return None


# ----------------------------------------------
# ----------------------------------------------


def changeToAdmin(user):
    print(colored("Changing to admin", "green"))
    params = {
        "personId": user.personId,
        "username": "admin",
        "password": "password",
        "email": "admin@admin.com",
        "permissions": "ADMIN",
    }

    headers = {
        "Authorization": json.dumps(
            {
                "token": user.token,
                "username": user.username,
            }
        )
    }

    response = requests.post(f"{url}/update-settings", data=params, headers=headers)
    if response.status_code != 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"User changed to admin: {user}")
        user.setPermissions("ADMIN")
        updateUser(user)
        return user
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("User could not be changed to admin")
        return None


# ----------------------------------------------
# ----------------------------------------------


def createUser():
    print(colored("Creating user", "green"))
    name = randomGenerator.generate_name()
    params = {
        "username": name,
        "password": "password",
        "email": randomGenerator.generate_email(name),
    }
    # add user to list of possible users
    user = User(params["username"], params["password"], params["email"])
    logging.info(f"User created: {user}")
    Users.append(user)

    # register user
    response = requests.post(f"{url}/register", data=params)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"User created: {user}")
        return user
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("User could not be created")
        return user


# ----------------------------------------------
# ----------------------------------------------


def changeToGardener(user):
    print(colored("Changing to gardener", "green"))
    params = {
        "personId": user.personId,
        "username": user.username,
        "password": "password",
        "email": user.email,
        "permissions": "GARDENER",
    }
    headers = {
        "Authorization": json.dumps(
            {
                "token": user.token,
                "username": user.username,
            }
        )
    }

    response = requests.post(f"{url}/update-settings", data=params, headers=headers)
    if response.status_code != 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"User changed to gardener: {user}")
        user.setPermissions("GARDENER")
        updateUser(user)
        return user
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("User could not be changed to gardener")
        return None


# ----------------------------------------------
# ----------------------------------------------
def createAccessPoints():
    print(colored("Creating access points", "green"))
    params = {
        "selfAssignedId": uuid.uuid4(),
        "roomName": "Room-" + randomGenerator.generate_room_name(),
    }
    response = requests.post(f"{url}/register-access-point", data=params)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"Access Point created: {params}")
        return params
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Access Point could not be created")
        return None


# ----------------------------------------------
# ----------------------------------------------


def updateAccessPoint(newAP):
    print(colored("Updating access point", "green"))
    for i, ap in enumerate(AccessPoints):
        if ap.selfAssignedId == newAP.selfAssignedId:
            AccessPoints[i] = newAP
            break


# ----------------------------------------------
# ----------------------------------------------


def getAccessPoints():
    print(colored("Getting access points", "green"))
    header = {
        "Authorization": json.dumps(
            {
                "token": Admin.token,
                "username": Admin.username,
            }
        )
    }

    response = requests.get(f"{url}/get-access-points", headers=header)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"Access Points retrieved: {response.json()}")

        for ap in response.json()["accessPoints"]:
            accessPointId = ap["accessPointId"]
            selfAssignedId = ap["selfAssignedId"]
            roomName = ap["roomName"]
            unlocked = ap["unlocked"]
            scanActive = ap["scanActive"]
            connected = ap["connected"]
            transferInterval = ap["transferInterval"]
            sensorStations = ap["sensorStations"]

            ap = AccessPoint(
                accessPointId,
                selfAssignedId,
                roomName,
                unlocked,
                scanActive,
                connected,
                transferInterval,
                sensorStations,
            )
            logging.info(f"Access Point updated: {ap}")
            AccessPoints.append(ap)
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Access Points could not be retrieved")


# ----------------------------------------------
# ----------------------------------------------


def unlockAccessPoint(ap):
    print(colored("Unlocking access point", "green"))
    params = {"accessPointId": ap.accessPointId, "unlocked": "true"}

    header = {
        "Authorization": json.dumps(
            {
                "token": Admin.token,
                "username": Admin.username,
            }
        )
    }
    print(colored("Unlocking Access Point", "green"))
    response = requests.post(
        f"{url}/set-unlocked-access-point", data=params, headers=header
    )
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"Access Point unlocked: {ap}")
        ap.unlocked = True
        updateAccessPoint(ap)
        return ap
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Access Point could not be unlocked")
        return None


# ----------------------------------------------
# ----------------------------------------------


def getAccessPointToken(ap):
    print(colored("Getting access point token", "green"))
    params = {"selfAssignedId": ap.selfAssignedId, "roomName": ap.roomName}

    response = requests.post(f"{url}/register-access-point", data=params)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"Access Point: {ap.roomName}, {ap.selfAssignedId} got token")
        ap.token = response.json()["token"]
        ap.unlocked = True
        updateAccessPoint(ap)
        return ap
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Access Point could not get token")
        return None


# ----------------------------------------------
# ----------------------------------------------


def foundSensorStationsForUnlockedAccessPoint(ap):
    print(colored("Found sensor stations for unlocked access point", "green"))
    _sensorStations = []
    requestData = []
    for i in range(0, random.randint(1, howManySensorStations)):
        bdAddress = randomGenerator.generate_ble_mac_address()
        dipSwitchId = random.randint(0, 255)

        s = SensorStation(bdAddress, dipSwitchId, ap)
        _sensorStations.append(s)
        for station in _sensorStations:
            data = {"bdAddress": bdAddress, "dipSwitchId": dipSwitchId}
            requestData.append(data)

    headers = {
        "Authorization": json.dumps({"token": ap.token}),
        "User-Agent": "AccessPoint",
    }

    print(colored("create sensor stations", "green"))
    response = requests.post(
        f"{url}/found-sensor-stations", json=requestData, headers=headers
    )
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        ap.sensorStations = _sensorStations
        updateAccessPoint(ap)
        logging.info(f"Sensor Stations created: {ap.sensorStations}")
        for s in ap.sensorStations:
            SensorStations.append(s)
        return ap
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Sensor Stations could not be created")
        return None


# ----------------------------------------------
# ----------------------------------------------


def findSensorStation(bdAddress):
    for i, s in enumerate(SensorStations):
        if s.bdAddress == bdAddress:
            return SensorStations[i]


# ----------------------------------------------
# ----------------------------------------------


def updateSensorStation(s):
    for i, _s in enumerate(SensorStations):
        if _s.bdAddress == s.bdAddress:
            SensorStations[i] = s
            break

    for i, ap in enumerate(AccessPoints):
        for j, station in enumerate(ap.sensorStations):
            if station.bdAddress == s.bdAddress:
                ap.sensorStations[j] = s
                break


# ----------------------------------------------
# ----------------------------------------------


def getSensorStations():
    print(colored("Getting sensor stations", "green"))
    header = {
        "Authorization": json.dumps(
            {
                "token": Admin.token,
                "username": Admin.username,
            }
        )
    }

    response = requests.get(f"{url}/get-sensor-stations", headers=header)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")

        for found in response.json()["sensorStations"]:
            bdAddress = found["bdAddress"]
            s = findSensorStation(bdAddress)
            if s == None:
                return

            s.dipSwitchId = found["dipSwitchId"]
            s.roomName = found["roomName"]
            s.name = found["name"]
            s.connected = found["connected"]
            s.deleted = found["deleted"]
            s.unlocked = found["unlocked"]
            s.sensorStationId = found["sensorStationId"]

            logging.info(f"Sensor Station got/updated: {s}")

            updateSensorStation(s)
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Sensor Stations could not be got/updated")


# ----------------------------------------------
# ----------------------------------------------
def unlockSensorStation(station):
    print(colored("Unlocking sensor station", "green"))
    header = {
        "Authorization": json.dumps(
            {
                "token": Admin.token,
                "username": Admin.username,
            }
        )
    }

    params = {
        "bdAddress": station.bdAddress,
    }

    response = requests.get(f"{url}/get-sensor-stations", headers=header, params=params)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"Sensor Station unlocked: {station}")
        station.unlocked = True
        updateSensorStation(station)
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Sensor Station could not be unlocked")


# ----------------------------------------------
# ----------------------------------------------


def transferData(ap):
    print(colored("Transfering data", "green"))
    headers = {
        "Authorization": json.dumps({"token": ap.token}),
        "User-Agent": "AccessPoint",
    }
    requestData = []
    if len(ap.sensorStations) > 0:
        for i, s in enumerate(ap.sensorStations):
            if (
                i in range(random.randint(0, len(ap.sensorStations) - 1))
                and s.unlocked == True
            ):
                sensorData = []
                for j in range(len(standardSensors)):
                    for k in range(random.randint(0, howManyDataPointsPerSensor)):
                        sensorData.append(
                            {
                                "timeStamp": datetime.now().strftime(
                                    "%Y-%m-%dT%H:%M:%S"
                                ),
                                "value": random.random(),
                                "alarm": "h",
                                "sensor": {
                                    "type": standardSensors[j]["type"],
                                    "unit": standardSensors[j]["unit"],
                                },
                            }
                        )
                s.sensorData = sensorData
                updateSensorStation(s)
                data = {
                    "bdAddress": s.bdAddress,
                    "dipSwitchId": s.dipSwitchId,
                    "connectionAlive": True,
                    "sensorData": sensorData,
                }

                requestData.append(data)

    logging.debug(f"TransferData: {requestData}")
    response = requests.post(f"{url}/transfer-data", json=requestData, headers=headers)
    if response.status_code == 200:
        logging.info(f"Request: {response.request}")
        logging.info(f"Response: {response.text}")
        logging.info(f"Data transferred: {ap}")
    else:
        logging.error(f"Request: {response.request}")
        logging.error(f"Response: {response.text}")
        logging.error("Data could not be transferred")


def creation():
    for i in range(0, userCount):
        user = createUser()
        if user is None:
            logging.error("User already exists")
            continue

        user = login(user)
        # set every 5th user as gardener
        if i % howOftenGardener == 0:
            changeToGardener(user)

    for i in range(0, accessPointsCount):
        ap = createAccessPoints()

    getAccessPoints()

    for i, ap in enumerate(AccessPoints):
        if i % howOftenUnlockAccessPoint == 0:
            logging.info(f"Unlock AccessPoint: {ap}")
            unlockAccessPoint(ap)
            logging.info(f"AccessPoint unlocked: {ap}")
            logging.info(f"Get AccessPoint Token: {ap}")
            getAccessPointToken(ap)

    for ap in AccessPoints:
        if ap.unlocked == True:
            logging.info(
                f"Create sensorStations add them to SensorStation and report them to Backend: {ap}"
            )
            foundSensorStationsForUnlockedAccessPoint(ap)

    for station in SensorStations:
        logging.debug(f"SensorStation: {station}")

    getSensorStations()
    for station in SensorStations:
        if i % howOftenUnlockSensorStation == 0:
            logging.info(f"Unlock SensorStation: {station}")
            unlockSensorStation(station)

    for ap in AccessPoints:
        if ap.unlocked == True and len(ap.sensorStations) > 0:
            logging.info(f"Transfer data: {ap}")
            transferData(ap)

    print(colored("Finished", "green"))


# ----------------------------------------------
# ----------------------------------------------
if __name__ == "__main__":
    # create admin
    createAdmin()
    login(Admin)
    changeToAdmin(Admin)

    print(f"Admin: {Admin.username}, {Admin.password}")
    print(
        colored("Some of this code will fail its a really basic implementation", "red")
    )
    # _url = input("Enter url: ")
    # if _url != "":
    #     url = _url
    # userCount = int(input("How many users do you want to create? "))
    # howOftenGardener = int(input("How often should a user be a gardener? "))
    # accessPointsCount = int(input("How many access points do you want to create? "))
    # howOftenUnlockAccessPoint = int(input("How often should an access point be unlocked? "))
    # howManySensorStations = int(input("How many sensor stations do you want to create? "))
    # howOftenUnlockSensorStation = int(input("How often should a sensor station be unlocked? "))
    # howManyDataPointsPerSensor = int(input("How many data points per sensor do you want to create? "))

    creation()
