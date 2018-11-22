---
title: Monitoring
---

# Monitoring PDD

Monitoring the API server is critical to ensure that it is continuously running and behaving as expected.
Note that the dashboard can be monitored using the same techniques that the ones presented on this page, although it is usually considered less critical (as the dashboard is not responsible for collecting the data).

## Logs

All the log messages are written to the standard output.
When running the API server with Docker, this allows to easily inspect logs later by using the `docker logs` command. 
By default the logging verbosity is INFO, but you can change it with the `-log_level` flag.
For example, to include debug messages, you can pass the `-log_level=DEBUG` flag when starting the server. 

## Metrics

The API server exposes a lot of metrics allowing to inspect its status.
They can be exported as several formats.

First, all metrics are available through the admin HTTP server, available by default on port 9990.

```
curl -s localhost:9990/admin/metrics.json
```

The admin HTTP server also exposes several interfaces to graphically visualise the values of the metrics.
You can find more information about this in the [documentation for Twitter Server](https://twitter.github.io/twitter-server/Features.html#metrics).

We also provide built-in support for exporting metrics to Datadog.
This is enabled with the `-datadog_server` flag which has to be configured to designate the Datadog agent, usually `-datadog_server=127.0.0.1:8125`.
Note that Finagle metrics have flat names, e.g., "status/200", which creates a high cardinality of metric names, while Datadog expects a small number of metric names with tags associated, e.g., "http.requests" with a "status" tag.
The Datadog integration does its best to translate the most common metric names into a format suitable for Datadog.
The `ENVIRONMENT` environment variable is always included as a Datadog tag specifying the current environment in which the server is running, e.g., "devel", "stating" or "production".

## Errors

It may happen that a server generates exceptions.
Although it won't usually crash it, it still indicates a bad behaviour that should be corrected.
Exceptions can be automatically forwarded to [Sentry](https://sentry.io), if the `SENTRY_DSN` environment variable is configured.
[Sentry documentation](https://docs.sentry.io/clients/java/config/) contains more information about how to configure Sentry.
The `ENVIRONMENT` environment variable is always included as a Sentry tag specifying the current environment in which the server is running, e.g., "devel", "stating" or "production".
