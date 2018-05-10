load("@build_bazel_rules_nodejs//:defs.bzl", "nodejs_binary")

nodejs_binary(
  name = "webpack",
  node_modules = "//3rdparty:node_modules",
  entry_point = "webpack/bin/webpack.js",
  visibility = ["//visibility:public"],
)

nodejs_binary(
  name = "eslint",
  node_modules = "//3rdparty:node_modules",
  entry_point = "eslint/bin/eslint.js",
  visibility = ["//visibility:public"],
)
