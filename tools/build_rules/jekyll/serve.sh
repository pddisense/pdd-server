#!/bin/sh
echo "serving on http://localhost:8080"
exec TEMPLATED_httpserver $PWD/TEMPLATED_dist_dir
