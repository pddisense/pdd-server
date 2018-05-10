# Private Data Donor

This repository contains the source code for the Private Data Donor platform, whose goal is to collect web search queries, in order to identify outbreaks of infectious disease much earlier than ever before.
It is a research project conducted in the frame of the broader [i-sense project](https://www.i-sense.org.uk/), the EPSRC IRC in Early Warning Sensing Systems for Infectious Diseases.
Private Data Donor is developed conjointly by research teams from [UCL's CS department](http://www.cs.ucl.ac.uk/home/).

Private Data Donor is made of three components:
  * the API server;
  * the dashboard;
  * the Chrome extension.

## Getting started

This repository follows the mono-repository pattern, and all the code is compiled using Bazel.
It allows to compile multiple languages (Java/Scala and Javascript in our case) using the same build tool.

1. Clone this repository: `git clone git@gitlab.cs.ucl.ac.uk:pdd/pdd.git`
2. [Install Bazel](https://docs.bazel.build/versions/master/install.html) on your machine.
3. Install the NodeJS dependencies: `bazel run @yarn//:yarn`
4. Compile everything: `bazel build ...`

## License

Private Data Donor is made available under the terms of the GNU GPL v3: https://www.gnu.org/licenses/gpl-3.0.en.html
