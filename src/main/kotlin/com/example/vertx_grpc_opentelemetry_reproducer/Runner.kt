package com.example.vertx_grpc_opentelemetry_reproducer

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.extension.trace.propagation.B3Propagator
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.tracing.TracingOptions
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions

private val tracer: Tracer
  get() = GlobalOpenTelemetry.get().getTracer("some-global-scope")

object Runner {

  @JvmStatic
  fun main(args: Array<String>) {
    val resource: Resource = Resource.getDefault()
      .merge(
        Resource.create(
          Attributes.of(ResourceAttributes.SERVICE_NAME, "reproducer")
        )
      )

    val spanExporter = OtlpGrpcSpanExporter
      .builder()
      .setEndpoint("http://localhost:4317")
      .build()

    val sdkTracerProvider = SdkTracerProvider.builder()
      .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
      .setResource(resource)
      .build()

    val openTelemetry = OpenTelemetrySdk.builder()
      .setTracerProvider(sdkTracerProvider)
      .setPropagators(ContextPropagators.create(B3Propagator.injectingMultiHeaders()))
      .buildAndRegisterGlobal()

    val vertx = Vertx.vertx(VertxOptions().setTracingOptions(TracingOptions(OpenTelemetryOptions(openTelemetry))))
    vertx.deployVerticle(GrpcServerVerticle())
  }

}

