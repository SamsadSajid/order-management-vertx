package com.example.storage.handler;

import com.example.storage.model.Product;
import com.example.storage.model.RedisData;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class GetDataFromStorageHandler implements Handler<RoutingContext> {

	private final Logger logger = LoggerFactory.getLogger(GetDataFromStorageHandler.class);

	@Override
	public void handle(RoutingContext context) {

		var vertx = context.vertx();

		var data = RedisData.builder()
			.key(context.pathParam("key"))
			.object(Product.builder().build())
			.build();

		vertx.executeBlocking(promise -> vertx.eventBus().<RedisData>request("fetch.data", data, reply -> {
			logger.info("waiting for reply from fetch.data channel...");

			int statusCode;
			String res;

			if (reply.succeeded()) {
				statusCode = 200;
				Product product = (Product) reply.result().body().getObject();

				res = Json.encode(product);
			} else {
				statusCode = 404;
				res = Json.encode(reply.cause().getMessage());
			}

			context.response()
				.setStatusCode(statusCode)
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(res);
		}));
	}
}
