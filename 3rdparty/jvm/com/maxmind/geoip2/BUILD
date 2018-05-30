load("@io_bazel_rules_scala//scala:scala_import.bzl", "scala_import")
java_library(
    name = "geoip2",
    exports = [
        "//3rdparty/jvm/com/fasterxml/jackson/core:jackson_annotations",
        "//3rdparty/jvm/com/fasterxml/jackson/core:jackson_core",
        "//3rdparty/jvm/com/fasterxml/jackson/core:jackson_databind",
        "//3rdparty/jvm/com/maxmind/db:maxmind_db",
        "//3rdparty/jvm/org/apache/httpcomponents:httpclient",
        "//external:jar/com/maxmind/geoip2/geoip2"
    ],
    visibility = [
        "//visibility:public"
    ]
)


