import com.google.protobuf.gradle.id
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin ("jvm") version "1.9.0"
  application
  id("com.google.protobuf") version "0.9.3"
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.4.5"
val junitJupiterVersion = "5.9.1"
val openTelemetryVersion  = "1.28.0"
val protobufVersion = "3.23.0"

application {
  mainClass.set("com.example.vertx_grpc_opentelemetry_reproducer.Runner")
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-grpc-server")
  implementation("io.vertx:vertx-opentelemetry")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")
  implementation(kotlin("stdlib-jdk8"))
  implementation("io.grpc:grpc-kotlin-stub:1.3.0")
  implementation("io.grpc:grpc-protobuf:1.57.0")
  implementation("com.google.protobuf:protobuf-kotlin:${protobufVersion}")
  implementation("io.opentelemetry:opentelemetry-extension-trace-propagators:${openTelemetryVersion}")
  implementation("io.opentelemetry:opentelemetry-semconv:${openTelemetryVersion}-alpha")
  implementation("io.opentelemetry:opentelemetry-exporter-otlp:${openTelemetryVersion}")
  implementation("io.opentelemetry:opentelemetry-extension-kotlin:${openTelemetryVersion}")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "17"

tasks.withType<Test> {
  useJUnitPlatform()
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${protobufVersion}"
  }

  plugins {
    create("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:1.57.0"
    }
    create("grpckt") {
      artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
    }
  }

  generateProtoTasks {
    all().forEach {
      it.plugins {
        id("grpc")
        id("grpckt")
      }
      it.builtins {
        create("kotlin")
      }
    }
  }
}
