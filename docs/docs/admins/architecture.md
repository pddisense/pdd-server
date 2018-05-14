---
layout: docs
title: Private Data Donor Architecture
---

The Private Data Donor platform is made of three components:

  * The API server, offering a REST API;
  * The Chrome extension, installed by the volunteers;
  * The dashboard, used by the analysts to parametrise the platform and access the results.

On the official Private Data Donor instance, the components are hosted are the following addresses:

  * The API server: [https://api.ppd.cs.ucl.ac.uk](https://api.ppd.cs.ucl.ac.uk)
  * The Chrome extension: [https://chrome.google.com/webstore/detail/private-data-donor/ipeekohlgfhagcopnndkgoommcihmdmk](https://chrome.google.com/webstore/detail/private-data-donor/ipeekohlgfhagcopnndkgoommcihmdmk)
  * The dashboard: [https://app.ppd.cs.ucl.ac.uk](https://app.ppd.cs.ucl.ac.uk)
 
## API dashboard

This is the central component, whose goal is to provide read and write access to data managed by the platform. 
The state is persisted into a storage, such as a MySQL database.
The API server exposes a REST API, which is used both by the extension and the dashboard.

The part of the REST API used by the extension is referred as the "public" API, as it is unauthenticated.
The only authentication comes from the client identifier which is included in those public endpoints; the client identifier is hence considered as credentials.
[CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) is enabled on the public API.
This API is quite restricted, and provides the client registration, client ping and sketch update capabilities.

The part of the REST API used by the dashboard is referred as the "private" API, as it is authenticated.
The authentication is provided by the only mean of a [bearer token](https://swagger.io/docs/specification/authentication/bearer-authentication/).
This API offers a large array of capabilities, allowing to dynamically alter the behaviour of the platform by launching new campaigns, viewing clients' activity statistics or exporting results.

In addition to the REST API, the API server also comes with several built-in cron jobs, who are typically executed during the night. 
There are in charge of handling things such as aggregating results.
Please note that we only support a single instance of the API server, as there is no mechanism to elect a leader and ensure that cron jobs run only once.

## Chrome extension

The Chrome extension is a lightweight component whose purpose is to monitor the Web searches made by users, and once a day send relevant data to the API server.
More specifically, the extension sends the total number of searches that were performed (regardless of whether they are being actively monitored) and the number of times each monitored query was performed.
These query counts can be send either encrypted or unencrypted, depending on the configuration of the associated campaign.
Sending unencrypted data means that the server may link a given (monitored) query to a given client (although the default server implementation does not use that information).
Sending encrypted data means that the server is only able to decrypt it once it has received the data of a given number of users (thus preventing individual data linkage).

The Chrome extension runs fully in the background, without requiring the user to take any specific action.
It comes with a FAQ, and provides statistics to the interested user.

## Dashboard

The dashboard is a stateless component providing a Web interface to administrators and analysts.
It allows to create collection campaigns, define the monitored queries and parametrise the platform's behaviour.
It also provide various statistics about the platform's health and allows to export collection results.

Because it is highly sensitive, it comes with a built-in password-based authentication, relying on [JSON Web Tokens](https://jwt.io).
