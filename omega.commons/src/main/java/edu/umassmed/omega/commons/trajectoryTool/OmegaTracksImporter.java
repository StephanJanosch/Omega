package edu.umassmed.omega.commons.trajectoryTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.RootPaneContainer;

import edu.umassmed.omega.commons.data.analysisRunElements.OmegaAnalysisRunContainer;
import edu.umassmed.omega.commons.data.coreElements.OmegaPlane;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaParticle;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaROI;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaTrajectory;
import edu.umassmed.omega.commons.eventSystem.events.OmegaImporterEventResultsParticleTracking;
import edu.umassmed.omega.commons.trajectoryTool.gui.OmegaTracksToolDialog;
import edu.umassmed.omega.commons.utilities.OmegaStringUtilities;
import edu.umassmed.omega.commons.utilities.OmegaTrajectoryIOUtility;

//TODO just copied from the importer, needs to be review to export a list of tracks
public class OmegaTracksImporter extends OmegaTrajectoryIOUtility {
	public static final String PARTICLE_FRAMEINDEX = "identifier";
	public static final String PARTICLE_XCOORD = "x";
	public static final String PARTICLE_YCOORD = "y";
	public static final String PARTICLE_INTENSITY = "intensity";
	public static final String PARTICLE_PROBABILITY = "probability";
	public static final String PARTICLE_SEPARATOR = "separator";

	private final Map<Integer, OmegaPlane> frames;
	private final Map<OmegaPlane, List<OmegaROI>> particles;
	private final Map<OmegaROI, Map<String, Object>> particlesValues;
	private final List<OmegaTrajectory> tracks;

	private OmegaTracksToolDialog dialog;

	private OmegaAnalysisRunContainer container;

	public OmegaTracksImporter(final RootPaneContainer parent) {
		this.frames = new LinkedHashMap<Integer, OmegaPlane>();
		this.particles = new LinkedHashMap<OmegaPlane, List<OmegaROI>>();
		this.particlesValues = new LinkedHashMap<OmegaROI, Map<String, Object>>();
		this.tracks = new ArrayList<OmegaTrajectory>();

		this.dialog = new OmegaTracksToolDialog(parent, true, true, this);

		this.container = null;
	}

	public OmegaTracksImporter() {
		this.frames = new LinkedHashMap<Integer, OmegaPlane>();
		this.particles = new LinkedHashMap<OmegaPlane, List<OmegaROI>>();
		this.particlesValues = new LinkedHashMap<OmegaROI, Map<String, Object>>();
		this.tracks = new ArrayList<OmegaTrajectory>();

		this.dialog = null;

		this.container = null;
	}

	public void showDialog(final RootPaneContainer parent) {
		if (this.dialog == null) {
			this.dialog = new OmegaTracksToolDialog(parent, true, true, this);
		}
		this.dialog.updateParentContainer(parent);
		this.dialog.setVisible(true);
	}

	// TODO change IllegalArgumentException with a custom exception
	public void importTrajectories(final String fileNameIdentifier,
	        final String trajectoryIdentifier, final String particleIdentifier,
			final boolean startAtOne, final String nonParticleIdentifier,
			final String particleSeparator,
	        final List<String> particleDataOrder, final File sourceFolder)
	        throws IOException, IllegalArgumentException {
		if (!sourceFolder.isDirectory())
			throw new IllegalArgumentException("The destination folder: "
			        + sourceFolder + " has to be a valid directory");
		if (sourceFolder.listFiles().length == 0)
			throw new IllegalArgumentException("The destination folder: "
			        + sourceFolder + " has to be not empty");
		boolean isValid = false;

		for (final File f : sourceFolder.listFiles()) {
			final String fName = f.getName();
			if (!fName.matches(fileNameIdentifier)
					&& !fName.startsWith(fileNameIdentifier)) {
				continue;
			}
			isValid = true;
			final FileReader fr = new FileReader(f);
			final BufferedReader br = new BufferedReader(fr);
			this.importTrajectories(f.getName(), trajectoryIdentifier,
			        particleIdentifier, startAtOne, nonParticleIdentifier,
			        particleSeparator, particleDataOrder, br);
			br.close();
			fr.close();
		}
		if (!isValid)
			throw new IllegalArgumentException(
			        "The source folder: "
			                + sourceFolder
			                + " has to contain at least 1 file containing the given file name identifier");

		for (final OmegaTrajectory t : this.tracks) {
			t.recalculateLength();
		}

		final Map<OmegaPlane, List<OmegaROI>> resultingParticles = new LinkedHashMap<OmegaPlane, List<OmegaROI>>(
				this.particles);
		final Map<OmegaROI, Map<String, Object>> resultingParticlesValues = new LinkedHashMap<OmegaROI, Map<String, Object>>(
				this.particlesValues);
		final List<OmegaTrajectory> resultingTrajectories = new ArrayList<OmegaTrajectory>(
				this.tracks);
		final OmegaImporterEventResultsParticleTracking evt = new OmegaImporterEventResultsParticleTracking(
		        this, this.container, resultingParticles,
				resultingTrajectories, resultingParticlesValues);
		this.fireEvent(evt);
	}

