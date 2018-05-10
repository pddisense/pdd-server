load("//tools/build_rules/node:node_build.bzl", "node_build")

config_setting(name = "devel", define_values = {"env": "devel"})
config_setting(name = "production", define_values = {"env": "production"})

node_build(
  name = "ui",
  srcs = glob(["**/*.js", "**/*.jsx", "**/*.css"]),
  config = "webpack.config.js",
  data = ["index.html", "robots.txt"],
  env = select({
    ":devel": {
      "NODE_ENV": "devel",
      "API_URL": "http://localhost:8080",
    },
    "//conditions:default": {
      "NODE_ENV": "devel",
      "API_URL": "http://localhost:8080",
    },
    ":production": {
      "NODE_ENV": "production",
      "API_URL": "https://api.ppd.cs.ucl.ac.uk",
      "SENTRY_DSN": "https://5b07d9c3667a4aa3a9549f977f903dfd@sentry.ppd.cs.ucl.ac.uk/3",
    },
  })
)
