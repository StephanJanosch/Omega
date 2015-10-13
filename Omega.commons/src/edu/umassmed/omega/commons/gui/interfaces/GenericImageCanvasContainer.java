package edu.umassmed.omega.commons.gui.interfaces;

import java.util.List;

import edu.umassmed.omega.commons.data.trajectoryElements.OmegaTrajectory;

public interface GenericImageCanvasContainer {

	void sendCoreEventTrajectories(List<OmegaTrajectory> trajectories,
			boolean selection);
}
