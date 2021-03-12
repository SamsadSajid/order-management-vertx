package com.example.storage.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;

import java.util.Arrays;

public class RedisVerticle extends AbstractVerticle {

	private final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

	private RedisAPI redis;

	@Override
	public void start() {

		Redis client = Redis.createClient(vertx, getRedisOptions());
		redis = RedisAPI.api(client);

		vertx.eventBus().consumer("store.data", this::saveData);
		vertx.eventBus().consumer("fetch.data", this::getData);
	}

	private RedisOptions getRedisOptions() {

		return new RedisOptions()
			.setConnectionString("redis://localhost:6379");
	}

	private void saveData(Message<JsonObject> message) {

		logger.info("Saving to cache - " + message.body());
		JsonObject msgBody = message.body();

		redis.set(Arrays.asList(msgBody.getString("key"), msgBody.getString("value")))
			.onSuccess(v -> message.reply(new JsonObject().put("msg", "Stored data to redis successfully")))
			.onFailure(v -> {
				logger.error(v);
				message.fail(404, "Could not save value");
			});
	}

	private void getData(Message<JsonObject> message) {

		logger.info("Fetching from cache - " + message.body());

		redis.get(message.body().getString("key"))
			.onSuccess(v -> {
				logger.info(v);
				message.reply(new JsonObject().put("msg", v.toString()));
			})
			.onFailure(v -> {
				logger.error(v);
				message.fail(404, "No value exists with the key");
			});
	}
}
