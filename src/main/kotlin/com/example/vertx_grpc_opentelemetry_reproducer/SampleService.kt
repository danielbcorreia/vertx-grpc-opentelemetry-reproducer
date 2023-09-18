package com.example.vertx_grpc_opentelemetry_reproducer

import com.example.vertx_grpc_opentelemetry_reproducer.proto.SampleGrpcKt
import com.example.vertx_grpc_opentelemetry_reproducer.proto.SomeEndpointReply
import com.example.vertx_grpc_opentelemetry_reproducer.proto.SomeEndpointRequest
import kotlin.coroutines.CoroutineContext

class SampleService(coroutineContext: CoroutineContext) : SampleGrpcKt.SampleCoroutineImplBase(coroutineContext) {

  override suspend fun someEndpoint(request: SomeEndpointRequest): SomeEndpointReply =
      OpenTelemetryExtensions.withSpanSuspend("internalSpan") { span ->
          span.setAttribute("someAttribute", "someValue")

          SomeEndpointReply.newBuilder()
              .setId("some id")
              .setName("some name")
              .build()
      }

}
