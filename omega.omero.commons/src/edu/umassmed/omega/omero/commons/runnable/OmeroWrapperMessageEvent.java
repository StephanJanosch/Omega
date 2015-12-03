package edu.umassmed.omega.omero.commons.runnable;

import java.util.List;

import edu.umassmed.omega.commons.eventSystem.events.OmegaMessageEvent;
import edu.umassmed.omega.omero.commons.data.OmeroImageWrapper;

public class OmeroWrapperMessageEvent extends OmegaMessageEvent {

	private final List<OmeroImageWrapper> wrappers;

	public OmeroWrapperMessageEvent(final String msg,
	        final List<OmeroImageWrapper> wrappers) {
		super(msg);
		this.wrappers = wrappers;
	}

	public List<OmeroImageWrapper> getWrappers() {
		return this.wrappers;
	}
}
