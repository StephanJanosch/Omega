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
package edu.umassmed.omega.sdSbalzariniPlugin.runnable;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import edu.umassmed.omega.commons.OmegaLogFileManager;
import edu.umassmed.omega.commons.constants.OmegaConstantsAlgorithmParameters;
import edu.umassmed.omega.commons.data.analysisRunElements.OmegaParameter;
import edu.umassmed.omega.commons.data.coreElements.OmegaImage;
import edu.umassmed.omega.commons.data.coreElements.OmegaImagePixels;
import edu.umassmed.omega.commons.data.coreElements.OmegaPlane;
import edu.umassmed.omega.commons.data.imageDBConnectionElements.OmegaGateway;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaROI;
import edu.umassmed.omega.commons.gui.interfaces.OmegaMessageDisplayerPanelInterface;
import edu.umassmed.omega.commons.plugins.OmegaPlugin;
import edu.umassmed.omega.omero.ij.testConvert.OmeroImageJTestConvert;

public class SDRunner2 implements SDRunnable {
	private final OmegaPlugin plugin;
	private static final String RUNNER = "Runner service: ";
	private final OmegaMessageDisplayerPanelInterface displayerPanel;
	
	private final Map<Integer, Map<OmegaImage, List<OmegaParameter>>> imagesToProcess;
	private final Map<Integer, Map<OmegaImage, Map<OmegaPlane, List<OmegaROI>>>> resultingParticles;
	private final Map<Integer, Map<OmegaImage, Map<OmegaROI, Map<String, Object>>>> resultingParticlesValues;
	
	private final OmegaGateway gateway;
	private final boolean isDebugMode, isPreview;
	private boolean isJobCompleted, isTerminated;
	
	public SDRunner2(final OmegaMessageDisplayerPanelInterface displayerPanel,
			final OmegaPlugin plugin) {
		this.displayerPanel = displayerPanel;
		this.plugin = plugin;
		
		this.imagesToProcess = null;
		this.gateway = null;
		
		this.isDebugMode = true;
		this.isPreview = false;
		this.isJobCompleted = false;
		
		this.resultingParticles = new LinkedHashMap<Integer, Map<OmegaImage, Map<OmegaPlane, List<OmegaROI>>>>();
		this.resultingParticlesValues = new LinkedHashMap<Integer, Map<OmegaImage, Map<OmegaROI, Map<String, Object>>>>();
	}
	
	public SDRunner2(
			final OmegaMessageDisplayerPanelInterface displayerPanel,
			final Map<Integer, Map<OmegaImage, List<OmegaParameter>>> imageToProcess,
			final OmegaGateway gateway, final boolean isPreview,
			final OmegaPlugin plugin) {
		this.displayerPanel = displayerPanel;
		this.plugin = plugin;
		
		this.imagesToProcess = new LinkedHashMap<>(imageToProcess);
		this.gateway = gateway;
		
		this.isDebugMode = false;
		this.isPreview = isPreview;
		this.isJobCompleted = false;
		
		this.resultingParticles = new LinkedHashMap<Integer, Map<OmegaImage, Map<OmegaPlane, List<OmegaROI>>>>();
		this.resultingParticlesValues = new LinkedHashMap<Integer, Map<OmegaImage, Map<OmegaROI, Map<String, Object>>>>();
	}
	
	@Override
	public boolean isJobCompleted() {
		return this.isJobCompleted;
	}
	
	@Override
	public void run() {
		// TODO move the call in the panel action listeners that setup the
		// thread
		// JPanelSPT.this.switchControlsStatus();
		// JPanelSPT.this.jButtonDisplayTracks.setEnabled(false);
		
		// ==============================
		// for each image to be processed
		// ==============================
		// final ArrayList<ImageDataHandler> images =
		// JPanelSPT.this.sptParametersHandler
		// .getImages();
		// final Iterator<ImageDataHandler> it = images.iterator();
		this.updateStatusSync(SDRunner2.RUNNER + " started.", false);
		
		if (this.isDebugMode) {
			this.debugModeRun();
		} else {
			this.normalModeRun();
		}
		
		this.isJobCompleted = true;
		
		if (this.isPreview) {
			this.updateStatusAsync(SDRunner2.RUNNER + " preview completed.",
					true);
		} else {
			this.updateStatusAsync(SDRunner2.RUNNER + " batch completed.", true);
		}
	}
	
