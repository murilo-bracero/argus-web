package br.com.argus.customer_backend.config

import br.com.argus.authapi.AuthApiServiceGrpc
import io.grpc.ManagedChannelBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfig(
    @Value("\${services.grpc.auth-api}") private val grpcAuthApiUrl: String,
) {

    private val authApiChannel = ManagedChannelBuilder.forTarget(grpcAuthApiUrl).usePlaintext().build()

    @Bean
    fun authApiBlockingStub() : AuthApiServiceGrpc.AuthApiServiceBlockingStub{
        return AuthApiServiceGrpc.newBlockingStub(authApiChannel)
    }

    @Bean
    fun authApiAsyncStub() : AuthApiServiceGrpc.AuthApiServiceStub{
        return AuthApiServiceGrpc.newStub(authApiChannel)
    }

}