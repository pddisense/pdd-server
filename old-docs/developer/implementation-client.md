---
title: Client implementation
sidebar: sdaq_sidebar
permalink: implementation-client.html
---

Chrome extension implements the whole client logic. Additionally, a browser extension is a vehicle for detecting google search engine queries and storing those queries in a local browser storage. Each time the user performs a google search engine query, the search query is saved in the local storage, time stamped.
Browser extension is capable of asking the user to provide demographic de- scription. The user can update this data.

In the folder ‘client’ you can find the ‘client.js’ script, a Node.js script to be used to run one client. It uses a configuration file named ‘client_config.ini’.
The script takes two optional arguments: USERID, a name for the user that will be created (or restored if already existing) and LIMIT_SESSION, an integer number representing the number of times that the client will ping the server before killing itself. If not given, the two arguments will be: ‘FAKE’ and ‘false’ (meaning that there is no limit).
Once you run the ‘client.js’ script, it stores in the folder ‘LocalData/USERID’ his own information, so it can use them again if you stop and start it again.

The running flow of the client is as follows:
-	initialize variables using the arguments and the config file
-	try to recover the user data or create them from scratch (function ‘recoverUserData’)
-	register the user to the server (function ‘runRegisterKey’)
-	call the function ‘setTimeout’ on the function ‘SCHEDULER’ using time interval ‘SchedulerPollInterval’ (5000ms hard-coded in the client at the beginning and then updated by receiving it from the server)
-	from now on using the same time interval (it changes only if the server sends a new one) the client contacts the server to check if there is an aggregation session running, in which case it has to encrypt its data with a key received from the server, and then send the encrypted data to the server

‘Tester.sh’ is a bash script useful to run more than one client at the same time. It takes two arguments ‘n’ and ‘k’, where ‘n’ is the number of clients and ‘k’ the number used for the name of the first of them (the others will have subsequent numbers). ‘Ctrl+C’ to kill the script with all the running clients. Every client will store his own logs in folder ‘log_test’.

Performance: look at variable ‘PERF’ and all the ‘Timer framework’ section. Now clients are sending to the server two time values: key-sketch (from keys receival to encrypted data submission); compute-sketch (to compute the three raw encryptions). The server is saving these infos in the db (collection ‘measurementrecords’).
To run an extra experiment in which the encryption is repeated more than one time and every execution time is saved, uncomment the code in ‘client.js’, function ‘runSubmitSketch’, lines 577-78,586-593. This will allow the client to run the encryption 1000 times, saving the results in ‘/encryption_perf_data/gs-encryption_computation_times_raw.txt’, where the folder must already exist and ‘gs’ is the size of the group during the process. You can change both the number of experiments and the file where the results are stored.


## Extra tips

-	The old version of the system was using a probabilistic data structure called ‘count-min sketch’. Now this has been removed from it, but there are still files, methods and variables named with words like ‘sketch’ or ‘cm’ (count-min). Also there is a bunch of unused old functions related to the previous implementation.
-	Collected data in the system are of three types: single words (or terms), pairs of terms, exact queries. That’s why every piece of code on both client and server that manages data (to encrypt, collect, aggregate or decrypt them) is working with 3 array variables. You can give a look at the current data sets in the json file ‘sample9k.json’, inside the server folder. There are 9291 exact queries, 13539 pairs of terms and 2855 terms.
-	Every client simulates its own searched queries sending the following counters to the server: 100 for the first 5 terms (0 for every other), 10 for the first 5 pairs (0 every oter), generate a random sample of 100 queries from the exact queries set (stored in ‘exact9k.json’ on the client side) using a probability distribution (the function doing this is ‘generateRandomSample’ in ‘ client.js’, and the distribution is stored in ‘weights9k.json’). To change this, look at the function ‘runSubmitSketch’ in ‘client.js’.
-	About ‘AGGREGATION_INTERVAL’ config parameter on the server side: the system is now working in this way: at the beginning this is off; then when an order is placed (‘request_query’ is called) an aggregation session is started and at the end of it the interval is activated. The problem is that if there are no new orders when it automatically triggers it won’t start the session, but when you place an order it will trigger the aggregation too. So, now there is no point to use this variable. To use it, ‘cacheOrderRequest’ function in the server should be modified to not trigger the aggregation session (it does it as last thing before exit the function)
-	‘RECOVERY_CHECK_INTERVAL’ is a variable hard-coded in ‘client.js’. It is used (by adding a small random extra time) in function ‘checkRecovery’. It represents the time interval for the client to send a ‘check_recovry’ request to the server. It goes on until the server tells him to stop. Currently it is set to 2seconds + random.
-	Currently the person who runs the server is the one who can decide when send orders, aggregate data and so on. For the future, an administration console is needed, to allow an administrator to easily decide when to execute some operations or change settings of the system while running (e.g. call ‘request_query’, ‘aggregate, ‘sum_aggregations’, change time intervals like ‘POLL_TIME’ or ‘AGGREGATION_INTERVAL’, ecc)

