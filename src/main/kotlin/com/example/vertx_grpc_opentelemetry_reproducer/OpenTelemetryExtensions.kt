package com.example.vertx_grpc_opentelemetry_reproducer

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.extension.kotlin.asContextElement
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

object OpenTelemetryExtensions {

  private val tracer: Tracer
    get() = GlobalOpenTelemetry.get().getTracer("some-scope")

  suspend fun <T> withSpanSuspend(
    spanName: String,
    parameters: (SpanBuilder.() -> Unit)? = null,
    block: suspend (span: Span) -> T
  ): T = tracer.startSpanSuspend(spanName, parameters, block)

  suspend fun <T> Tracer.startSpanSuspend(
    spanName: String,
    parameters: (SpanBuilder.() -> Unit)? = null,
    block: suspend (span: Span) -> T
  ): T {
    val span: Span = this.spanBuilder(spanName).run {
      if (parameters != null) parameters()
      coroutineContext[CoroutineName]?.let {
        setAttribute("coroutine.name", it.name)
      }
      startSpan()
    }

    val context = Vertx.currentContext() ?: throw IllegalStateException("No Vert.x context found")

    return withContext(context.dispatcher() + span.asContextElement()) {
      try {
        block(span)
      } catch (throwable: Throwable) {
        span.setStatus(StatusCode.ERROR)
        span.recordException(throwable)
        throw throwable
      } finally {
        span.end()
      }
    }
  }

}
