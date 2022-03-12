# Flask app for saving data to mysql database

A basic Flask app exposing a REST API to the web. Was ran using mod_wsgi on an apache server on a hosted virtual machine. The initial intent was collecting data from multiple, distinct users, and so a local database would have been bothersome, and a local server wouldn't have sufficed. 

The data is stored in a locally ran mysql database. Make sure to change the \_\_init\_\_.py file to use the username and password of your own database (as well as the port).
