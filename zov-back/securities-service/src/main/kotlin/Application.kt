package zov.deneg

import io.ktor.server.application.*
import zov.deneg.repository.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val dataSource = configureDatabase()

    configureSecurity()
    configureRouting()
}