	private void normalModeRun() {
		for (final Integer index : this.imagesToProcess.keySet()) {
			for (final OmegaImage image : this.imagesToProcess.get(index)
					.keySet()) {
				final List<OmegaParameter> parameters = this.imagesToProcess
						.get(index).get(image);
				final OmegaImagePixels defaultPixels = image.getDefaultPixels();
				defaultPixels.getOmeroId();
				final int width = defaultPixels.getSizeX();
				final int height = defaultPixels.getSizeY();
				final int sizeT = defaultPixels.getSizeT();
				
				if (sizeT < 2) {
					// TODO throw error and skip image or stop thread?
				}
				
				Integer radius = null;
				Double cutoff = null;
				Float percentile = null;
				Float threshold = null;
				Boolean percAbs = null;
				Integer z = null, c = null;
				for (int i = 0; i < parameters.size(); i++) {
					final OmegaParameter param = parameters.get(i);
					if (param.getName().equals(
							OmegaConstantsAlgorithmParameters.PARAM_RADIUS)) {
						radius = (Integer) param.getValue();
					} else if (param.getName().equals(
							OmegaConstantsAlgorithmParameters.PARAM_CUTOFF)) {
						cutoff = (Double) param.getValue();
					} else if (param.getName().equals(
							OmegaConstantsAlgorithmParameters.PARAM_PERCENTILE)) {
						threshold = (Float) param.getValue();
						percentile = (Float) param.getValue() / 100;
					} else if (param
							.getName()
							.equals(OmegaConstantsAlgorithmParameters.PARAM_PERCENTILE_ABS)) {
						percAbs = (Boolean) param.getValue();
					} else if (param.getName().equals(
							OmegaConstantsAlgorithmParameters.PARAM_ZSECTION)) {
						z = (Integer) param.getValue();
					} else if (param.getName().equals(
							OmegaConstantsAlgorithmParameters.PARAM_CHANNEL)) {
						c = (Integer) param.getValue();
					} else
						return;
				}
				
				if ((radius == null) || (cutoff == null)
						|| (percentile == null) || (threshold == null))
					// TODO ERROR
					return;
				
				if ((z == null) || (c == null))
					// TODO ERROR
					return;
				
				// final boolean error = false;
				
				final Map<OmegaPlane, List<OmegaROI>> frames = new LinkedHashMap<OmegaPlane, List<OmegaROI>>();
				
				this.updateStatusSync(SDRunner2.RUNNER
						+ " loading values for image " + image.getName(), false);
				
				final List<SDWorker2> workers = new ArrayList<SDWorker2>();
				final List<SDWorker2> completedWorkers = new ArrayList<SDWorker2>();
				final ExecutorService loaderExecutor = Executors
						.newFixedThreadPool(5);
				
				final OmeroImageJTestConvert oij = new OmeroImageJTestConvert();
				final ImagePlus imgPlus = oij.convert(image.getOmeroId(),
						this.gateway);
				imgPlus.setC(c + 1);
				imgPlus.setZ(z + 1);
				final ImageStack newImgStack = new ImageStack(width, height);
				for (int t = 0; t < sizeT; t++) {
					imgPlus.setT(t + 1);
					newImgStack.addSlice(imgPlus.getProcessor());
				}
				final ImagePlus newImgPlus = new ImagePlus(image.getName(),
						newImgStack);
				newImgPlus.setDimensions(1, 1, sizeT);
				Float globalMin = Float.MAX_VALUE, globalMax = 0F;
				final StackStatistics stack_stats = new StackStatistics(
						newImgPlus);
				globalMax = (float) stack_stats.max;
				globalMin = (float) stack_stats.min;
				// globalMin = (float) newImgPlus.getStatistics().min;
				// globalMax = (float) newImgPlus.getStatistics().max;
				
				if (this.isPreview) {
					final int t = defaultPixels.getSelectedT();
					final int realT = t + 1;
					OmegaLogFileManager.appendToPluginLog(this.plugin,
							"Creating preview (frame " + realT
									+ ") SDWorker for " + image.getName());
					final ImageStack lis = new ImageStack(width, height);
					newImgPlus.setT(realT);
					final ImageProcessor ip = newImgPlus.getProcessor();
					lis.addSlice(ip);
					final SDWorker2 worker = new SDWorker2(lis, defaultPixels,
							t, radius, cutoff, percentile, threshold, percAbs,
							c, z);
					worker.setGlobalMin(globalMin);
					worker.setGlobalMax(globalMax);
					completedWorkers.add(worker);
					loaderExecutor.execute(worker);
				} else {
					OmegaLogFileManager.appendToPluginLog(
							this.plugin,
							"Creating " + sizeT + " SDWorkers for "
									+ image.getName());
					for (int t = 0; t < sizeT; t++) {
						final int realT = t + 1;
						final ImageStack lis = new ImageStack(width, height);
						newImgPlus.setT(realT);
						final ImageProcessor ip = newImgPlus.getProcessor();
						lis.addSlice(ip);
						final SDWorker2 worker = new SDWorker2(lis,
								defaultPixels, t, radius, cutoff, percentile,
								threshold, percAbs, c, z);
						worker.setGlobalMin(globalMin);
						worker.setGlobalMax(globalMax);
						workers.add(worker);
						loaderExecutor.execute(worker);
					}
				}
				
				final boolean frameAdded = this.waitForExecutor(loaderExecutor,
						workers, completedWorkers, image.getName(), sizeT,
						false);
				
				// for (final SDWorker loader : workers) {
				// final ImageProcessor ip = loader.getProcessor();
				// if (globalMin > ip.getMin()) {
				// globalMin = Float.valueOf(String.valueOf(ip.getMin()));
				// }
				// if (globalMax < ip.getMax()) {
				// globalMax = Float.valueOf(String.valueOf(ip.getMax()));
				// }
				// }
				
				// this.updateStatusSync(SDRunner2.RUNNER + " processing image "
				// + image.getName(), false);
				
				this.orderList(completedWorkers);
				for (final SDWorker2 worker : completedWorkers) {
					image.getDefaultPixels().addFrame(worker.getC(),
							worker.getZ(), worker.getFrame());
				}
				
				if (frameAdded) {
					image.getDefaultPixels().orderFrames();
				}
				
				final Map<OmegaROI, Map<String, Object>> particleValues = new LinkedHashMap<OmegaROI, Map<String, Object>>();
				OmegaLogFileManager.appendToPluginLog(this.plugin,
						"Pulling results from SDWorkers");
				// int counter = 0;
				for (int counter = 0; counter < completedWorkers.size(); counter++) {
					final SDWorker2 worker = completedWorkers.get(counter);
					// for (final SDWorker2 worker : completedWorkers) {
					final OmegaPlane frame = worker.getFrame();
					if (frame.getIndex() != counter) {
						OmegaLogFileManager.appendToPluginLog(this.plugin,
								"Problem in frame " + frame.getIndex());
					}
					// counter++;
					final List<OmegaROI> particles = worker
							.getResultingParticles();
					particleValues
							.putAll(worker.getParticlesAdditionalValues());
					frames.put(frame, particles);
				}
				
				final Map<OmegaImage, Map<OmegaPlane, List<OmegaROI>>> imageAndFrames = new LinkedHashMap<OmegaImage, Map<OmegaPlane, List<OmegaROI>>>();
				imageAndFrames.put(image, frames);
				
				final Map<OmegaImage, Map<OmegaROI, Map<String, Object>>> imageAndValues = new LinkedHashMap<OmegaImage, Map<OmegaROI, Map<String, Object>>>();
				imageAndValues.put(image, particleValues);
				
				this.resultingParticles.put(index, imageAndFrames);
				this.resultingParticlesValues.put(index, imageAndValues);
				
				if (this.isTerminated)
					return;
				
				// if (error) {
				// JOptionPane.showMessageDialog(null,
				// OmegaConstantsError.ERROR_DURING_SPT_RUN,
				// OmegaConstants.OMEGA_TITLE,
				// JOptionPane.ERROR_MESSAGE);
				// }
			}
		}
	}
	
