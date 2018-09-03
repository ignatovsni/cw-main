package com.cwsni.world.model.engine.modifiers;

public class ModifierSource {

	private ModifierSourceType type;
	private Object id;

	public ModifierSource(ModifierSourceType type, Object id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public int hashCode() {
		return getType().hashCode() + getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ModifierSource)) {
			return false;
		}
		ModifierSource otherObj = (ModifierSource) obj;
		return otherObj.getType().equals(getType()) && otherObj.getId().equals(getId());
	}

	@Override
	public String toString() {
		return "ModifierSource: type=" + getType() + ", id=" + getId();
	}
	

	public ModifierSourceType getType() {
		return type;
	}

	public Object getId() {
		return id;
	}


}
