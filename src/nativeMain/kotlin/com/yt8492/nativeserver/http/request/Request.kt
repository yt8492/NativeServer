package com.yt8492.nativeserver.http.request

import com.yt8492.nativeserver.http.Header

class Request(
    val requestLine: RequestLine,
    val headers: List<Header>,
    val body: ByteArray,
)
