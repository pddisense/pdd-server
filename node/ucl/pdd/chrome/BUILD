package(default_visibility = ["//visibility:public"])
load("//tools/build_rules/node:node_build.bzl", "node_build")
load("//tools/build_rules/pkg:pkg.bzl", "pkg_zip")

config_setting(name = "devel", define_values = {"env": "devel"})
config_setting(name = "production", define_values = {"env": "production"})

devel_env = {
  "NODE_ENV": "devel",
  "API_URL": "http://localhost:8080",
}

production_env = {
  "NODE_ENV": "production",
  "API_URL": "https://api.ppd.cs.ucl.ac.uk",
  "SENTRY_DSN": "https://7460f432c8bf4fe6bf516fa467141b24@sentry.io/302350",
}

node_build(
  name = "chrome",
  entry = ["app/options.js", "app/background.js"],
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
