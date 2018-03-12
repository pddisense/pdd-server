package(default_visibility = ["//visibility:public"])

scala_library(
  name = "config",
  srcs = glob(["*.scala"]),
  deps = [
    ":annotations",
    "//3rdparty/jvm/com/twitter:inject_core",
    "//3rdparty/jvm/com/github/nscala_time",
  ]
)

java_library(
  name = "annotations",
  srcs = glob(["*.java"]),
  deps = [
    "//3rdparty/jvm/com/twitter:inject_core",
  ],
)
