load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "quartz",
    exports = [
        "//3rdparty/jvm/com/mchange:c3p0",
        "//3rdparty/jvm/com/mchange:mchange_commons_java",
        "//3rdparty/jvm/com/zaxxer:HikariCP_java6",
        "//3rdparty/jvm/org/slf4j:slf4j_api",
        "//external:jar/org/quartz_scheduler/quartz"
    ],
    visibility = [
        "//visibility:public"
    ]
)


