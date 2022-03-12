package com.yt8492.nativeserver

import com.yt8492.nativeserver.http.Headers
import com.yt8492.nativeserver.http.Server
import com.yt8492.nativeserver.http.response.Response
import com.yt8492.nativeserver.http.response.StatusLine
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.usePinned
import platform.posix.SIGINT
import platform.posix.STDOUT_FILENO
import platform.posix.signal
import platform.posix.write

var server: Server? = null

fun main() {
    shutdownHook()
    server = Server()
    val server = server ?: return
    server.get("/") { request ->
        val body = """{"message": "This is root."}""".encodeToByteArray()
        val headers = Headers()
        headers.add("Content-Length", body.size.toString())
        return@get Response(
            statusLine = StatusLine(
                httpVersion = "HTTP/1.1",
                statusCode = 200,
                reasonPhrase = "OK",
            ),
            headers = headers,
            body = body,
        )
    }
    server.listen(8080)
}

fun shutdownHook() {
    signal(SIGINT, staticCFunction<Int, Unit> {
        server?.close()
        "server stop\n".usePinned { pinned ->
            write(STDOUT_FILENO, pinned.addressOf(0), 24)
        }
    })
}
