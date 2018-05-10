---
title: Client architecture
---

Clients are providing data to the server.
To do so, clients monitor the changes to chrome’s browser history object.
After the client detects a new history item and verifies that the new item is a google search engine query, the query is saved to a browser’s local storage.

Clients participating in the Private Data Donor system periodically submit data relating to search engine use patterns.
In order to do so, clients implement client logic, which guides how the clients perform specific actions.
The main task for the clients is to transform array of counters (corresponding to the numbers of times the user performed a specific search engine query).
Client’s primary task is to submit data to the server.
Specifically, client can:
* Generate cryptographic material (private and public keys), as required by the privacy-preserving protocol
* Obtain a list of Queries of Interest from the server
* Prepare data for submission. Populate Data arrays with counters
* Encrypt data (counters)
* Contact the server to receive instructions (i.e. for a a new version of QoI’s, checking if is waiting for data)

Clients use a cryptographic library implementing the privacy-preserving protocol.
Core functions of the client logic follow.

## Periodic ping to the server
The client periodically (currently 5 seconds) contacts the server to receive instructions, such as the following:
* change the frequency of pinging the server (POLL TIME)
* obtain new version of QoI file
* send encrypted data

If upon the client’s request, the client detects that the server is in aggregation mode, the client immediately extracts cryptographic public keys of group members from the server’s response and starts preparing the data and executing `computeEncryptedRaw` function.

## Client operation
The function ping server runs continuously on the client.
It is ran by the SCHEDULER function. When a client detects that the server is in aggregation mode, the process of submitting data commences.
1. `runSubmitSketch` function is executed. It executes `prepareCounterDataHistory`.
2. `prepareCounterDataHistory` queries the local HistoryLog (cached search engine queries) and prepares counters corresponding to the times individual term, a pair of terms, or an exact queries have been seen in the HistoryLog, corresponding to a period of time for which the server is asking
3. `encryptThenSubmit` is executed. It’s role is to use `computeEncryptedRaw` function that performs encryption. The encrypted data are then composed in a JSON request.
4. Function `submitSketch` actually submits the data.
5. Function `checkRecovery` starts the operation 3 seconds after the server confirms a HTTP request has been received
6. If `checkRecovery` detects that an aggregation is complete, it stops the operation and the extension returns to the point of pinging the server and waiting for aggregations.
7. If `checkRecovery` detects that a recovery is needed, `submitRecoveryFactors` submits the recovery data, it stops the operation and the extension returns to the point of pinging the server and waiting for aggregations.

## Preparing a response
During tests, arrays of counters are initialized with synthetic data.

## Computing encrypted sketch – obsolete
Function `computeSketchData` working on the client’s side encrypts data meant to be submitted to a server.
The inputs are three arrays: DataRaw1, DataRaw2, DataRaw3.
Those arrays contain counters corresponding to numbers of times a given search term has been found in client’s local database.
A step-by-step description of operations performed by `computeSketchData` is as follows:
1. The client generates cryptographic shares, according to the cryptographic protocol
2. Three Count-Min-Sketch structures are created for counter data arrays DataRaw1, DataRaw2, DataRaw3: cms1, cms2, cms3
3. cms1, cms2, cms3 are populated with data from DataRaw1, DataRaw2, DataRaw3
4. for each sketch, its *underlying raw representation* (i.e. an array of numbers) is converted to JSON , ”*blinding* factors” (according to the cryptographic protocol) are generated, and the underlying raw representations of sketches are encrypted; three encrypted sketches ecms1, ecms2, ecms3 are computed as a result.
5. each element of encrypted array is converted to string representation

The function returns the data in JSON format.
The output from this function is submitted to a server.
After this submission, the client periodically checks if recovery process needs to be initiated.
**This function is not used in practice.
The client extension is using `computeEncryptedRaw`.**

## Computing encrypted sketch (version 2) – obsolete
Function `computeEncryptedSketch` working on the client’s side encrypts data meant to be submitted to a server. The input is an array: DataRawi.
The array contain counters corresponding to numbers of times a given search term has been found in client’s local database.
First, the client generates cryptographic shares, according to the cryptographic protocol, and then a following operation takes place:
1. Count-Min-Sketch structure is created for counter data array DataRawi: cmsi
2. cmsi, cms2, cms3 are populated with data from DataRawi
3. for a sketch, its *underlying raw representation* (i.e. an array of numbers) is converted to JSON, ”*blinding factors*” (according to the cryptographic protocol) are generated, and the underlying raw representation of a sketch is encrypted; encrypted sketch ecmsi is computed as a result.
4. each element of encrypted array is converted to string representation

The function returns the data in JSON format.
The output from this function is submitted to a server.
After this submission, the client periodically checks if recovery process needs to be initiated.
**This function is not used in practice.
The client extension is using `computeEncryptedRaw`.**

## Computing encrypted raw
Function `computeEncryptedRaw` working on the client’s side encrypts data meant to be submitted to a server.
The input is an array: DataRawi.
The array contain counters corresponding to numbers of times a given search term has been found in client’s local database.
First, the client generates cryptographic shares, according to the cryptographic protocol, and then a following operation takes place:
1. DataRawi are populated with data from DataRawi
2. DataRawi array is converted converted to JSON, ”*blinding factors*” (according to the cryptographic protocol) are generated, and the DataRawi is encrypted; encrypted array EncDataRawi is computed as a result.
3. each element of encrypted array is converted to string representation

The function returns the data in JSON format.
The output from this function is submitted to a server.
After this submission, the client periodically checks if recovery process needs to be initiated.

## Recovery process
When the client receives an instruction to enter in recovery process, the client computes recovery factors according to the protocol and submits them to the server.
