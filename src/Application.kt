package com.github.jflc

import com.fasterxml.jackson.databind.SerializationFeature
import com.github.jflc.api.accounts
import com.github.jflc.api.accountsExceptionHandler
import com.github.jflc.db.dbInit
import com.github.jflc.service.AccountsService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.minimumSize
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.DataConversionException
import io.ktor.util.KtorExperimentalAPI
import org.slf4j.event.Level
import java.util.UUID

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    // init database
    val db = dbInit {
        url = environment.config.property("db.url").getString()
        driver = environment.config.property("db.driver").getString()
    }

    // init services
    val accountsService = AccountsService(db)

    // start module with dependencies
    accountsModule(accountsService)
}

@KtorExperimentalLocationsAPI
fun Application.accountsModule(accountsService: AccountsService) {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(DefaultHeaders)

    install(Locations)

    install(DataConversion) {
        // support for uuid data types
        convert<UUID> {
            encode { value -> when (value) {
                null -> emptyList()
                is UUID -> listOf(value.toString())
                else -> throw DataConversionException("Cannot convert $value as UUID")
            }}
            decode { values, _ -> values.singleOrNull()?.let { UUID.fromString(it) } }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(StatusPages) {
        exception<Throwable> { ex ->
            accountsExceptionHandler(
                log = environment.log,
                call = call,
                ex = ex
            )
        }
    }


    routing {
        get("/healthcheck") {
            call.respondText("UP")
        }

        accounts(accountsService)
    }
}

