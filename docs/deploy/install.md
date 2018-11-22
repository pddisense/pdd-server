---
title: Installing
---

# Installing PDD

Private Data Donor is made of three different components: an API server, a Web dashboard and a Chrome extension.
The API server is a mandatory component with which the extension communicates, while the dashboard is an optional component providing an intuitive Web UI for administrators and analysts.
If you want to get a deeper understanding of the architecture, you can read [the dedicated page](../contribute/architecture.md) in the developer guide.
 
This page explains how to deploy the API server and the Web dashboard using Docker.

## Installing Docker

The PDD platform is packaged as a Docker image to ease its installation on heterogeneous platforms.
Consequently, you can install PDD on any platform supported by Docker, i.e., Linux, Mac OS and Windows.
First of all, please follow [Docker's documentation](https://docs.docker.com/install/) to install the Docker Engine on the target machine.

Please check that Docker is correctly installed by running `docker version`.    

## Deploying the API server

The API server is published as a Docker image on Docker Hub: [https://hub.docker.com/r/pddisense/pdd-server/](https://hub.docker.com/r/pddisense/pdd-server/).
You can retrieve it by running 
```bash
docker pull pddisense/pdd-server
```

The server accepts a lot of options, which you can visualise by running:
```bash
docker run pddisense/pdd-server -help
```

### Networking

By default, two ports are used:

  * Port 8000, which provides the main HTTP interface.
  * Port 9990, which provides [an HTTP administrative interface](https://twitter.github.io/twitter-server/Admin.html).

Both interfaces can be bound to another host/port by using the `-http.port=` and `-admin.port` flags. 
Both take as argument a string formatted like `host:port`, where the host can possibly be left empty (but the colon still has to be included).
The administrative interface should *not* be publicly accessible and remain behind a firewall, while the main interface should be exposed on the Internet. 

### Storage
By default, the server uses an in-memory storage, which is by definition not persistent.
While this may be useful for local testing, a production setup requires to use a proper persistent storage.
For now, the only implementation is a MySQL storage, which is enabled by specifying the server address with tge `-mysql_server` flag, e.g., `-mysql_server=localhost:3306`. 
Then, the `-mysql_user`, `-mysql_password` and `-mysql_database` flags can be used to override, respectively, 
the MySQL username, password and database name.
By default, it connects to a database named `pdd` as the `root` user and no password.

### Security
The private endpoints are secured by the means of an access token, specified with the `-api.access_token` flag.
By default, a random access token is generated and printed in the standard output.
If you want to connect the dashboard to the API server later on, you may wish to generate your own access token and provide it a an option.

### Example
As a reference, the following command is used to start the production API server:

```bash
docker run \
  --net host \
  --detach \
  --restart=always \
  --env 'SENTRY_DSN=https://<public>:<private>@sentry.io/302347' \
  --env ENVIRONMENT=production \
  --name pdd-server \
  pddisense/pdd-server \
    -mysql_server=localhost:3306 \
    -mysql_user=pdd \
    -mysql_password=<mysql password> \
    -datadog_server=127.0.0.1:8125 \
    -geocoder=maxmind \
    -api.access_token=<access token> \
    -http.port=:8000 \
    -admin.port=:9000
```

## Deploying the dashboard

The API server is published as a Docker image on Docker Hub: [https://hub.docker.com/r/pddisense/pdd-dashboard/](https://hub.docker.com/r/pddisense/pdd-dashboard/).
You can retrieve it by running 
```bash
docker pull pddisense/pdd-dashboard
```

The server accepts a lot of options, which you can visualise by running:
```bash
docker run pddisense/pdd-dashboard -help
```

### Networking
By default, two ports are used:

  * Port 8001, which provides the main HTTP interface.
  * Port 9990, which provides [an HTTP administrative interface](https://twitter.github.io/twitter-server/Admin.html).

In the same manner than for the API server, both interfaces can be bound to another host/port by using the `-http.port=` and `-admin.port` flags. 
Both take as argument a string formatted like `host:port`, where the host can possibly be left empty (but the colon still has to be included).
The administrative interface should *not* be publicly accessible and remain behind a firewall, while the main interface should be exposed on the Internet.

### Security
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

### Example
As a reference, the following command is used to start the production dashboard:

```bash
docker run \
  --net host \
  --detach \
  --restart=always \
  --name pdd-dashboard \
  --env ENVIRONMENT=production \
  pddisense/pdd-dashboard \
    -api.access_token=<access token> \
    -master_password=<master password> \
    -admin.port=:9001 \
    -http.port=:8001
```
