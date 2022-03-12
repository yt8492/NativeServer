package com.yt8492.nativeserver.http

import com.yt8492.nativeserver.http.request.Method
import com.yt8492.nativeserver.http.request.Request
import com.yt8492.nativeserver.http.response.Response
import com.yt8492.nativeserver.http.response.StatusLine
import com.yt8492.nativeserver.socket.ServerSocket
import com.yt8492.nativeserver.socket.SocketException

class Server {
    private val handlers = mutableListOf<Handler>()
    private var defaultResponse = Response(
        statusLine = StatusLine(
            httpVersion = "HTTP/1.1",
            statusCode = 404,
            reasonPhrase = "Not Found"
        ),
        headers = Headers(),
        body = byteArrayOf()
    )
    var serverSocket: ServerSocket? = null

    fun get(path: String, handleFunc: HandleFunc) {
        handle(path, Method.GET, handleFunc)
    }

    fun post(path: String, handleFunc: HandleFunc) {
        handle(path, Method.POST, handleFunc)
    }

    fun put(path: String, handleFunc: HandleFunc) {
        handle(path, Method.PUT, handleFunc)
    }

    fun head(path: String, handleFunc: HandleFunc) {
        handle(path, Method.HEAD, handleFunc)
    }

    fun delete(path: String, handleFunc: HandleFunc) {
        handle(path, Method.DELETE, handleFunc)
    }

    fun connect(path: String, handleFunc: HandleFunc) {
        handle(path, Method.CONNECT, handleFunc)
    }

    fun options(path: String, handleFunc: HandleFunc) {
        handle(path, Method.OPTIONS, handleFunc)
    }

    fun trace(path: String, handleFunc: HandleFunc) {
        handle(path, Method.TRACE, handleFunc)
    }

    fun patch(path: String, handleFunc: HandleFunc) {
        handle(path, Method.PATCH, handleFunc)
    }

    fun handle(path: String, method: Method, handleFunc: HandleFunc) {
        val newHandler = Handler(
            RoutingPath.parse(path),
            method,
            handleFunc
        )
        handlers.add(newHandler)
    }

    fun listen(port: Int) {
        serverSocket?.let {
            throw ServerAlreadyStartException("server already start on port ${it.port}")
        }
        try {
            val serverSocket = ServerSocket(port)
            this.serverSocket = serverSocket
            println("server start")
            while (true) {
                val socket = serverSocket.accept()
                val request = Request.from(socket.inputStream)
                val (evaluateResult, handler) = handlers
                    .associateBy {
                        it.routingPath.evaluate(request.path)
                    }
                    .filter { (result, _) ->
                        result.succeeded
                    }
                    .maxByOrNull { (result, _) ->
                        result.quality
                    }
                    ?.toPair()
                    ?: (PathEvaluationResult.failed() to null)
                if (handler == null) {
                    defaultResponse.writeTo(socket.outputStream)
                } else {
                    val response = handler.handleFunc(request)
                    response.writeTo(socket.outputStream)
                }
                socket.close()
            }
        } catch (e: SocketException) {
            println("server stopped")
            return
        }
    }

    fun close() {
        serverSocket?.close()
        serverSocket = null
    }
}
