package com.cwsni.world.model.engine.modifiers;

import com.cwsni.world.model.data.DataEvent;

public class Modifier<T> {

	private T feature;
	private ModifierType type;
	private ModifierSource source;
	private Double value;

	@Override
	public int hashCode() {
		return getFeature().hashCode() + getType().hashCode() + getSource().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Modifier)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Modifier otherObj = (Modifier) obj;
		return otherObj.getType().equals(getType()) && otherObj.getFeature().equals(getFeature())
				&& otherObj.getSource().equals(getSource());
	}

	@Override
	public String toString() {
		return "Modifier: feature=" + getFeature() + ", type=" + getType() + ", source=" + getSource() + ", value=" + getValue();
	}

	protected Modifier(T feature, ModifierType type, Double value, ModifierSourceType sourceType,
			Object sourceId) {
		this.feature = feature;
		this.type = type;
		this.source = new ModifierSource(sourceType, sourceId);
		this.value = value;
	}

	public T getFeature() {
		return feature;
	}

	public ModifierType getType() {
		return type;
	}

	public ModifierSource getSource() {
		return source;
	}

	public Double getValue() {
		return value;
	}
	
	protected void setValue(Double value) {
		this.value = value;
	}

	// -------------------------- static section --------------------------------
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Modifier createModifierByEvent(Object feature, ModifierType type, Double value, DataEvent event) {
		return new Modifier(feature, type, value, ModifierSourceType.EVENT, event.getId());
	}

}
