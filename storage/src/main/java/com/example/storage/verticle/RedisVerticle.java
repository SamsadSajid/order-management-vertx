package com.example.storage.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RedisVerticle extends AbstractVerticle {

	private final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

	private RedisAPI redis;

	@Override
	public void start() {
		Redis client = Redis.createClient(vertx);
		redis = RedisAPI.api(client);

		vertx.eventBus().consumer("store.data", this::save);
		vertx.eventBus().consumer("fetch.data", this::get);
	}

	private void save(Message<JsonObject> message) {
		logger.info("put - " + message.body());
		JsonObject msgBody = message.body();

		Future<Response> responseFuture = redis
			.set(Arrays.asList(msgBody.getString("key"), msgBody.getString("value")));

		responseFuture.onSuccess(v -> {
			JsonObject ack = new JsonObject()
				.put("success", true)
				.put("msg", "Stored data to redis successfully");

			message.reply(ack);
		});

		responseFuture.onFailure(v -> {
			message.fail(404, "Could not save value");
		});
	}

	private void get(Message<JsonObject> message) {
		logger.info("get - " + message.body());

		Future<Response> responseFuture = redis.get("nice");

		responseFuture.onSuccess(v -> {
			logger.info(v);
			JsonObject ack = new JsonObject().put("msg", v.toString());
			message.reply(ack);
		});
		responseFuture.onFailure(v -> {
			message.fail(404, "No value exists with the key");
		});
	}
}
