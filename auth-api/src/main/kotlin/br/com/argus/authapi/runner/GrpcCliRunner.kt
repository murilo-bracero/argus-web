package br.com.argus.authapi.runner

import br.com.argus.authapi.grpc.AuthServiceGrpc
import io.grpc.Server
import io.grpc.ServerBuilder
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.io.IOException

@Profile("!test")
@Component
class GrpcCliRunner(
    @Autowired private val authServiceGrpc: AuthServiceGrpc,
    @Value("\${grpc.server.port}") private val port: Int
) : CommandLineRunner {

    private val log = KotlinLogging.logger {  }

    private var server: Server? = null

    @Throws(IOException::class)
    fun start() {
        server = ServerBuilder.forPort(port)
            .addService(authServiceGrpc)
            .build()
            .start()
        log.info("gRPC server listening at port {}", port)
        Runtime.getRuntime().addShutdownHook(Thread { stop() })
    }

    private fun stop() {
        if (server != null) {
            log.info("Shutting down gRPC server")
            server!!.shutdown()
        }
    }

    @Throws(InterruptedException::class)
    fun block() {
        if (server != null) {
            server!!.awaitTermination()
        }
    }

    override fun run(vararg args: String?) {
        start()
        block()
    }
}