## More
Upon installation of the prototype client generates crucial data, such as the cryptographic material, unique ID, and other necessary information. The cryptographic computation layer is resolved by CMSprotocol.js, a JavaScript module implementing the key functions. The library can generate keys (generateKey function) or encrypt data (computeEncryptedRaw function), according to the privacy-preserving protocol.

The encrypted data is formed and submitted in the following form:

```json
					{
						d1: raw_encrypted_data1,
						d2: raw_encrypted_data2,
						d3: raw_encrypted_data3
					};
```

where d1 corresponds to the individual query terms (words), d2 corresponds to the pairs of words, and d3 corresponds to the exact queries.

The cryptographic data are saved locally in the browser database.

The extension then contacts the server to register the user. The extension is submitting the public key as well as the client ID.

The extension obtains the latest version of the Queries of Interest file by making a request to SERVER_NAME/b/patterns?v= - the parameter ‘v’ is a version of queries of interest advertised by configuration data that the extension receives by periodic pings to the server (HTTP request to SERVER_NAME/b/gs/). The code responsible for this phases is placed in fetchKeywords() function.  The Queries of interest is an indexed document database. The order of the elements (E.g. queries of interest) in the lists matters – as this is the index of the counter related to the query of interest. Currently, this file is called “sample9k.json” and needs to be placed in the directory where the server files are located. “Sample9k.json” contains proprietary data and should be protected. The format:
 {"terms": ["term1", "term2", …], “pairs: [“termX,termY”, …], “exact”: [“termZ termT”]}

Immediately after the installation, the extension allows the user to include demographic data – the data is submitted to the server.

The client extension monitors the web history object of the browser. As soon as a new item is added to the browser history, the extension checks if it’s a Google search query. If a search query is identified, this search query is saved in the browser database.

The extension is periodically contacting with the server (in the current phase this happens every few seconds) to receive instructions such as:

- the new Queries of Interest file
- information about the fact whether a private aggregation session is active (the server is in aggregation mode)
- the polling time (how frequently should the client be contacting the server)

As soon as the extension detects that a private aggregation session is held (server is in aggregation mode) and the server is accepting submissions, the client extension computes the data. This is done based on the information stored in the local database of found search query terms. The data is encrypted according to the privacy-preserving protocol.

The extension currently submits:

-	Encrypted counters, corresponding to the numbers of times a given phrases such as search query was found
-	Non-encrypted counters
-	Raw detected search queries

This data corresponds to the period of time indicated by the server.

After submitting the data, the extension continuously connects to the server to see if all the other clients (being members of a group -according to the privacy-preserving protocol) have also submitted data. If all group members submitted the data, private aggregation session has been completed. Following the submission of data, the client (continuously) checks the status of a private aggregation session by performing a HTTP GET request to SERVER_NAME/b/check_recovery. This method signals if the private aggregation session has completed successfully, or – if It did not complete successfully - whether the client should send recovery factors. If a recovery mechanism is triggered, the extension is computing recovery factors (based on the data obtained by the server) and submits those to the server (SERVER_NAME/b/submit_recovery).
