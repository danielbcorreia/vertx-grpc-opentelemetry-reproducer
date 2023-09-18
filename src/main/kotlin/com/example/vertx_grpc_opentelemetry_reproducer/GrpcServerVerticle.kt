package com.example.vertx_grpc_opentelemetry_reproducer

import io.vertx.core.http.HttpServerOptions
import io.vertx.core.tracing.TracingPolicy
import io.vertx.grpc.server.GrpcServer
import io.vertx.grpc.server.GrpcServiceBridge
import io.vertx.kotlin.coroutines.CoroutineVerticle

class GrpcServerVerticle : CoroutineVerticle() {

  companion object {
    private const val PORT = 8080
  }

  override suspend fun start() {
    val sampleService = SampleService(coroutineContext)
    val grpcServer = GrpcServer.server(vertx)
    GrpcServiceBridge.bridge(sampleService).bind(grpcServer)

    vertx.createHttpServer(
      HttpServerOptions()
        .setTracingPolicy(TracingPolicy.ALWAYS)
    )
      .requestHandler(grpcServer)
      .listen(PORT)
      .onSuccess { println("gRPC server deployed [localhost:$PORT]") }
      .onFailure { e -> println("Error on deploying gRPC server [localhost:$PORT]: $e") }
  }

}
