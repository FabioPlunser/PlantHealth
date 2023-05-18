# Instructions for running the access point
There are multiple shell scripts inside this directory available for configuring and running the access point.
## Command overview
| Command  | Description |
| ------------- | ------------- |
| `./configure`  | runs the configuration script for setup and initialization  |
| `./start`  | starts the main routine (cancel with Ctrl+C)  |
| `./start &` | starts the main routine in background |
| `./stop` | stops the main routine running in background |
| `./reset` | stops the main routine running in background, disables automatic restarts and resets any files created during initialization (except the Python virtual environment) |
## Setup for productive deployment
1. Run `./configure`. The script will guide you through the access point initialization and provide all necessary pre-requisites.
In the end, the script will ask you if you prefer to automatically start the access point. Select `yes` and the the main routine will automatically start (and even restart after reboots).
2. Run `./reset`. Main routine execution will be stopped and default state will be restored. Be aware that the database file, configuration file and all logfiles will be removed. Create copies before running `./reset` if you need backups of these files.
## Setup for (personal) development deployment
1. Run `./configure`. The script will guide you through the access point initialization and provide all necessary pre-requisites.
In the end, the script will ask you if you prefer to automatically start the access point. Select `no`. The main routine will not automatically start.
3. Run `./start &` to start the main routine of the access point. Check the logfile `main.log` (by running `tail -30 main.log` or similar) for execution details.
4. Run `./stop` to stop the main routine of the access point. Database files and the logfiles will NOT be deleted.
5. To restore default state run `./reset`.  Database files, configuration file and all logfiles will be deleted.
## Simulating an accesspoint on a Windows device
Running the main routine on a Windows device is possible. Run the routine from Powershell or the standard Windows Terminal. From inside WSL it is usually not possible to access any Bluetooth Adapter and the routine will therefore not work as desired.
1. Create the minimum required `conf.yaml` file where you just specifiy `backend_address: <ENTER BACKEND URL OR IP HERE>`
2. Create a Python virtual environment by running `py -m venv venv` and activate it by running `venv\Scripts\activate`
3. Install Python dependencies by running `pip install -r requirements.txt`
4. Run the main routine by calling `py access_point.py`
5. Execution details are available within `main.log`
6. Stop the main routine by pressing Ctrl+C