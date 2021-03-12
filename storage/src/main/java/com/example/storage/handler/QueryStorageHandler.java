package com.example.storage.handler;

import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class QueryStorageHandler implements Handler<RoutingContext> {

	private final Logger logger = LoggerFactory.getLogger(QueryStorageHandler.class);

	@Override
	public void handle(RoutingContext context) {

		JsonObject jsonObject = context.getBodyAsJson();

		context.vertx().eventBus().<JsonObject>request("store.data", jsonObject, reply -> {
			if (reply.succeeded()) {
				logger.info(reply.result().body());
				var success = reply.result().body().getBoolean("success");
				if (success) {
					context.response().end(reply.result().body().getString("msg"));
				} else {
					context.response().setStatusCode(404).end("Something went wrong while saving data :(");
				}
			} else {
				context.response().setStatusCode(404).end(reply.cause().getMessage());
			}
		});
		logger.info("Executing here");
	}
}
