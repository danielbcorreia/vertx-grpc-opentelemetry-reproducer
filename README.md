# vertx-grpc-opentelemetry-reproducer

This is a reproducer for an issue with tracing context propagation between vertx and grpc.
Running this locally will show two separate traces being generated for each request, one with the vert.x span and another with the grpc service span.

## Running

To run the example, first start the jaeger all-in-one docker image:

```bash
docker compose up -d
```

Then run the example using your IDE of choice (e.g. IntelliJ IDEA) - use Runner::main.

Example service calls can be made using the following curl command:

```bash
grpcurl -proto src/main/proto/api.proto -d '{"id": "test"}' -plaintext localhost:8080 com.example.vertx_grpc_opentelemetry_reproducer.Sample/SomeEndpoint
```

## Expected behaviour

The example should generate a single trace for each request, with the vert.x span as the parent of the grpc service span.

## Actual behaviour

The example generates two traces for each request, one with the vert.x span and another with the grpc service span.
