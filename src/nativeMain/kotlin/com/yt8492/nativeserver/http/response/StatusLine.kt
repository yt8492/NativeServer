package com.yt8492.nativeserver.http.response

data class StatusLine(
    val httpVersion: String,
    val statusCode: Int,
    val reasonPhrase: String,
)
