---
layout: docs
title: Installing the platform
---

This page explains how to deploy the API server, the Chrome extension and the dashboard.

## Deploying the API server

The easiest way to deploy the API server is by using the Docker image published on Docker Hub: [https://hub.docker.com/r/pddisense/pdd-server/](https://hub.docker.com/r/pddisense/pdd-server/).
By default, two ports are used:

  * Port 8000, which provides the main HTTP interface.
  * Port 9990, which provides [an HTTP administrative interface](https://twitter.github.io/twitter-server/Admin.html).

Both interfaces can be bound to another host/port by using the `-http.port=` and `-admin.port` flags. 
Both take as argument a string formatted like `host:port`, where the host can possibly be left empty (but the colon still has to be included).
The administrative interface should *not* be publicly accessible and remain behind a firewall, while the main interface should be exposed on the Internet. 

By default, the server uses an in-memory storage, which is by definition not persistent.
While this may be useful for local testing, a production setup requires to use a proper persistent storage.
For now, the only implementation is a MySQL storage, which is enabled with the `-storage.type=mysql` flag. 
Then, the `-storage.mysql.user`, `-storage.mysql.pass`, `-storage.mysql.database` and `-storage.mysql.server` flags can be used to override, respectively, 
the MySQL username, password, database name and the MySQL server address.
By default, it connects to a database named `pdd` on a local server listening on port 3306, as the `root` user and no password.

The private endpoints are secured by the means of an access token, specified with the `-api.access_token` flag.
By default, a random access token is generated and printed in the standard output.

As a reference, the following command is used to start the production API server:

```bash
docker run \
  --net host \
  --detach \
  --restart=always \
  --env 'SENTRY_DSN=https://<public>:<private>@sentry.io/302347' \
  --env ENVIRONMENT=production \
  --env ROLE=server \
  --name pdd-server \
  pddisense/pdd-server \
    -storage.type=mysql \
    -storage.mysql.user=pdd \
    -storage.mysql.pass=<mysql password> \
    -metrics.type=datadog \
    -api.access_token=<access token> \
    -http.port=:8000 \
    -admin.port=:9000
```

## Deploying the dashboard

The easiest way to deploy the API server is by using the Docker image published on Docker Hub: [https://hub.docker.com/r/pddisense/pdd-dashboard/](https://hub.docker.com/r/pddisense/pdd-dashboard/).
By default, two ports are used:

  * Port 8001, which provides the main HTTP interface.
  * Port 9990, which provides [an HTTP administrative interface](https://twitter.github.io/twitter-server/Admin.html).

In the same manner than for the API server, both interfaces can be bound to another host/port by using the `-http.port=` and `-admin.port` flags. 
Both take as argument a string formatted like `host:port`, where the host can possibly be left empty (but the colon still has to be included).
The administrative interface should *not* be publicly accessible and remain behind a firewall, while the main interface should be exposed on the Internet.

The dashboard provides all its features by communicating with the API server.
The only required information is the `-api.access_token` information, which specifies the access token used to authenticate to the API server.
It should match the `-api.access_token` flag provided to the API server (or the value displayed on the standard output when the latter started).
The address of the API server can be specified with the `-api.server` flag, and if SSL is enabled you should use the `-api.ssl_hostname` to specify the matching hostname.
By default, it attempts to contact a local API server listening on port 8000.

By default the dashboard does not require authentication.
If the dashboard is publicly accessible, **it is highly recommended that you enable the authentication**.
This can be done by defining the `-master_password` flag to some secret value.
This password will be then used by the users to login interactively to the dashboard.
This value is by design different from the access token; the access token should not change very often, while the master password may possibly change as often as you need it to be.

As a reference, the following command is used to start the production dashboard:

```bash
docker run \
  --net host \
  --detach \
  --restart=always \
  --name pdd-dashboard \
  --env ENVIRONMENT=production \
  --env ROLE=dashboard \
  pddisense/pdd-dashboard \
    -api.access_token=<access token> \
    -master_password=<master password> \
    -admin.port=:9001 \
    -http.port=:8001
```
