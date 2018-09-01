package group.openalcoholics.cocktailparty

import group.openalcoholics.cocktailparty.api.Api
import group.openalcoholics.cocktailparty.module.FullBinder
import io.vertx.core.*
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

class Launcher : AbstractVerticle() {
    override fun start(done: Future<Void>) {
        // TODO deployment opts

        deploy(vertx, Api::class.java).setHandler {
            if (it.succeeded()) {
                done.complete()
            } else {
                done.fail(it.cause())
            }
        }
    }
}

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    deploy(vertx, Launcher::class.java)
}

private fun deploy(vertx: Vertx, verticle: Class<out Verticle>, opts: DeploymentOptions = DeploymentOptions())
        : Future<Void> {
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
