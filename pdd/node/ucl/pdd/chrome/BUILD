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

package(default_visibility = ["//visibility:public"])

#load("@build_bazel_rules_nodejs//:defs.bzl", "jasmine_node_test")
load("//tools/build_rules/node:node_build.bzl", "node_build")
load("//tools/build_rules/pkg:pkg.bzl", "pkg_zip")

config_setting(name = "devel", define_values = {"env": "devel"})
config_setting(name = "production", define_values = {"env": "production"})

devel_env = {
  "NODE_ENV": "devel",
  "API_URL": "http://localhost:8000",
}

production_env = {
  "NODE_ENV": "production",
  "API_URL": "https://api.ppd.cs.ucl.ac.uk",
  "SENTRY_DSN": "https://7460f432c8bf4fe6bf516fa467141b24@sentry.io/302350",
}

node_build(
  name = "chrome",
  entry = ["options.js", "background.js"],
  srcs = glob(["**/*.js", "**/*.css"]),
  env = select({
    ":devel": devel_env,
    ":production": production_env,
    "//conditions:default": devel_env,
  })
)

pkg_zip(
  name = "package",
  srcs = [
    ":chrome",
    "options.html",
    "manifest.json",
  ] + glob(["images/**"]),
)

#jasmine_node_test(
#  name = "unit_test",
#  srcs = [":unit_test_lib"],
#  node_modules = "//3rdparty:node_modules",
#)
#
#node_build(
#  name = "unit_test_lib",
#  entry = ["crypto/index.spec.js"],
#  srcs = glob(["**/*.js", "**/*.css"]),
#)
