package com.yt8492.nativeserver.http.response

import com.yt8492.nativeserver.http.Header
import com.yt8492.nativeserver.socket.SocketOutputStream

class Response(
    val statusLine: StatusLine,
    val headers: List<Header>,
    val body: ByteArray,
) {
    fun writeTo(
        outputStream: SocketOutputStream,
    ) {
        outputStream.write(statusLine.httpVersion)
        outputStream.write(" ")
        outputStream.write(statusLine.statusCode.toString())
        outputStream.write(" ")
        outputStream.write(statusLine.reasonPhrase)
        outputStream.write("\r\n")
        headers.forEach { header ->
            outputStream.write("${header.name}:${header.value}\r\n")
        }
        outputStream.write("\r\n")
        outputStream.write(body, body.size)
    }
}
