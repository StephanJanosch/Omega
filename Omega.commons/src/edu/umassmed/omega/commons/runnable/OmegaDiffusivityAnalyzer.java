package edu.umassmed.omega.commons.runnable;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import edu.umassmed.omega.commons.data.trajectoryElements.OmegaROI;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaSegment;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaTrajectory;
import edu.umassmed.omega.commons.gui.interfaces.OmegaMessageDisplayerPanelInterface;
import edu.umassmed.omega.commons.libraries.OmegaDiffusivityLibrary;

public class OmegaDiffusivityAnalyzer implements Runnable {

	private OmegaMessageDisplayerPanelInterface displayerPanel;

	private final Map<OmegaTrajectory, List<OmegaSegment>> segments;
	private final Map<OmegaSegment, Double[]> nyMap;
	private final Map<OmegaSegment, Double[]> gammaFromLogMap;
	private final Map<OmegaSegment, Double[]> gammaMap;
	private final Map<OmegaSegment, Double[][]> gammaDFromLogMap;
	private final Map<OmegaSegment, Double[][]> gammaDMap;
	private final Map<OmegaSegment, Double[][]> logMuMap;
	private final Map<OmegaSegment, Double[][]> muMap;
	private final Map<OmegaSegment, Double[][]> logDeltaTMap;
	private final Map<OmegaSegment, Double[][]> deltaTMap;
	private final Map<OmegaSegment, Double[]> smssFromLogMap;
	private final Map<OmegaSegment, Double[]> smssMap;

	public OmegaDiffusivityAnalyzer(
	        final Map<OmegaTrajectory, List<OmegaSegment>> segments) {
		this.segments = segments;
		this.nyMap = new LinkedHashMap<>();
		this.gammaFromLogMap = new LinkedHashMap<>();
		this.gammaMap = new LinkedHashMap<>();
		this.gammaDFromLogMap = new LinkedHashMap<>();
		this.gammaDMap = new LinkedHashMap<>();
		this.logMuMap = new LinkedHashMap<>();
		this.muMap = new LinkedHashMap<>();
		this.logDeltaTMap = new LinkedHashMap<>();
		this.deltaTMap = new LinkedHashMap<>();
		this.smssFromLogMap = new LinkedHashMap<>();
		this.smssMap = new LinkedHashMap<>();
		this.displayerPanel = null;
	}

	public OmegaDiffusivityAnalyzer(
	        final OmegaMessageDisplayerPanelInterface displayerPanel,
	        final Map<OmegaTrajectory, List<OmegaSegment>> segments) {
		this(segments);
		this.displayerPanel = displayerPanel;
	}

