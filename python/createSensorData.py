import requests
import random
import string
import uuid
from termcolor import colored
import json
import datetime
import logging

import randomGenerator


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
        "type": "AIRQUALITY",
        "unit": "%",
    },
    {
        "type": "BATTERYLEVEL",
        "unit": "%",
    }
]

sensorStations = [{"bdAddress": "00:A0:50CC:A5:DD", "dipSwitchId": 112}]


def transferData(count):
    print(colored("Transfering data", "green"))
    headers = {
        "Authorization": json.dumps({"token": "965551fb-07fb-4dae-9b11-084d16b6a523"}),
        "User-Agent": "AccessPoint",
    }
    requestData = []

    sensorData = []
    for i in range(len(sensorStations)):
        for j in range(len(standardSensors)):
            for k in range(random.randint(0, count)):
                value = random.random()
                rand_hours = random.randint(0, 2)
                rand_days = random.randint(0, 2)
                now = datetime.datetime.now()
                rand_time = now - datetime.timedelta(hours=rand_hours)
                sensorData.append(
                    {
                        "timeStamp": rand_time.strftime("%Y-%m-%dT%H:%M:%S"),
                        "value": value,
                        "alarm": "h",
                        "aboveLimit": value > 0.8,
                        "belowLimit": value < 0.2,
                        "sensor": {
                            "type": standardSensors[j]["type"],
                            "unit": standardSensors[j]["unit"],
                        },
                    }
                )
        data = {
            "bdAddress": sensorStations[i]["bdAddress"],
            "dipSwitchId": sensorStations[i]["dipSwitchId"],
            "connectionAlive": True,
            "sensorData": sensorData,
        }

    requestData.append(data)

    logging.debug(f"TransferData: {requestData}")
    response = requests.post(
        f"http://localhost:8080/transfer-data", json=requestData, headers=headers
    )

    print(response.status_code)
    print(response.text)


if __name__ == "__main__":
    transferData(50)
