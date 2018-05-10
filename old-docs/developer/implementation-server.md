---
title: Server implementation
sidebar: sdaq_sidebar
permalink: implementation-server.html
---

The folder ‘sdaq_server’ contains all the server files. The main script is ‘index.js’, a Node.js script to be used to run the server. It uses a configuration file called ‘server_config.ini’, where you can see and set some important parameter.
The script needs a mongoDB server running on localhost and it will create and use a database called ‘nodetest3’.

The running flow of the server is as follows:
-	initialize variables using the config file, connect to the database and load the db schemas from ‘schemas.js’ (this file can help you to understand the db structure)
-	initialize the cached objects environment. It is used to keep temporarily some useful information in memory
-	start listening for client calls on port 3000
-	from now on, the server answers all the get and post calls received (most of them will be from the clients)

‘order.sh’ is a bash script (in folder ‘scripts’) used to send the get call ‘request_query’ to the server. With this call, the server receives an order to gather clients data in a specified time interval and for a certain group size. After storing the order information in memory, the server starts a session in which it asks the clients to send their encrypted counters, and it stores them in the db. The script takes one argument for the group size (the time interval is hard-coded in the script).

‘aggr.sh’ is the second bash script you need to continue an aggregation session. It sends a get call ‘aggregate’ to the server. The latter is triggered to aggregate and decrypt all the received but not yet aggregated (and decrypted) data. The script takes one argument (0 or 1) to contact the server on localhost or on the ppd server.

‘sum_aggr.sh’ is the last script needed to conclude an aggregation session started by running ‘order.sh’ and decrypted with ‘aggr.sh’. Given an order id as second argument (the first is, again, 0 or 1 to choose the server address), it sums up and stores all the previously aggregated and decrypted data found in the db for that specific order id. The get call on the server is ‘sum_aggregations’, taking the order id as unique argument.

Performance: there is an array, called ‘PERF’, kept in memory (and never stored in the db), that is storing the following times while the server is running: gs-key (group keys retrieving); check-recovery (entire ‘check_recovery’ call time),  group-management (assign users into groups, ‘group_management’ function), aggregate-data (whole aggregate data time, decryption included, function ‘aggregateRawData_consecutive’), decryption-time (stored during consecutive aggregations), aggregation-recovery (taken after an aggregation only if something went wrong and a recovery is needed. It includes decryption time)
The python script ‘perftime.py’ prints the content of PERF variable in the server, calculating the average on all the operations executed with same settings and printing all the details.

## more
The prototype server registers users, monitors available (online) users, accepts data from the clients, manages group assignments, accepts submissions, and decrypts the data. Below is a description of the API calls necessary to understand how the system operates.

3.1. Accepting order requests

The server accepts aggregation from requests made at the address SERVER_ADDRESS/b/request_query.

Following parameters in the GET query are recognized:

- author - string describing the author of the executed query
EXAMPLE: author=admin
- rdata - time period intended for client extensions to seach in the local database, in milliseconds (UNIX timestamps)
EXAMPLE:  rdata=0,1963495565695

- gs - group size for the private aggregation session
if this parameter is not defined, the prototype defaults to a default value
EXAMPLE: gs=2
- ver - the version of Queries of Interest file. If this parameter is not supplied, the prototype defaults to a default value
	EXAMPLE: ver=3
- gm - unsused, obsolete
- qt - unsused, obsolete

An example full request may look as follows:

SERVER_ADDRESS/b/request_query?author=admin&rdata=0,1963495565695&qt=1&gm=1

An example script can be used to create aggregation orders:
scripts/order.sh DESIRED_GROUP_SIZE

After executing that script, the prototype server saves the order details in a database (along with a generated unique ORDER_ID), as well as caches this data in memory. Currently, two seconds after submission, the server enters aggregation mode (assigns users into groups, etc).

In the current version, only "author=admin" can use /request_query method. The prototype has no authentication as this should be deferred to a management console.

3.2 Submitting data

 Clients submits data by performing a POST request to SERVER_NAME/b/submit_sketch.

In the current setting of the prototype system, the post body contains a number of submitted items, such as: encrypted data, non-encrypted counters, raw search queries, group id, device id.

The data is then saved in the database. The fact that the extension submits encrypted and non-encrypted data means that should a private aggregation session fail, the data are still effectively submitted anyway.

A similar method exists for submitting recovery factors


1.3	Aggregating data

An operator can make the server to aggregate data by making a HTTP request to SERVER_NAME/b/aggregate

In the current setting, the method by default attempts to aggregate all data in the database.

A helper script can be used to request the execution of data aggregation: client/aggr.sh

In the current setting, the server attempts to consecutively aggregate all of the saved encrypted data, by executing computeAggregatedRaw function, which calls the function aggregateRawData_consecutive.

The functionality should be transferred to the management console.
A helper script scripts/analyze_aggregate.py demonstrates how the data can be retrieved from the database.

The script is executed as follows:

scripts/analyze_aggregate.py ORDER_ID – for an ID of order


1.4	Server configuration

The system accepts configuration options. These are stored in server/server_config.ini and relate to the server operation. They for example indicate how long a server is waiting for submitted data, etc. The settings relating to time values are expressed in milliseconds.

Some of the values of these configuration settings can be modified by requests made to the server.

The  poll time can be changes by making a request to:

SERVER_NAME/b/poll_time?time=NEW_TIME (in ms)

In the current prototype of the server it’s best to change the configuration in the .ini file and if needed – restart the server. A server restart re-reads the configuration (also: purges in-memory caches). The configuration or a server restart should never be done during an active private aggregation session.

1.5	Recovery process

After submitting encrypted data, clients continuously make requests to SERVER_NAME/b/check_recovery. On the server side, this method checks if all the group members have submitted data within a pre-configured time frame. If at least one client did not submit the data within a predefined time frame, recovery process is triggered. The waiting time should be aligned to the client polling time, the frequency of client requests to the check_recovery, the time it takes for the client to compute and submit the data.

If the server signals that a recovery is needed, the clients compute recovery factors and submit them to the server. On the server side, decrypting data supports the use of recovery factors.

## Roadmap

-	Deploy a management console simplifying the issuing of requests to run private aggregation sessions, address access control needs, make it straight-forward to decrypt data and recover results, later also present the results and possibly study the difference between results obtained from different aggregation sessions 
-	Further simplify the connection API further by rewriting the system to use WebSockets which would improve latency
