# PTS
It is the server part of the project of my course 'Principles of Distributed Database Systems'. 
It collects, stores and processes timeseries data. Timeseries data include gyro data, GPS data, light sensor data and so on. 
They are collected by the application installed on users' mobile phone and uploaded to the server. 
Finally, the system will generate personal daily(or weekly) reports for users and users can view them on the website.

The application is written in Kotlin and is mainly based on Spring Boot. 
It uses [IoTDB](https://iotdb.apache.org) as the database to store timeseries data, 
and uses MySQL to store relational data. The data analysis codes are written in Python.



