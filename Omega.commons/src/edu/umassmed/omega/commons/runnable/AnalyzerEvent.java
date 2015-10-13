package edu.umassmed.omega.commons.runnable;

import edu.umassmed.omega.commons.eventSystem.events.OmegaMessageEvent;

public class AnalyzerEvent extends OmegaMessageEvent {

	private final boolean isEnded;

	public AnalyzerEvent(final String msg, final boolean isEnded) {
		super(msg);
		this.isEnded = isEnded;
	}

	public boolean isEnded() {
		return this.isEnded;
	}
}
