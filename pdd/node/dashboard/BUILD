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

load("//tools/build_rules/node:node_build.bzl", "node_build")

config_setting(name = "devel", define_values = {"env": "devel"})
config_setting(name = "production", define_values = {"env": "production"})

devel_env = {
  "NODE_ENV": "devel",
  #"SENTRY_DSN": "https://3b359301775f40f992827f8d685cbcb4@sentry.io/302349",
}

production_env = {
  "NODE_ENV": "production",
  "SENTRY_DSN": "https://3b359301775f40f992827f8d685cbcb4@sentry.io/302349",
}

node_build(
  name = "ui",
  entry = ["index.js"],
  srcs = glob(["**/*.js", "**/*.jsx", "**/*.css"]),
  env = select({
    ":devel": devel_env,
    ":production": production_env,
    "//conditions:default": devel_env,
  }),
  visibility = ["//visibility:public"],
)

exports_files(["index.html", "robots.txt"])
