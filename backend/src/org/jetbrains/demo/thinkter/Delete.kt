@file:Suppress("ImplicitThis")

package org.jetbrains.demo.thinkter


import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.jetbrains.demo.thinkter.dao.ThinkterStorage
import org.jetbrains.demo.thinkter.model.PostThoughtToken
import org.jetbrains.demo.thinkter.model.RpcData

@KtorExperimentalLocationsAPI
fun Route.delete(dao: ThinkterStorage, hashFunction: (String) -> String) {
    get<ThoughtDelete> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        val date = System.currentTimeMillis()

        if (user == null) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val code = call.securityCode(date, user, hashFunction)
            call.respond(PostThoughtToken(user.userId, date, code))
        }
    }

    post<ThoughtDelete> {
        val user = call.sessions.get<Session>()?.let { dao.user(it.userId) }
        val thought = dao.getThought(it.id)

        if (user == null || thought.userId != user.userId || !call.verifyCode(it.date, user, it.code, hashFunction)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            dao.deleteThought(it.id)
            call.respond(object : RpcData {})
        }
    }
}
