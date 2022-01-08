package com.yt8492.nativeserver.http.response

import com.yt8492.nativeserver.http.Header

class Response(
    val statusLine: StatusLine,
    val headers: List<Header>,
    val body: ByteArray,
)
