/*******************************************************************************
 * Copyright (C) 2014 University of Massachusetts Medical School Alessandro
 * Rigano (Program in Molecular Medicine) Caterina Strambio De Castillia
 * (Program in Molecular Medicine)
 *
 * Created by the Open Microscopy Environment inteGrated Analysis (OMEGA) team:
 * Alex Rigano, Caterina Strambio De Castillia, Jasmine Clark, Vanni Galli,
 * Raffaello Giulietti, Loris Grossi, Eric Hunter, Tiziano Leidi, Jeremy Luban,
 * Ivo Sbalzarini and Mario Valle.
 *
 * Key contacts: Caterina Strambio De Castillia: caterina.strambio@umassmed.edu
 * Alex Rigano: alex.rigano@umassmed.edu
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package edu.umassmed.omega.commons.data.analysisRunElements;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.umassmed.omega.commons.data.coreElements.OmegaExperimenter;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaSegment;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaSegmentationTypes;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaTrajectory;

public class OmegaTrajectoriesSegmentationRun extends OmegaAnalysisRun {
	
	private static String DISPLAY_NAME = "Segmentation Run";

	private final OmegaSegmentationTypes segmentationTypes;
	private final Map<OmegaTrajectory, List<OmegaSegment>> resultingSegments;

	public OmegaTrajectoriesSegmentationRun(final OmegaExperimenter owner,
			final OmegaRunDefinition algorithmSpec,
			final Map<OmegaTrajectory, List<OmegaSegment>> resultingSegments,
			final OmegaSegmentationTypes segmentationTypes) {
		super(owner, algorithmSpec,
				AnalysisRunType.OmegaTrajectoriesSegmentationRun);
		this.resultingSegments = resultingSegments;
		this.segmentationTypes = segmentationTypes;

		this.reorderSegments();
	}

	public OmegaTrajectoriesSegmentationRun(final OmegaExperimenter owner,
			final OmegaRunDefinition algorithmSpec, final String name,
			final Map<OmegaTrajectory, List<OmegaSegment>> resultingSegments,
			final OmegaSegmentationTypes segmentationTypes) {
		super(owner, algorithmSpec,
				AnalysisRunType.OmegaTrajectoriesSegmentationRun, name);
		this.resultingSegments = resultingSegments;
		this.segmentationTypes = segmentationTypes;

		this.reorderSegments();
	}

	public OmegaTrajectoriesSegmentationRun(final OmegaExperimenter owner,
			final OmegaRunDefinition algorithmSpec, final Date timeStamps,
			final String name,
			final Map<OmegaTrajectory, List<OmegaSegment>> resultingSegments,
			final OmegaSegmentationTypes segmentationTypes) {
		super(owner, algorithmSpec,
				AnalysisRunType.OmegaTrajectoriesSegmentationRun, timeStamps,
				name);
		this.resultingSegments = resultingSegments;
		this.segmentationTypes = segmentationTypes;

		this.reorderSegments();
	}

	private void reorderSegments() {
		for (final OmegaTrajectory traj : this.resultingSegments.keySet()) {
			Collections.sort(this.resultingSegments.get(traj),
					new Comparator<OmegaSegment>() {
						@Override
						public int compare(final OmegaSegment o1,
								final OmegaSegment o2) {
							final int o1Start = o1.getStartingROI()
									.getFrameIndex();
							final int o2Start = o2.getStartingROI()
									.getFrameIndex();
							if (o1Start == o2Start)
								return 0;
							else if (o1Start < o2Start)
								return -1;
							return 1;
						};
					});
		}
	}

	public OmegaSegmentationTypes getSegmentationTypes() {
		return this.segmentationTypes;
	}

	public Map<OmegaTrajectory, List<OmegaSegment>> getResultingSegments() {
		return this.resultingSegments;
	}
	
	public static String getStaticDisplayName() {
		return OmegaTrajectoriesSegmentationRun.DISPLAY_NAME;
	}

	@Override
	public String getDynamicDisplayName() {
		return OmegaTrajectoriesSegmentationRun.getStaticDisplayName();
	}
}
