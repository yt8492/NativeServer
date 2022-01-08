package com.yt8492.nativeserver.http.request

data class RequestLine(
    val method: Method,
    val uri: String,
    val httpVersion: String,
)
