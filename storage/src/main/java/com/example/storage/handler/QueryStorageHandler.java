package com.example.storage.handler;

import com.example.storage.model.Product;
import com.example.storage.model.RedisData;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;


public class QueryStorageHandler implements Handler<RoutingContext> {

	private final Logger logger = LoggerFactory.getLogger(QueryStorageHandler.class);

	@Override
	public void handle(RoutingContext context) {

		var vertx = context.vertx();

		var product = getProduct(context.getBodyAsJson());

		var data = RedisData.builder()
			.key(product.getName())
			.object(product)
			.build();

		vertx.executeBlocking(promise -> vertx.eventBus().<RedisData>request("store.data", data, reply -> {
			int statusCode;
			String res;

			if (reply.succeeded()) {
				statusCode = 200;
				res = Json.encode(reply.result().body());
			} else {
				statusCode = 404;
				res = Json.encode(reply.cause().getMessage());
			}

			context.response().setStatusCode(statusCode).end(res);
		}));
	}

	Product getProduct(JsonObject jsonObject) {

		return Product.builder()
			.name(jsonObject.getString("name"))
			.price(jsonObject.getString("price"))
			.quantity(jsonObject.getString("price"))
			.build();
	}
}
