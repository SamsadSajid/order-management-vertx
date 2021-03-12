package com.example.storage.verticle;

import com.example.storage.handler.GetDataFromStorageHandler;
import com.example.storage.handler.QueryStorageHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ApiVerticle extends AbstractVerticle {

	private QueryStorageHandler queryStorageHandler;
	private GetDataFromStorageHandler getDataFromStorageHandler;

	@Override
	public void start(Promise<Void> startPromise) {
		initHandler();

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
