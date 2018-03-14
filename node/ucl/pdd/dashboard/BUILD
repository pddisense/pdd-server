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