	private void importTrajectories(final String fileName,
	        final String trajectoryIdentifier, final String particleIdentifier,
			final boolean startAtOne, final String nonParticleIdentifier,
			final String particleSeparator,
	        final List<String> particleDataOrder, final BufferedReader br)
	        throws IOException, IllegalArgumentException {
		final String name1 = fileName.substring(0, fileName.lastIndexOf("."));
		OmegaTrajectory trajectory = null;
		if (trajectoryIdentifier == null) {
			trajectory = new OmegaTrajectory(-1);
			trajectory.setName(name1);
			this.tracks.add(trajectory);
		}
		String line = br.readLine();
		while (line != null) {
			if (line.isEmpty()) {
				line = br.readLine();
				continue;
			}
			if ((trajectoryIdentifier != null)
			        && line.startsWith(trajectoryIdentifier)) {
				final String name = OmegaStringUtilities.removeSymbols(line);
				String name2 = OmegaStringUtilities.replaceWhitespaces(name,
				        "_");
				if (name2.startsWith("_")) {
					name2 = name2.replaceFirst("_", "");
				}
				trajectory = new OmegaTrajectory(-1);
				trajectory.setName(name1 + "_" + name2);
				this.tracks.add(trajectory);
			}

			if ((nonParticleIdentifier != null)
			        && line.startsWith(nonParticleIdentifier)) {
				line = br.readLine();
				continue;
			}

			if ((particleIdentifier != null)
			        && line.startsWith(particleIdentifier)
			        && (trajectory != null)) {
				line = line.replaceFirst(particleIdentifier, "");
				final OmegaParticle p = this.importParticle(startAtOne,
				        particleSeparator, particleDataOrder, line);
				trajectory.addROI(p);
			} else {
				final OmegaParticle p = this.importParticle(startAtOne,
				        particleSeparator, particleDataOrder, line);
				trajectory.addROI(p);
			}
			line = br.readLine();
		}
	}

	private OmegaParticle importParticle(final boolean startAtOne,
	        final String particleSeparator,
	        final List<String> particleDataOrder, final String particleToImport)
	        throws IllegalArgumentException {
		final String[] particleData = particleToImport.split(particleSeparator);
		Integer frameIndex = null;
		Double x = null, y = null, intensity = null, probability = null;
		final Map<String, Object> particleValues = new LinkedHashMap<String, Object>();
		for (int i = 0; i < particleDataOrder.size(); i++) {
			final String order = particleDataOrder.get(i);
			final String data = particleData[i];
			if (order.equals(OmegaTracksImporter.PARTICLE_SEPARATOR)) {
				continue;
			} else if (order.equals(OmegaTracksImporter.PARTICLE_FRAMEINDEX)) {
				frameIndex = Integer.valueOf(data);
			} else if (order.equals(OmegaTracksImporter.PARTICLE_XCOORD)) {
				x = Double.valueOf(data);
			} else if (order.equals(OmegaTracksImporter.PARTICLE_YCOORD)) {
				y = Double.valueOf(data);
			} else if (order.equals(OmegaTracksImporter.PARTICLE_INTENSITY)) {
				intensity = Double.valueOf(data);
			} else if (order.equals(OmegaTracksImporter.PARTICLE_PROBABILITY)) {
				probability = Double.valueOf(data);
			} else {
				// TODO ADD CHOICE OF TYPE OF CONTENT IN GUI AND HERE
				particleValues.put(order, Double.valueOf(data));
			}
		}
		if ((frameIndex == null) || (x == null) || (y == null))
			throw new IllegalArgumentException(
			        "The line: "
			                + particleToImport
			                + " doesn't contain enough information for identifying a particle");
		OmegaParticle p = null;
		if (startAtOne) {
			frameIndex--;
		}
		if (intensity == null) {
			p = new OmegaParticle(frameIndex, x, y);
		} else if (probability == null) {
			p = new OmegaParticle(frameIndex, x, y, intensity);
		} else {
			p = new OmegaParticle(frameIndex, x, y, intensity, probability);
		}

		OmegaPlane frame;
		if (this.frames.containsKey(frameIndex)) {
			frame = this.frames.get(frameIndex);
		} else {
			frame = new OmegaPlane(frameIndex);
			this.frames.put(frameIndex, frame);
		}

		List<OmegaROI> rois;
		if (this.particles.containsKey(frame)) {
			rois = this.particles.get(frame);
		} else {
			rois = new ArrayList<OmegaROI>();
		}
		rois.add(p);
		this.particles.put(frame, rois);
		this.particlesValues.put(p, particleValues);

		return p;
	}

	public Map<Integer, OmegaPlane> getFrames() {
		return this.frames;
	}

	public Map<OmegaPlane, List<OmegaROI>> getParticles() {
		return this.particles;
	}

	public Map<OmegaROI, Map<String, Object>> getParticlesValues() {
		return this.particlesValues;
	}

	public List<OmegaTrajectory> getTracks() {
		return this.tracks;
	}

	public void reset() {
		this.frames.clear();
		this.particles.clear();
		this.particlesValues.clear();
		this.tracks.clear();
	}

	public void setContainer(final OmegaAnalysisRunContainer container) {
		this.container = container;
	}
}
