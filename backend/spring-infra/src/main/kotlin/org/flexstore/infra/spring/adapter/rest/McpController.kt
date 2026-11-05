package org.flexstore.infra.spring.adapter.rest

import org.flexstore.mcp.McpAnswer
import org.flexstore.mcp.McpServer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

// The endpoint of the MCP server (Streamable HTTP transport): an AI assistant posts its JSON-RPC messages here.
// Not served when flexstore.mcp.enabled is false
@RestController
@ConditionalOnProperty("flexstore.mcp.enabled", havingValue = "true", matchIfMissing = true)
class McpController(private val mcpServer: McpServer) {

    @PostMapping("/mcp")
    fun receive(@RequestBody message: String): ResponseEntity<String> =
        when (val answer = mcpServer.answer(message)) {
            is McpAnswer.Reply -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(answer.json)
            McpAnswer.Accepted -> ResponseEntity.accepted().build()
        }

    // The server never opens a stream to the client
    @GetMapping("/mcp")
    fun noStream(): ResponseEntity<Unit> = ResponseEntity.status(HTTP_METHOD_NOT_ALLOWED).build()
}

private const val HTTP_METHOD_NOT_ALLOWED = 405
