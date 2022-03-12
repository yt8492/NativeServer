package com.yt8492.nativeserver.http

import com.yt8492.nativeserver.http.request.Method
import com.yt8492.nativeserver.http.request.ServerRequest
import com.yt8492.nativeserver.http.response.ServerResponse

data class Handler(
    val routingPath: RoutingPath,
    val method: Method,
    val handleFunc: HandleFunc
)

typealias HandleFunc = (request: ServerRequest) -> ServerResponse
