package com.yt8492.nativeserver

import com.yt8492.nativeserver.http.Headers
import com.yt8492.nativeserver.http.request.Request
import com.yt8492.nativeserver.http.response.Response
import com.yt8492.nativeserver.http.response.StatusLine
import com.yt8492.nativeserver.socket.ServerSocket

fun main() {
    val serverSocket = ServerSocket(8080)
    while (true) {
        val socket = serverSocket.accept()
        val request = Request.from(socket.inputStream)
        val response = Response(
            statusLine = StatusLine(
                httpVersion = "HTTP/1.1",
                statusCode = 200,
                reasonPhrase = "OK",
            ),
            headers = Headers(),
            body = request.body,
        )
        response.writeTo(socket.outputStream)
        socket.close()
    }
}
