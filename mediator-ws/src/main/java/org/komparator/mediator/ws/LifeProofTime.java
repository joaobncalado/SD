package org.komparator.mediator.ws;

import java.util.Timer;

public class LifeProofTime extends Timer {

	public LifeProofTime() {
	}

	public LifeProofTime(boolean isDaemon) {
		super(isDaemon);
	}

	public LifeProofTime(String name) {
		super(name);
	}

	public LifeProofTime(String name, boolean isDaemon) {
		super(name, isDaemon);
	}

}