	@Override
	public void run() {
		final int counter = 1;
		final int max = this.segments.keySet().size();
		for (final OmegaTrajectory track : this.segments.keySet()) {
			final List<OmegaROI> rois = track.getROIs();
			if (this.displayerPanel != null) {
				this.updateStatusAsync(
						"Processing trajectory " + track.getName() + " "
								+ counter + "/" + max, false);
			}
			for (final OmegaSegment segment : this.segments.get(track)) {
				final int roiStart = rois.indexOf(segment.getStartingROI());
				final int roiEnd = rois.indexOf(segment.getEndingROI());
				final List<OmegaROI> segmentROIs = rois.subList(roiStart,
						roiEnd);
				final Double[] x = new Double[segmentROIs.size()];
				final Double[] y = new Double[segmentROIs.size()];
				for (int i = 0; i < segmentROIs.size(); i++) {
					final OmegaROI roi = segmentROIs.get(i);
					x[i] = roi.getX();
					y[i] = roi.getY();
				}
				final int windowDivisor = 3;
				if ((x.length / 3) < 2) {
					System.out.println(track.getName()
					        + " skipped because length=" + x.length
					        + " divided by wd=" + windowDivisor
					        + " less than 2");
					continue;
				}
				final int Delta_t = 1; // Time between frames?
				final Double[][][] nu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS = OmegaDiffusivityLibrary
				        .computeNu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS(x, y,
				                windowDivisor, Delta_t);
				final Double[] ny = nu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS[0][0];
				final Double[][] deltaT = nu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS[1];
				final Double[][] mu = nu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS[2];
				final Double[][] gammaD = nu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS[3];
				final Double[] gamma = new Double[ny.length];
				for (int i = 0; i < gammaD.length; i++) {
					gamma[i] = gammaD[i][0];
				}
				final Double[] smss = nu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS[4][0];

				final Double[][][] nu_DeltaNLogDeltaT_DeltaNLogMu_GammaDFromLog_SMSSFromLog = OmegaDiffusivityLibrary
				        .computeNu_DeltaNLogDeltaT_DeltaNLogMu_GammaDFromLog_SMSSFromLog(
				                x, y, windowDivisor, Delta_t);
				// final Double[] ny =
				// nu_DeltaNDeltaT_DeltaNMu_GammaD_SMSS[0][0];
				final Double[][] logDeltaT = nu_DeltaNLogDeltaT_DeltaNLogMu_GammaDFromLog_SMSSFromLog[1];
				final Double[][] logMu = nu_DeltaNLogDeltaT_DeltaNLogMu_GammaDFromLog_SMSSFromLog[2];
				final Double[][] gammaDFromLog = nu_DeltaNLogDeltaT_DeltaNLogMu_GammaDFromLog_SMSSFromLog[3];
				final Double[] gammaFromLog = new Double[ny.length];
				for (int i = 0; i < gammaD.length; i++) {
					gammaFromLog[i] = gammaDFromLog[i][0];
				}
				final Double[] smssFromLog = nu_DeltaNLogDeltaT_DeltaNLogMu_GammaDFromLog_SMSSFromLog[4][0];

				this.nyMap.put(segment, ny);
				this.gammaFromLogMap.put(segment, gammaFromLog);
				this.gammaMap.put(segment, gamma);
				this.logDeltaTMap.put(segment, logDeltaT);
				this.deltaTMap.put(segment, deltaT);
				this.logMuMap.put(segment, logMu);
				this.muMap.put(segment, mu);
				this.gammaDFromLogMap.put(segment, gammaDFromLog);
				this.gammaDMap.put(segment, gammaD);
				this.smssFromLogMap.put(segment, smssFromLog);
				this.smssMap.put(segment, smss);
			}
		}
		if (this.displayerPanel != null) {
			this.updateStatusAsync("Processing ended", true);
		}
	}

	public Map<OmegaTrajectory, List<OmegaSegment>> getSegments() {
		return this.segments;
	}

	public Map<OmegaSegment, Double[]> getNyResults() {
		return this.nyMap;
	}

	public Map<OmegaSegment, Double[]> getGammaFromLogResults() {
		return this.gammaFromLogMap;
	}

	public Map<OmegaSegment, Double[]> getGammaResults() {
		return this.gammaMap;
	}

	public Map<OmegaSegment, Double[][]> getGammaDFromLogResults() {
		return this.gammaDFromLogMap;
	}

	public Map<OmegaSegment, Double[][]> getGammaDResults() {
		return this.gammaDMap;
	}

	public Map<OmegaSegment, Double[][]> getLogMuResults() {
		return this.logMuMap;
	}

	public Map<OmegaSegment, Double[][]> getMuResults() {
		return this.muMap;
	}

	public Map<OmegaSegment, Double[][]> getLogDeltaTResults() {
		return this.logDeltaTMap;
	}

	public Map<OmegaSegment, Double[][]> getDeltaTResults() {
		return this.deltaTMap;
	}

	public Map<OmegaSegment, Double[]> getSmssFromLogResults() {
		return this.smssFromLogMap;
	}

	public Map<OmegaSegment, Double[]> getSmssResults() {
		return this.smssMap;
	}

	private void updateStatusSync(final String msg, final boolean ended) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					OmegaDiffusivityAnalyzer.this.displayerPanel
					.updateMessageStatus(new AnalyzerEvent(msg, ended));
				}
			});
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void updateStatusAsync(final String msg, final boolean ended) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				OmegaDiffusivityAnalyzer.this.displayerPanel
				.updateMessageStatus(new AnalyzerEvent(msg, ended));
			}
		});
	}
}
