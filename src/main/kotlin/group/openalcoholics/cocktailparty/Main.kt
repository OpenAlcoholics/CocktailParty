@file:JvmName("Main")

package group.openalcoholics.cocktailparty

import group.openalcoholics.cocktailparty.api.Api
import group.openalcoholics.cocktailparty.module.FullBinder
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import mu.KotlinLogging
import java.util.logging.LogManager

class Launcher : AbstractVerticle() {
    private val logger = KotlinLogging.logger { }

    override fun start(done: Future<Void>) {
        // TODO deployment opts

        deploy(vertx, Api::class.java).setHandler {
            logger.info { "Deploying API..." }
            if (it.succeeded()) {
                logger.info { "API deployed" }
                done.complete()
            } else {
                logger.error(it.cause()) { "Deployment failed" }
                done.fail(it.cause())
            }
        }
    }
}

fun main(args: Array<String>) {
    LogManager.getLogManager().readConfiguration(Launcher::class.java
        .getResourceAsStream("/logging.properties"))

    val vertx = Vertx.vertx()
    deploy(vertx, Launcher::class.java)
}

private fun deploy(
    vertx: Vertx, verticle: Class<out Verticle>,
    opts: DeploymentOptions = DeploymentOptions()): Future<Void> {
    val done: Future<Void> = Future.future()
    val deploymentName = "java-guice:" + verticle.name
    val config = json {
        obj(
            "guice_binder" to FullBinder::class.java.name
        )
    }

    opts.config = config

    vertx.deployVerticle(deploymentName, opts) {
        if (it.succeeded()) {
            done.complete()
        } else {
            done.fail(it.cause())
        }
    }

    return done
}
