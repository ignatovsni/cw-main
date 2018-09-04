package com.cwsni.world.model.data.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

public class DoubleContextualSerializer extends JsonSerializer<Double> implements ContextualSerializer {

	private Precision precision;

	public DoubleContextualSerializer(Precision precision) {
		this.precision = precision;
	}

	public DoubleContextualSerializer() {

	}

	@Override
	public void serialize(Double value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		if (precision == null) {
			gen.writeNumber(value.doubleValue());
		} else {
			BigDecimal bd = new BigDecimal(value);
			bd = bd.setScale(precision.precision(), RoundingMode.HALF_UP);
			gen.writeNumber(bd.doubleValue());
		}

	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
			throws JsonMappingException {
		Precision precision = property.getAnnotation(Precision.class);
		return new DoubleContextualSerializer(precision);
	}
}
