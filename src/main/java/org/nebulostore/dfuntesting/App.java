package org.nebulostore.dfuntesting;

public abstract class App {
	private final int id_;
	private final String name_;
	
	public App(int id, String name) {
		id_ = id;
		name_ = name;
	}
	
	public int getId() {
		return id_;
	}
	
	public String getName() {
		return name_;
	}
	
	public abstract boolean isRunning();

	public abstract boolean isWorking();
	
	public abstract void run() throws Exception;

	public abstract void shutDown() throws Exception;
}
