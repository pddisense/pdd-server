# Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

load("@io_bazel_rules_docker//scala:image.bzl", "scala_image")
load("@io_bazel_rules_docker//container:push.bzl", "container_push")

srcs = glob(["*.scala"])
deps = [
  ":annotations",
  "//pdd/java/ucl/pdd/api",
  "//pdd/java/ucl/pdd/jackson",
  "//pdd/java/ucl/pdd/strategy",
  "//pdd/java/ucl/pdd/logging",
  "//pdd/java/ucl/pdd/metrics",
  "//pdd/java/ucl/pdd/service",
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
