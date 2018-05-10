package main

import "net/http"
import "os"

func main() {
    var dir = os.Args[1]
    panic(http.ListenAndServe(":8080", http.FileServer(http.Dir(dir))))
}
