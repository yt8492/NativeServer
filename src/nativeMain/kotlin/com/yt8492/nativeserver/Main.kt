package com.yt8492.nativeserver

import com.yt8492.nativeserver.http.Headers
import com.yt8492.nativeserver.http.request.Request
import com.yt8492.nativeserver.http.response.Response
import com.yt8492.nativeserver.http.response.StatusLine
import com.yt8492.nativeserver.socket.ServerSocket
import com.yt8492.nativeserver.socket.SocketException
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.usePinned
import platform.posix.SIGINT
import platform.posix.STDOUT_FILENO
import platform.posix.signal
import platform.posix.write

var serverSocket: ServerSocket? = null

fun main() {
    shutdownHook()
    try {
        serverSocket = ServerSocket(8080)
        println("server start")
        while (true) {
            val socket = serverSocket?.accept() ?: return
            val request = Request.from(socket.inputStream)
            val body = """
                path: ${request.path}
                queries: ${request.queryParameters.joinToString { "${it.name}: ${it.value}" }}
            """.trimIndent()
            val headers = Headers().apply {
                add("Content-Type", "text/html; charset=utf-8")
            }
            val response = Response(
                statusLine = StatusLine(
                    httpVersion = "HTTP/1.1",
                    statusCode = 200,
                    reasonPhrase = "OK",
                ),
                headers = headers,
                body = body.encodeToByteArray(),
            )
            response.writeTo(socket.outputStream)
            socket.close()
        }
    } catch (e: SocketException) {
        println("socket closed")
        return
    }
}

fun shutdownHook() {
    signal(SIGINT, staticCFunction<Int, Unit> {
        serverSocket?.close()
        "server stop\n".usePinned { pinned ->
            write(STDOUT_FILENO, pinned.addressOf(0), 24)
        }
    })
}
