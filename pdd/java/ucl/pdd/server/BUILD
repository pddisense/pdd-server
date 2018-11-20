# PDD is a platform for privacy-preserving Web searches collection.
# Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
#
# PDD is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# PDD is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with PDD.  If not, see <http://www.gnu.org/licenses/>.

load("@io_bazel_rules_docker//scala:image.bzl", "scala_image")
load("@io_bazel_rules_docker//container:push.bzl", "container_push")

srcs = glob(["*.scala"])
deps = [
  ":annotations",
  "//pdd/java/ucl/pdd/domain",
  "//pdd/java/ucl/pdd/jackson",
  "//pdd/java/ucl/pdd/strategy",
  "//pdd/java/ucl/pdd/logging",
  "//pdd/java/ucl/pdd/metrics",
  "//pdd/java/ucl/pdd/service",
  "//pdd/java/ucl/pdd/geocoder",
  "//pdd/java/ucl/pdd/storage",
  "//pdd/java/ucl/pdd/storage/install",
  "//3rdparty/jvm/com/twitter:finatra_http",
]
main_class = "ucl.pdd.server.PddServerMain"
runtime_deps = [
  "//3rdparty/jvm/ch/qos/logback:logback_classic",
  "//3rdparty/jvm/com/twitter:twitter_server_logback_classic",
]

scala_binary(
  name = "server",
  srcs = srcs,
  deps = deps,
  main_class = main_class,
  runtime_deps = runtime_deps,
)

java_library(
  name = "annotations",
  srcs = glob(["*.java"]),
  deps = [
    "//3rdparty/jvm/com/twitter:inject_core",
  ],
)

scala_image(
  name = "image",
  srcs = srcs,
  deps = deps,
  main_class = main_class,
  runtime_deps = runtime_deps,
)

container_push(
 name = "publish",
 image = ":image",
 format = "Docker",
 registry = "index.docker.io",
 repository = "pddisense/pdd-server",
 tag = "latest",
 stamp = True,
)
