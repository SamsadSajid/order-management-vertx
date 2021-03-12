package com.example.storage;

import com.example.storage.verticle.ApiVerticle;
import com.example.storage.verticle.RedisVerticle;
import io.vertx.reactivex.core.Vertx;

public class StorageApplication {

	public static void main(String[] args) {
		Vertx vertx = Vertx.vertx();

		vertx.deployVerticle(new ApiVerticle());
		vertx.deployVerticle(new RedisVerticle());
	}
}
