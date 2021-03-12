package com.example.storage.handler;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class GetDataFromStorageHandler implements Handler<RoutingContext> {

	private final Logger logger = LoggerFactory.getLogger(GetDataFromStorageHandler.class);

	@Override
	public void handle(RoutingContext context) {
		context.vertx().eventBus().<JsonObject>request("fetch.data", context.pathParam("key"), reply -> {
			logger.info(reply);

			if (reply.succeeded()) {
				context.response().end(reply.result().body().encode());
			} else {
				context.response().setStatusCode(404).end(reply.cause().getMessage());
			}
		});
		logger.info("executing here");
	}
}
