package group.openalcoholics.cocktailparty.api.handler

import group.openalcoholics.cocktailparty.module.AuthConfig
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import java.util.*
import javax.inject.Inject

class AdminSecurityHandler @Inject constructor(authConfig: AuthConfig) : Handler<RoutingContext> {

    private val key = authConfig.adminKey

    override fun handle(ctx: RoutingContext) {
        val key = ctx.request().getHeader("AdminKey") ?: return ctx.fail(401)
        val decrypted = Base64.getDecoder()
            .decode(key.toByteArray())
            .toString(Charsets.UTF_8)
        when {
            decrypted.isBlank() -> ctx.fail(401)
            decrypted == this.key -> ctx.next()
            else -> ctx.fail(403)
        }
    }
}
