load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "sentry",
    exports = [
        "//3rdparty/jvm/com/fasterxml/jackson/core:jackson_core",
        "//external:jar/io/sentry/sentry"
    ],
    visibility = [
        "//visibility:public"
    ]
)



java_library(
    name = "sentry_logback",
    exports = [
        "//3rdparty/jvm/ch/qos/logback:logback_classic",
        "//3rdparty/jvm/ch/qos/logback:logback_core",
        "//3rdparty/jvm/org/slf4j:slf4j_api",
        "//external:jar/io/sentry/sentry_logback",
        ":sentry"
    ],
    visibility = [
        "//visibility:public"
    ]
)


