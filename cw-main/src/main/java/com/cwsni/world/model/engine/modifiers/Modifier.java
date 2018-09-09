package com.cwsni.world.model.engine.modifiers;

import com.cwsni.world.client.desktop.locale.LocaleMessageSource;
import com.cwsni.world.client.desktop.util.DataFormatter;
import com.cwsni.world.model.engine.Event;

public class Modifier<T> {

	private T feature;
	private ModifierType type;
	private ModifierSource source;
	private Double value;
	private String targetInfo;

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
		return "Modifier: targetInfo=" + targetInfo + ", feature=" + getFeature() + ", type=" + getType() + ", source="
				+ getSource() + ", value=" + getValue();
	}

	protected Modifier(String targetInfo, T feature, ModifierType type, Double value, ModifierSourceType sourceType,
			Object sourceId) {
		this.feature = feature;
		this.type = type;
		this.targetInfo = targetInfo;
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

	public String createDescription(LocaleMessageSource messageSource, Integer precision) {
		StringBuilder sb = new StringBuilder();
		if (ModifierType.ADD.equals(type) || ModifierType.ADD_2.equals(type)) {
			if (value > 0) {
				sb.append("+");
			}
			if (precision == null) {
				sb.append(value);
			} else {
				sb.append(DataFormatter.doubleWithPrecison(value, precision));
			}
			sb.append(" ");
			sb.append(messageSource.getMessage("data-model.modifiers.add"));
		} else if (ModifierType.MULTIPLY.equals(type)) {
			sb.append("*");
			if (precision == null) {
				sb.append(value);
			} else {
				sb.append(DataFormatter.doubleWithPrecison(value, precision));
			}
			sb.append(" ");
			sb.append(messageSource.getMessage("data-model.modifiers.multiply"));
		}		
		return sb.toString();
	}

	// -------------------------- static section --------------------------------
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Modifier createModifierByEvent(String targetInfo, Object feature, ModifierType type, Double value,
			Event event) {
		return new Modifier(targetInfo, feature, type, value, ModifierSourceType.EVENT, event.getId());
	}

}
