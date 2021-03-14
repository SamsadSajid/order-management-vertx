package com.example.storage.verticle;

import com.example.storage.model.RedisData;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Optional;

public class RedisVerticle extends AbstractVerticle {

	private static final String HASH = "myhash-";
	private final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);
	private ObjectMapper objectMapper;

	private RedisAPI redis;

	@Override
	public void start() {

		objectMapper = new ObjectMapper();

		Redis client = Redis.createClient(vertx, getRedisOptions());
		redis = RedisAPI.api(client);

		vertx.eventBus().consumer("store.data", this::saveData);
		vertx.eventBus().consumer("fetch.data", this::getData);
	}

	private RedisOptions getRedisOptions() {

		return new RedisOptions()
			.setConnectionString("redis://localhost:6379");
	}

	private void saveData(Message<RedisData> message) {

		logger.info("Saving to cache - " + message.body());

		// The following is event-loop
		logger.info(Thread.currentThread().getName());

		vertx.executeBlocking(promise -> {
			var key = message.body().getKey();
			var data = processAndGetData(message.body().getObject());

			if (data.isEmpty()) {
				message.fail(404, "Something went wrong!");
			} else {
				redis
					.hset(Arrays.asList(HASH, key, data.get()))
					.onSuccess(v -> {
						logger.info(v);
						message.reply(new JsonObject().put("msg", "Stored data to redis successfully"));
					})
					.onFailure(v -> {
						logger.error(v);
						message.fail(404, "Something went wrong!");
					});
			}
		});
	}

	private void getData(Message<RedisData> message) {

		logger.info("Fetching from cache - " + message.body());

		// The following is event-loop
		logger.info(Thread.currentThread().getName());

		var key = message.body().getKey();
		Class tClass = message.body().getObject().getClass();

		vertx.executeBlocking(promise -> {
			// The following is worker thread from worker pool.
			logger.info(Thread.currentThread().getName());

			redis
				.hget(HASH, key)
				.onSuccess(v -> {
					// v -> of type byte array called BulkType
					if (v == null) {
						message.fail(404, "No value exists with the key");
					} else {
						Optional obj = getDataFromObject(v, tClass);

						if (obj.isEmpty()) {
							message.fail(404, "Something went wrong!");
						} else {
							message.reply(
								RedisData.builder()
									.object(obj.get()) // how to dynamically add Product.class
									.build()
							);
						}
					}
				})
				.onFailure(v -> {
					logger.error(v);
					message.fail(404, "Something went wrong!");
				});
		});
	}

	private Optional<String> processAndGetData(Object data) {

		try {
			return Optional.ofNullable(objectMapper.writeValueAsString(data));
		} catch (JsonProcessingException ex) {
			logger.error("Could not convert object to string " + data, ex);

			return Optional.empty();
		}
	}

	public <T> Optional<T> getDataFromObject(Object data, Class<T> tClass) {
		try {
			return Optional.ofNullable(objectMapper.readValue(String.valueOf(data), tClass));
		} catch (Exception ex) {
			logger.error("Could not convert object " + data + " to class " + tClass, ex);

			return Optional.empty();
		}
	}
}
