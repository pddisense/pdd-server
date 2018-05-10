package(default_visibility = ["//visibility:public"])
load("//tools/build_rules/node:node_build.bzl", "node_build")

config_setting(name = "devel", define_values = {"env": "devel"})
config_setting(name = "production", define_values = {"env": "production"})

devel_env = {
  "NODE_ENV": "devel",
  "API_URL": "http://localhost:8080",
  "SENTRY_DSN": "",
}

production_env = {
  "NODE_ENV": "production",
  "API_URL": "https://api.ppd.cs.ucl.ac.uk",
  "SENTRY_DSN": "https://c02524fee4574cf7b2fa608cdc1ef0eb@sentry.ppd.cs.ucl.ac.uk/4",
}

node_build(
  name = "chrome",
  srcs = glob(["**/*.js", "**/*.css"]),
  config = "webpack.config.js",
  deps = [
    "//node/ucl/colossus",
  ],
  data = glob(["images/*"]) + ["options.html", "manifest.json"],
  env = select({
    ":devel": devel_env,
    ":production": production_env,
    "//conditions:default": devel_env,
  })
)

# Actual NPM dependencies:
# - @blueprintjs/core
# - autobind-decorator
# - bn.js
# - crypto-browserify
# - elliptic
# - moment
# - prop-types
# - raven-js
# - react
# - react-addons-css-transition-group
# - react-dom
# - request
# - url-parse
