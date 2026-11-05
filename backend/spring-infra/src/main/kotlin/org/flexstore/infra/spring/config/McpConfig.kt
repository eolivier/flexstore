package org.flexstore.infra.spring.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper
import io.modelcontextprotocol.server.McpServer
import io.modelcontextprotocol.server.McpStatelessSyncServer
import io.modelcontextprotocol.server.transport.WebMvcStatelessServerTransport
import org.flexstore.domain.product.usecase.ProductUseCases
import org.flexstore.mcp.ProductTools
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.function.RouterFunction
import org.springframework.web.servlet.function.ServerResponse

private const val MCP_ENDPOINT = "/mcp"
private const val SERVER_NAME = "flexstore"
private const val SERVER_VERSION = "1.0.0"

// The MCP server of the products, served by the MCP Java SDK; turned off with flexstore.mcp.enabled=false.
// The transport answers each message on its own (no session): an AI assistant posts its JSON-RPC messages
// to /mcp
@Configuration
@ConditionalOnProperty("flexstore.mcp.enabled", havingValue = "true", matchIfMissing = true)
open class McpConfig {

    @Bean
    open fun productTools(productUseCases: ProductUseCases) = ProductTools(productUseCases)

    @Bean
    open fun mcpTransport(): WebMvcStatelessServerTransport {
        val jsonMapper = JacksonMcpJsonMapper(ObjectMapper())
        val transport = WebMvcStatelessServerTransport.builder().jsonMapper(jsonMapper)
        return transport.messageEndpoint(MCP_ENDPOINT).build()
    }

    @Bean
    open fun mcpServer(
        transport: WebMvcStatelessServerTransport,
        productTools: ProductTools
    ): McpStatelessSyncServer {
        val server = McpServer.sync(transport).serverInfo(SERVER_NAME, SERVER_VERSION)
        return server.tools(productTools.specifications()).build()
    }

    // Serves the endpoint of the transport; asking for the server makes sure it is built before the first
    // message
    @Bean
    open fun mcpRoutes(
        transport: WebMvcStatelessServerTransport,
        mcpServer: McpStatelessSyncServer
    ): RouterFunction<ServerResponse> = transport.routerFunction
}