	private boolean waitForExecutor(final ExecutorService exec,
			final List<SDWorker2> workers,
			final List<SDWorker2> completedWorkers, final String imgName,
			final int sizeT, final boolean isLoading) {
		int completed = 0;
		boolean frameAdded = false;
		while (!exec.isTerminated()) {
			if (this.isTerminated) {
				for (final SDWorker2 runnable : workers) {
					runnable.terminate();
					// TODO to be fixed because if everything terminate
					// there are no results to get, find a solution
				}
			}
			if (workers.isEmpty()) {
				exec.shutdown();
			}
			for (final SDWorker2 runnable : workers) {
				if (!runnable.isJobCompleted()) {
					continue;
				}
				if (runnable.isFrameAdded()) {
					frameAdded = true;
				}
				completed++;
				final StringBuffer sb = new StringBuffer();
				sb.append(SDRunner2.RUNNER + " image " + imgName);
				if (isLoading) {
					sb.append(", frame(s) loaded ");
				} else {
					sb.append(", frame(s)  completed ");
				}
				sb.append(completed + "/" + sizeT);
				this.updateStatusSync(sb.toString(), false);
				completedWorkers.add(runnable);
			}
			workers.removeAll(completedWorkers);
		}
		
		return frameAdded;
	}
	
	private void orderList(final List<SDWorker2> workers) {
		Collections.sort(workers, new Comparator<SDWorker2>() {
			@Override
			public int compare(final SDWorker2 o1, final SDWorker2 o2) {
				final int i1 = o1.getFrame().getIndex();
				final int i2 = o2.getFrame().getIndex();
				if (i1 < i2)
					return -1;
				else if (i1 > i2)
					return 1;
				return 0;
			}
		});
	}
	
	private void debugModeRun() {
		
	}
	
	public Map<Integer, Map<OmegaImage, List<OmegaParameter>>> getImageParameters() {
		return this.imagesToProcess;
	}
	
	public Map<Integer, Map<OmegaImage, Map<OmegaPlane, List<OmegaROI>>>> getImageResultingParticles() {
		return this.resultingParticles;
	}
	
	public Map<Integer, Map<OmegaImage, Map<OmegaROI, Map<String, Object>>>> getImageParticlesAdditionalValues() {
		return this.resultingParticlesValues;
	}
	
	@Override
	public void terminate() {
		this.isTerminated = true;
	}
	
	private void updateStatusSync(final String msg, final boolean ended) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					SDRunner2.this.displayerPanel
							.updateMessageStatus(new SDMessageEvent(msg,
									SDRunner2.this, ended,
									SDRunner2.this.isPreview));
				}
			});
		} catch (final InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	private void updateStatusAsync(final String msg, final boolean ended) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SDRunner2.this.displayerPanel
						.updateMessageStatus(new SDMessageEvent(msg,
								SDRunner2.this, ended, SDRunner2.this.isPreview));
			}
		});
	}
}
