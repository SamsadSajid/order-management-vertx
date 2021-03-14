package com.example.storage.verticle;

import com.example.storage.codec.GenericCodec;
import com.example.storage.handler.GetDataFromStorageHandler;
import com.example.storage.handler.QueryStorageHandler;
import com.example.storage.model.RedisData;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class ApiVerticle extends AbstractVerticle {

	private QueryStorageHandler queryStorageHandler;
	private GetDataFromStorageHandler getDataFromStorageHandler;

	@Override
	public void start(Promise<Void> startPromise) {
		initHandler();
		vertx.eventBus().registerDefaultCodec(RedisData.class, new GenericCodec<>(RedisData.class));

		vertx.createHttpServer()
			.requestHandler(initAndGetRouter())
			.listen(8888, http -> {
				if (http.succeeded()) {
					startPromise.complete();
					System.out.println("HTTP server started on port 8888");
				} else {
					startPromise.fail(http.cause());
				}
			});
	}

	private void initHandler() {
		this.queryStorageHandler = new QueryStorageHandler();
		this.getDataFromStorageHandler = new GetDataFromStorageHandler();
	}

	Router initAndGetRouter() {
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

//		router.get("/ping").handler(pingHandler).failureHandler(exceptionHandler);
		router.post("/check-storage").handler(queryStorageHandler);
		router.get("/data/:key").handler(getDataFromStorageHandler);

		return router;
	}
}
