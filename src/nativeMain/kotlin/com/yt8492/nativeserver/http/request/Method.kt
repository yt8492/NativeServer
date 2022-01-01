package com.yt8492.nativeserver.http.request

sealed class Method(val rawValue: String) {
    object OPTIONS : Method("OPTIONS")
    object GET : Method("GET")
    object HEAD : Method("HEAD")
    object POST : Method("POST")
    object PUT : Method("PUT")
    object PATCH : Method("PATCH")
    object DELETE : Method("DELETE")
    object TRACE : Method("TRACE")
    object CONNECT : Method("CONNECT")
    class EXTENSION(value: String) : Method(value)
}