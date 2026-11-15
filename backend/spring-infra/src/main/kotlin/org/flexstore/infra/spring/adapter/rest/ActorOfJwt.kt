package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.springframework.security.oauth2.jwt.Jwt
import org.ucop.domain.entity.Actor

// The actor behind the token of a request, a visitor when there is no token
fun ActorResolutionService.actorOf(jwt: Jwt?): Actor = jwt?.let { actorOf(UserId.of(it.subject)) } ?: visitor()
