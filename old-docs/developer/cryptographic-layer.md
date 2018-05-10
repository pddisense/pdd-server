---
title: Cryptographic layer
sidebar: sdaq_sidebar
permalink: cryptographic-layer.html
---

Private Data Donor is a system enabling to retrieve aggregated data from a group of clients in a way that preserves individual client’s privacy.
Privacy protection is facilitated by the application of a privacy-preserving cryptographic protocol.
The protocol allows to retrieve aggregated data from a group of users in a way that makes it impossible to link data with a particular member of the group.
The protocol allows decryption of aggregated (summed) data submissions from a group of users provided by all participants of the group.
If even a single user won’t submit, the data cannot be decrypted.
The protocol’s privacy layer is based on additively homomorphic Elliptic Curve type of of El-Gamal cryptosystem and is defined in [this research paper](http://arxiv.org/pdf/1508.06110.pdf).
The protocol that is used encrypts an array of numbers representing a Count-Min structure, although the current system [is not using this structure](https://sites.google.com/site/countminsketch/).

## Overview
Users being part of the system need to *register*.
This is the process when cryptographic public keys are generated on client’s computer and sent to the server.
From this moment, the server is able to detect when a registered user is online, as registered users contact the server periodically.

A *private aggregation session* is the entire process of instructing clients to submit encrypted data to a server.
Each private aggregation session starts at the operator’s request for data from a particular period of time.
This is done via sending an *interrogation request* with a *specific configuration*.

Clients participating in a private aggregation session are assigned in groups of size `GROUP_SIZE`.
Clients that are assigned to a group receive group keys.
Keys are used to prepare a response, i.e., compute an encrypted response according to the privacy-preserving protocol.
After a response is prepared, it is submitted to the server.

Subsequently, clients enter in a phase that establishes if a *recovery process* is needed.
A recovery process is sparked if at least one member of a group did not submit data.
If all users in a group submitted their data, the private aggregation session for this group was successful and the process finishes.
If a recovery is needed, clients receive an appropriate instruction stating that a number of users did not submit a response.
Clients then compute *recovery factors* and submit them to the server; this ends the recovery process and the private aggregation session is concluded.
When all the submissions have been performed correctly, the server can *decrypt* the data.

The following sections contain more details about each phase.

## Registration
Users wanting to be part of the system download a client application.
Upon first installation, the application generates cryptographic material (public/private keys) and a unique `device_id`.
The device identifier and the public key are then sent to the server; the server saves them in a database and then a normal client operation follows.
The client then fetches a file containing *Queries of Interest* (QoI), i.e., search engine queries that are of interest to us.

## Interrogation request
The server’s operator schedules a private aggregation session by sending an interrogation request to the server, specifying:

1. the period of time the operator is interested in;
2. the group size used by the cryptographic protocol.

When an interrogation request is sent to the server, a private aggregation session can start.

## Group forming
Group forming is the process of assigning users into groups identified by group identifiers.
For a number of N clients and a server setting for a group size of `GROUP_SIZE`, `N/GROUP_SIZE` groups are formed.
Only recent online (i.e., those who contacted the server in the last 10 minutes) users who have previously been registered in the system can take part in an aggregation session.
The composition of groups (mapping between user identifiers and group identifiers) is saved inside a database and a hash table.
At the same time, the public keys of the users assigned to a group are stored inside an in-memory hash structure.
When at least one group is formed, the server enters **aggregation mode**; the server starts providing group keys to requesting users.

## Aggregation mode
Client applications periodically contact the server to receive instructions.
The server periodically (currently triggered by a script executing in the background) triggers a private aggregation session.

After the client detects an active private aggregation session, it obtains from the server the public keys of all participants of its group.
Upon receiving information about the details of the aggregation (group members' public keys, period of time of interest), each client prepares an answer.
Three `DataRaw1`, `DataRaw2` and `DataRaw3` arrays are initialized with numbers – counters relating to the numbers of times a particular search engine query has been performed.
Specifically, our system counts the numbers of times an exact QoI has been made, the numbers of times a pair of keywords appears in a query, and the numbers of times that only a certain keyword appeared in a search engine query - search query data is stored in a client’s local storage and is never transmitted to the server in a clear-text form.

The raw arrays are then encrypted using a cryptographic protocol.
The final three arrays with encrypted data are sent to the remote server.
When the server receives an encrypted data, following actions are made:

  - the data are saved in a database for a later decryption.
  - the user who submitted is marked with a ”submitted” flag  

The process of recovery checking commences.

## Fail-safe method. Recovering from incomplete group submissions
If, for any reason, some `n` users from a `GROUP_SIZE` of users did not submit their encrypted data, it is impossible for the server to decrypt the answers and obtain an aggregation of data submitted by users.
For this reason, the system provides a recovery scheme.

After submitting encrypted data, the user is very frequently (currently 2 seconds) contacting the server to learn if all the group participants submitted data.
If after some predefined amount of time (currently 30 seconds between a user engaged in recovery checking process has submitted data, and the time of the execution of recovery check method) at least one group participant did not submit their data, the cryptographic protocol’s recovery aspects may be executed. Users are instructed to submit recovery factors in order to fulfill the requirements of the protocol.

## Decrypting data
A decryption function decrypts the data for all group submissions identified with `group_id`’s that do not have their data decrypted yet.
It is also possible to decrypt encrypted data for a particular `group_id`.
The decrypted data is saved in a database.

## Final answer
The final outcome of a private aggregation session is the summation of `DataRaw1`, `DataRaw2`, `DataRaw3` arrays.

Suppose there is a group of two clients.
Each of them have following raw data arrays relating to number of times a certain search engine query has been made: `DataRaw1 = [1, 2, 3, 4], DataRaw2 = [5, 6], DataRaw3 = [7, 8]` (i.e. those two clients performed the same number of queries).
After decrypting, the server obtains the following: DataRaw1 = [2,4,6,8],DataRaw2 = [10,12],DataRaw3 = [14,16], i.e. exact summation of two arrays stored on the client’s system.
