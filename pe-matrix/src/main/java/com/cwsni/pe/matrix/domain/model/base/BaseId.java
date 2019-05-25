package com.cwsni.pe.matrix.domain.model.base;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cwsni.pe.matrix.domain.model.base.BaseId.BaseIdDeserializer;
import com.cwsni.pe.matrix.domain.model.base.BaseId.BaseIdSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = BaseIdSerializer.class)
@JsonDeserialize(using = BaseIdDeserializer.class)
public class BaseId {

	private final String type;
	private final long id;

	public BaseId(String type, long id) {
		this.type = intern(type);
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return type.hashCode() + (int) id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BaseId to = (BaseId) obj;
		return type.equals(to.type) && id == to.id;
	}

	@Override
	public String toString() {
		return "id=" + type + ":" + id;
	}

	private static final Map<String, String> internMap = new ConcurrentHashMap<>();

	/**
	 * It is similar to String.intern(). To save memory and to improve performance
	 * in equals method.
	 */
	private String intern(String value) {
		return internMap.computeIfAbsent(value.toLowerCase(), k -> k);
	}

	public static class BaseIdSerializer extends JsonSerializer<BaseId> {
		@Override
		public void serialize(BaseId value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeObject(value.getType() + ":" + value.getId());
		}
	}

	public static class BaseIdDeserializer extends JsonDeserializer<BaseId> {
		@Override
		public BaseId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			String value = p.readValueAs(String.class);
			int index = value.indexOf(':');
			return new BaseId(value.substring(0, index), Long.valueOf(value.substring(index + 1)));
		}
	}

}
