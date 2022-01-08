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

    companion object {
        fun from(rawValue: String): Method {
            return when (rawValue) {
                OPTIONS.rawValue -> OPTIONS
                GET.rawValue -> GET
                HEAD.rawValue -> HEAD
                POST.rawValue -> POST
                PUT.rawValue -> PUT
                PATCH.rawValue -> PATCH
                DELETE.rawValue -> DELETE
                TRACE.rawValue -> TRACE
                CONNECT.rawValue -> TRACE
                else -> EXTENSION(rawValue)
            }
        }
    }
}