package com.yt8492.nativeserver.http.response

import com.yt8492.nativeserver.http.Headers

open class ServerResponse(
    val statusCode: Int,
    val reasonPhrase: String,
    val headers: Headers = Headers(),
    val body: ByteArray,
) {
    fun toHttpResponse(): Response {
        val statusLine = StatusLine(
            httpVersion = "HTTP/1.1",
            statusCode = statusCode,
            reasonPhrase = reasonPhrase,
        )
        val contentLength = body.size
        headers.add("Content-Length", contentLength.toString())
        return Response(
            statusLine = statusLine,
            headers = headers,
            body = body,
        )
    }
}
