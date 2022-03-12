#!/var/www/ble_heartrate_reader/env/bin/python
import sys
import logging
logging.basicConfig(stream=sys.stderr)

activate_this = '/var/www/ble_heartrate_reader/env/bin/activate_this.py'
with open(activate_this) as file_:
    exec(file_.read(), dict(__file__=activate_this))


sys.path.insert(0, '/var/www/ble_heartrate_reader')
sys.path.insert(0, '/var/www/ble_heartrate_reader/ble_heartrate_reader')


from ble_heartrate_reader import app as application

