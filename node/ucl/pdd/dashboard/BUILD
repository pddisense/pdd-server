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
