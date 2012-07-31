SmartTrace
==========

This is a spatio-temporal similarity search framework, coined SmartTrace. Our framework can be utilized to promptly answer queries of the form: “Report the objects (i.e., trajectories) that follow a similar spatio-temporal motion to Q, where Q is some query trajectory.” SmartTrace, relies on an in-situ data storage model, where spatio-temporal data remains on the smartphone that generated the given data, as well a state-of-the-art top-K query processing algorithm, which exploits distributed trajectory similarity measures in order to identify the correct answer promptly.

Credits:
========

+ Data Management Systems Laboratory, University of Cyprus
+ Costantinos Costa (http://www.cs.ucy.ac.cy/~cs07cc6/)

How to run a scenario on SmartTrace:
====================================

You should deploy the android application (or .apk) on any emulator or smartphone with android OS. 
The application is compatible with all the versions of android OS. The SmartTrace application has two parts, SmartTrace using GPS and SmartTrace using RSS. The two parts have separated settings, the settings’ page allows to the user to configure the setting for the connection each server respectively.

+ Step  1: 
You should download and run the Server into a specific port. There is a configuration file to configure some parameters.
E.g.
#Port for the requests
port 65001
#Minimum number of clients
client 2
#Interval time between the request and the reply
interval 2000
+ Step 2:
The ip and port of the server should be supply to the application.
+ Step 3:
Connect all the mobile phones to the server and run a scenario.
