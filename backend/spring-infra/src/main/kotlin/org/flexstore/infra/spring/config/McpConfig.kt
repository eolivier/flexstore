package org.flexstore.infra.spring.config

import org.flexstore.domain.product.usecase.ProductUseCases
import org.flexstore.mcp.McpServer
import org.flexstore.mcp.ProductTools
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The MCP server of the products; turned off with flexstore.mcp.enabled=false
@Configuration
@ConditionalOnProperty("flexstore.mcp.enabled", havingValue = "true", matchIfMissing = true)
open class McpConfig {

    @Bean
    open fun productTools(productUseCases: ProductUseCases) = ProductTools(productUseCases)

    @Bean
    open fun mcpServer(productTools: ProductTools) = McpServer(productTools)
}
