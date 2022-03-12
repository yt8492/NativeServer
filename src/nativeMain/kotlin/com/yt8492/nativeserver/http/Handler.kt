package com.yt8492.nativeserver.http

import com.yt8492.nativeserver.http.request.Method
import com.yt8492.nativeserver.http.request.Request
import com.yt8492.nativeserver.http.response.Response

data class Handler(
    val routingPath: RoutingPath,
    val method: Method,
    val handleFunc: HandleFunc
)

typealias HandleFunc = (request: Request) -> Response
