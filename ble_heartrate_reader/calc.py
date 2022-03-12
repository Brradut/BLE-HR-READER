import mysql.connector
import numpy

user = 'user1'
password = 'hr1password'
conn = mysql.connector.connect(user=user, password=password, host='127.0.0.1', database='heartratedb')
cursor = conn.cursor()

query = "SELECT hr_value FROM hr_entries WHERE mac like 'F6%'"

cursor.execute(query) 

hr_values = [hr_value[0] for hr_value in cursor]

for i in range(0, len(hr_values)):
	if hr_values[i] < 0:
		hr_values[i] = 256 + hr_values[i]


processed = [60/x for x in hr_values]

reprocessed = [sum(processed[i:i+5])/5 for i in range(0, len(processed), 5)]

print(hr_values)

print(numpy.std(processed), "pe 1 minute(not really)")
print(numpy.std(reprocessed), "pe 5 minute(not really)")
