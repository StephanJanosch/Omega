package edu.umassmed.omega.commons.data.analysisRunElements;

import java.util.ArrayList;
import java.util.List;

import edu.umassmed.omega.commons.data.coreElements.OmegaElement;

public class OrphanedAnalysisContainer extends OmegaElement implements
		OmegaAnalysisRunContainerInterface {
	
	private static String DISPLAY_NAME = "Orphaned Analysis Container";
	
	private final List<OmegaAnalysisRun> analysisRuns;

	public OrphanedAnalysisContainer() {
		this.analysisRuns = new ArrayList<OmegaAnalysisRun>();
	}

	@Override
	public List<OmegaAnalysisRun> getAnalysisRuns() {
		return this.analysisRuns;
	}

	@Override
	public void addAnalysisRun(final OmegaAnalysisRun analysisRun) {
		this.analysisRuns.add(analysisRun);
	}

	@Override
	public void removeAnalysisRun(final OmegaAnalysisRun analysisRun) {
		this.analysisRuns.remove(analysisRun);
	}

	@Override
	public boolean containsAnalysisRun(final long id) {
		for (final OmegaAnalysisRun analysisRun : this.analysisRuns) {
			if (analysisRun.getElementID() == id)
				return true;
		}
		return false;
	}

	public boolean isEmpty() {
		return this.analysisRuns.isEmpty();
	}
	
	public static String getStaticDisplayName() {
		return OrphanedAnalysisContainer.DISPLAY_NAME;
	}

	@Override
	public String getDynamicDisplayName() {
		return OrphanedAnalysisContainer.getStaticDisplayName();
	}
}
