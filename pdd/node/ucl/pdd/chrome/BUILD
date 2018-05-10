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
