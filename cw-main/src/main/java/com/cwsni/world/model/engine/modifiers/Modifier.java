package com.cwsni.world.model.engine.modifiers;

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
		Modifier otherObj = (Modifier) obj;
		return otherObj.getType().equals(getType()) && otherObj.getFeature().equals(getFeature())
				&& otherObj.getSource().equals(getSource());
	}

	@Override
	public String toString() {
		return "Modifier: feature=" + getFeature() + ", type=" + getType() + ", source=" + getSource();
	}

	protected Modifier(T feature, ModifierType type, ModifierSourceType sourceType, Object sourceId,
			Double value) {
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

	// -------------------------- static section --------------------------------
	public static Modifier createModifierByEvent(Object feature, ModifierType type, Object sourceId, Double value) {
		return new Modifier(feature, type, ModifierSourceType.EVENT, sourceId, value);
	}

}
