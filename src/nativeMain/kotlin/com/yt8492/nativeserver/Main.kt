package com.yt8492.nativeserver

import com.yt8492.nativeserver.http.Headers
import com.yt8492.nativeserver.http.request.Request
import com.yt8492.nativeserver.http.response.Response
import com.yt8492.nativeserver.http.response.StatusLine
import com.yt8492.nativeserver.socket.ServerSocket
import kotlinx.cinterop.toKString

fun main() {
    val serverSocket = ServerSocket(8080)
    while (true) {
        val socket = serverSocket.accept()
        val request = Request.from(socket.inputStream)
        val body = """
            uri: ${request.requestLine.uri}
            method: ${request.requestLine.method}
            headers: [${request.headers.joinToString { "${it.name}: ${it.value}" }}]
            body: ${request.body.toKString()}
        """.trimIndent()
        val response = Response(
            statusLine = StatusLine(
                httpVersion = "HTTP/1.1",
                statusCode = 200,
                reasonPhrase = "OK",
            ),
            headers = Headers(),
            body = body.encodeToByteArray(),
        )
        response.writeTo(socket.outputStream)
        socket.close()
    }
}
