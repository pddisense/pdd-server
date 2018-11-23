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
  "//pdd/java/ucl/pdd/monitoring",
  "//3rdparty/jvm/com/twitter:finatra_http",
  "//3rdparty/jvm/com/twitter:finatra_httpclient",
  "//3rdparty/jvm/com/pauldijou:jwt_core",
]
resources = [
  "//pdd/node/dashboard:index.bundle.js",
  "//pdd/node/dashboard:index.html",
  "//pdd/node/dashboard:robots.txt",
]
resource_strip_prefix = "pdd/node/dashboard"
main_class = "ucl.pdd.dashboard.PddDashboardMain"

scala_binary(
  name = "dashboard",
  srcs = srcs,
  deps = deps,
  resources = resources,
  resource_strip_prefix = resource_strip_prefix,
  main_class = main_class,
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
  resources = resources,
  resource_strip_prefix = resource_strip_prefix,
  main_class = main_class,
)

container_push(
 name = "publish",
 image = ":image",
 format = "Docker",
 registry = "index.docker.io",
 repository = "pddisense/pdd-dashboard",
 tag = "latest",
 stamp = True,
)
