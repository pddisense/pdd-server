# Private Data Donor

[![Build Status](https://travis-ci.com/pddisense/pdd-server.svg?branch=master)](https://travis-ci.com/pddisense/pdd-server)

This repository contains the source code for the PDD server.
More specifically, it provides an API server, with which the clients interact, and a dashboard, providing a user interface to analysts.
The server components are written in Scala and rely on [Finatra](https://twitter.github.io/finatra/) to provide HTTP services.

## Build
To build the server, you will need [Java 8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), [Scala 2.12.x](https://www.scala-lang.org) and [SBT ≥ 0.13.17](https://www.scala-sbt.org).
Alternatively, you can use the `sbt` wrapper script included at the root of the repository.

First clone the repository:
```bash
git clone git@github.com:pddisense/pdd-server.git
cd pdd-server
```

Then test and build the server:
```bash
sbt "project server" test compile
```

You will also need [Node ≥10.9.0](https://nodejs.org) and [Yarn](https://yarnpkg.com) if you want to work on the dashboard.
To build the latter, an extra step is required in order to build the user interface:
```bash
yarn install
yarn build
sbt "project dashboard" compile
```

## About
Private Data Donor is a research project whose goal is to gather statistics about Web search queries in a privacy-preserving way.
Collected data is then used to help monitoring and predicting outbreaks of infectious diseases such as flu.
It is developed by [UCL's CS department](http://www.cs.ucl.ac.uk/home/), in the frame of the [i-sense project](https://www.i-sense.org.uk/), the EPSRC IRC in Early Warning Sensing Systems for Infectious Diseases.

## License

Private Data Donor is made available under the terms of the GNU GPL v3.
