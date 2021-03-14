package com.example.storage.codec;

import com.example.storage.handler.QueryStorageHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.io.*;

public class GenericCodec<T> implements MessageCodec<T, T> {

	private final Class<T> tClass;
	private final Logger logger = LoggerFactory.getLogger(QueryStorageHandler.class);

	public GenericCodec(Class<T> tClass) {
		super();
		this.tClass = tClass;
	}

	@Override
	public void encodeToWire(Buffer buffer, T t) {

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			ObjectOutput out;
			out = new ObjectOutputStream(bos);
			out.writeObject(t);
			out.flush();
			byte[] yourBytes = bos.toByteArray();
			buffer.appendInt(yourBytes.length);
			buffer.appendBytes(yourBytes);
			out.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public T decodeFromWire(int pos, Buffer buffer) {

		// My custom message starting from this *position* of buffer
		int _pos = pos;

		// Length of JSON
		int length = buffer.getInt(_pos);

		// Jump 4 because getInt() == 4 bytes
		byte[] yourBytes = buffer.getBytes(_pos += 4, _pos += length);
		try (ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes)) {
			ObjectInputStream ois = new ObjectInputStream(bis);
//			@SuppressWarnings("unchecked")
			T msg = (T) ois.readObject();
			ois.close();

			return msg;
		} catch (IOException | ClassNotFoundException e) {
			logger.error("Listen failed " + e.getMessage());
		}

		return null;
	}

	@Override
	public T transform(T customMessage) {

		// If a message is sent *locally* across the event bus.
		// This example sends message just as is
		return customMessage;
	}

	@Override
	public String name() {

		// Each codec must have a unique name.
		// This is used to identify a codec when sending a message and for unregistering
		// codecs.
		return tClass.getSimpleName() + "Codec";
	}

	@Override
	public byte systemCodecID() {

		return -1;
	}
}
