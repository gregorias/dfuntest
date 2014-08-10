package org.nebulostore.dfuntesting;

public class TestResult {
	private final Type type_;
	private final String description_;

	public TestResult(Type type, String description) {
		type_ = type;
		description_ = description;
	}
	
	public Type getType() {
		return type_;
	}
	
	public String getDescription() {
		return description_;
	}
	
	public enum Type {
		SUCCESS,
		FAILURE
	};
}
