import mysql.connector
from flask import Flask, request, jsonify
import hashlib
import datetime

def log(message):
	f = open("/var/www/ble_heartrate_reader/ble_heartrate_reader/log.txt", "a")
	f.write(message + " | " +str( datetime.datetime.now()) + "\n")
	f.close()

app = Flask(__name__)
port = int("YOUR_PORT")

user = "YOUR_DB_USER"
password = "YOUR_DB_PASSWORD"

conn = mysql.connector.connect(user=user, password=password, host='127.0.0.1', database='heartratedb')
cursor = conn.cursor()
add_hr_entry_statement = "INSERT INTO hr_entries (hr_value, recording_timestamp, mac) VALUES (%(hr_value)s, %(recording_timestamp)s, %(mac)s)"

@app.get("/running")
def is_running():
	log("get /running")
	return "The server is certainly running"

@app.post("/hr_entry")
def add_hr_entry():
	log("post hr_entry")
	if request.is_json:
		details = request.get_json()
		token = details["token"]
		if hashlib.sha256(bytes(user, "utf-8")).hexdigest() == token["user"] and hashlib.sha256(bytes(password, "utf-8")).hexdigest() == token["password"]:
			hr_entry= {'hr_value':details["hr_value"], "recording_timestamp":details["recording_timestamp"], "mac":details["mac"]}
			cursor.execute(add_hr_entry_statement, hr_entry)
			conn.commit()
			return "added hr", 200
		else:
			return "wrong token", 400
	else:
		return "not cool man", 400


def main():
	app.run("0.0.0.0", port = port)
	cursor.close()
	conn.close()


if __name__ == "__main__":
	main()

