/*******************************************************************************
 * Copyright (C) 2014 University of Massachusetts Medical School Alessandro
 * Rigano (Program in Molecular Medicine) Caterina Strambio De Castillia
 * (Program in Molecular Medicine)
 *
 * Created by the Open Microscopy Environment inteGrated Analysis (OMEGA) team:
 * Alex Rigano, Caterina Strambio De Castillia, Jasmine Clark, Vanni Galli,
 * Raffaello Giulietti, Loris Grossi, Eric Hunter, Tiziano Leidi, Jeremy Luban,
 * Ivo ErrorIndex and Mario Valle.
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
package edu.umassmed.omega.snrSbalzariniPlugin.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.umassmed.omega.commons.OmegaLogFileManager;
import edu.umassmed.omega.commons.constants.OmegaConstants;
import edu.umassmed.omega.commons.constants.OmegaConstantsAlgorithmParameters;
import edu.umassmed.omega.commons.constants.OmegaGUIConstants;
import edu.umassmed.omega.commons.data.analysisRunElements.OmegaAnalysisRun;
import edu.umassmed.omega.commons.data.analysisRunElements.OmegaAnalysisRunContainerInterface;
import edu.umassmed.omega.commons.data.analysisRunElements.OmegaParameter;
import edu.umassmed.omega.commons.data.analysisRunElements.OmegaParticleDetectionRun;
import edu.umassmed.omega.commons.data.analysisRunElements.OmegaSNRRun;
import edu.umassmed.omega.commons.data.analysisRunElements.OrphanedAnalysisContainer;
import edu.umassmed.omega.commons.data.coreElements.OmegaElement;
import edu.umassmed.omega.commons.data.coreElements.OmegaImage;
import edu.umassmed.omega.commons.data.coreElements.OmegaPlane;
import edu.umassmed.omega.commons.data.imageDBConnectionElements.OmegaGateway;
import edu.umassmed.omega.commons.data.trajectoryElements.OmegaROI;
import edu.umassmed.omega.commons.eventSystem.events.OmegaMessageEvent;
import edu.umassmed.omega.commons.eventSystem.events.OmegaPluginEvent;
import edu.umassmed.omega.commons.eventSystem.events.OmegaPluginEventResultsSNR;
import edu.umassmed.omega.commons.eventSystem.events.OmegaPluginEventSelectionAnalysisRun;
import edu.umassmed.omega.commons.eventSystem.events.OmegaPluginEventSelectionImage;
import edu.umassmed.omega.commons.exceptions.OmegaPluginExceptionStatusPanel;
import edu.umassmed.omega.commons.gui.GenericPanel;
import edu.umassmed.omega.commons.gui.GenericPluginPanel;
import edu.umassmed.omega.commons.gui.GenericStatusPanel;
import edu.umassmed.omega.commons.gui.GenericTrackingResultsPanel;
import edu.umassmed.omega.commons.gui.interfaces.GenericElementInformationContainerInterface;
import edu.umassmed.omega.commons.gui.interfaces.OmegaMessageDisplayerPanelInterface;
import edu.umassmed.omega.commons.plugins.OmegaAlgorithmPlugin;
import edu.umassmed.omega.commons.plugins.OmegaPlugin;
import edu.umassmed.omega.commons.utilities.OmegaFileUtilities;
import edu.umassmed.omega.snrSbalzariniPlugin.SNRConstants;
import edu.umassmed.omega.snrSbalzariniPlugin.runnable.SNRMessageEvent;
import edu.umassmed.omega.snrSbalzariniPlugin.runnable.SNRRunner;
import edu.umassmed.omega.trajectoriesRelinkingPlugin.TRConstants;

public class SNRPluginPanel extends GenericPluginPanel implements
		OmegaMessageDisplayerPanelInterface,
		GenericElementInformationContainerInterface {

	private static final long serialVersionUID = -5740459087763362607L;

	private final OmegaGateway gateway;

	private JComboBox<String> images_cmb;

	// private OmeroListPanel projectListPanel;
	private SNRLoadedDataBrowserPanel loadedDataBrowserPanel;
	private SNRQueueRunBrowserPanel queueRunBrowserPanel;

	private JButton addToProcess_butt, removeFromProcess_butt;
	private JButton processBatch_butt, processRealTime_butt;

	private boolean popImages;

	private final boolean popParticles;

	private boolean isHandlingEvent;

	private JSplitPane mainSplitPane, browserSplitPane;
	private JTabbedPane tabbedPane;
	private GenericStatusPanel statusPanel;
	private SNRRunPanel runPanel;
	private GenericTrackingResultsPanel localResultsPanel,
			globalFrameResultsPanel, globalImageResultsPanel;

	private List<OmegaImage> images;
	private OrphanedAnalysisContainer orphanedAnalysis;

	private OmegaAnalysisRunContainerInterface selectedImage;
	private OmegaParticleDetectionRun selectedParticleDetectionRun;
	private OmegaSNRRun selectedSNRRun;

	private List<OmegaAnalysisRun> loadedAnalysisRuns;

	private final Map<Integer, Map<OmegaParticleDetectionRun, List<OmegaElement>>> selections;
	private final Map<Integer, Map<OmegaParticleDetectionRun, List<OmegaParameter>>> particlesToProcess;

	private Thread snrThread;
	private SNRRunner snrRunner;

	private boolean isRunningBatch;

	private JPanel topPanel;
	private JMenuItem hideDataSelection_mItm;

	public SNRPluginPanel(final RootPaneContainer parent,
			final OmegaPlugin plugin, final OmegaGateway gateway,
			final List<OmegaImage> images,
			final OrphanedAnalysisContainer orphanedAnalysis,
			final List<OmegaAnalysisRun> analysisRuns, final int index) {
		super(parent, plugin, index);

		this.gateway = gateway;

		this.particlesToProcess = new LinkedHashMap<Integer, Map<OmegaParticleDetectionRun, List<OmegaParameter>>>();
		this.selections = new LinkedHashMap<Integer, Map<OmegaParticleDetectionRun, List<OmegaElement>>>();

		this.images = images;
		this.orphanedAnalysis = orphanedAnalysis;
		this.loadedAnalysisRuns = analysisRuns;

		this.popImages = false;
		this.popParticles = false;
		this.isHandlingEvent = false;

		this.selectedImage = null;
		this.selectedParticleDetectionRun = null;

		this.setPreferredSize(new Dimension(750, 500));
		this.setLayout(new BorderLayout());
		// this.createMenu();
		this.createAndAddWidgets();
		// this.loadedDataBrowserPanel.updateTree(images);
		this.createMenu();

		this.addListeners();

		this.populateImagesCombo();
	}

	private void createMenu() {
		final JMenuBar menuBar = this.getMenu();
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			final JMenu menu = menuBar.getMenu(i);
			if (!menu.getText().equals(OmegaGUIConstants.MENU_VIEW)) {
				continue;
			}
			this.hideDataSelection_mItm = new JMenuItem(
					OmegaGUIConstants.MENU_VIEW_HIDE_DATA_SELECTION);
			menu.add(this.hideDataSelection_mItm);
		}
	}

	private void createAndAddWidgets() {
		this.topPanel = new JPanel();
		this.topPanel.setLayout(new GridLayout(1, 1));

		final JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout());
		final JLabel lbl1 = new JLabel(TRConstants.SELECT_IMAGE);
		lbl1.setPreferredSize(OmegaConstants.TEXT_SIZE);
		p1.add(lbl1, BorderLayout.WEST);
		this.images_cmb = new JComboBox<String>();
		this.images_cmb.setMaximumRowCount(OmegaConstants.COMBOBOX_MAX_OPTIONS);
		this.images_cmb.setEnabled(false);
		p1.add(this.images_cmb, BorderLayout.CENTER);
		this.topPanel.add(p1);

		this.add(this.topPanel, BorderLayout.NORTH);

		this.loadedDataBrowserPanel = new SNRLoadedDataBrowserPanel(
				this.getParentContainer(), this);

		this.queueRunBrowserPanel = new SNRQueueRunBrowserPanel(
				this.getParentContainer(), this);

		final JPanel browserPanel = new JPanel();
		browserPanel.setLayout(new BorderLayout());

		this.browserSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.browserSplitPane.setLeftComponent(this.loadedDataBrowserPanel);
		this.browserSplitPane.setRightComponent(this.queueRunBrowserPanel);

		browserPanel.add(this.browserSplitPane, BorderLayout.CENTER);

		final JPanel browserButtonPanel = new JPanel();
		browserButtonPanel.setLayout(new FlowLayout());
		final InputStream s1 = OmegaFileUtilities
				.getImageFilename("green_plus.png");
		ImageIcon addIcon = null;
		try {
			addIcon = new ImageIcon(ImageIO.read(s1));
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.addToProcess_butt = new JButton(addIcon);
		this.addToProcess_butt.setPreferredSize(new Dimension(30, 30));
		final InputStream s2 = OmegaFileUtilities
				.getImageFilename("red_minus.png");
		ImageIcon removeIcon = null;
		try {
			removeIcon = new ImageIcon(ImageIO.read(s2));
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.removeFromProcess_butt = new JButton(removeIcon);
		this.removeFromProcess_butt.setPreferredSize(new Dimension(30, 30));

		this.setAddAndRemoveButtonsEnabled(false);

		browserButtonPanel.add(this.addToProcess_butt);
		browserButtonPanel.add(this.removeFromProcess_butt);

		browserPanel.add(browserButtonPanel, BorderLayout.SOUTH);

		this.tabbedPane = new JTabbedPane(SwingConstants.TOP,
				JTabbedPane.WRAP_TAB_LAYOUT);

		// TODO create panel for parameters
		this.runPanel = new SNRRunPanel(this.getParentContainer(),
				this.gateway, this);
		// final JScrollPane scrollPaneRun = new JScrollPane(this.runPanel);
		this.tabbedPane.add(OmegaGUIConstants.PLUGIN_RUN_DEFINITION,
				this.runPanel);

		this.localResultsPanel = new GenericTrackingResultsPanel(
				this.getParentContainer());
		this.tabbedPane.add(SNRConstants.ROI_RESULTS_TABNAME,
				this.localResultsPanel);

		this.globalFrameResultsPanel = new GenericTrackingResultsPanel(
				this.getParentContainer());
		this.tabbedPane.add(SNRConstants.PLANE_RESULTS_TABNAME,
				this.globalFrameResultsPanel);

		this.globalImageResultsPanel = new GenericTrackingResultsPanel(
				this.getParentContainer());
		this.tabbedPane.add(SNRConstants.IMAGE_RESULTS_TABNAME,
				this.globalImageResultsPanel);

		this.mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		this.mainSplitPane.setLeftComponent(browserPanel);
		this.mainSplitPane.setRightComponent(this.tabbedPane);

		this.add(this.mainSplitPane, BorderLayout.CENTER);

		final JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());

		this.statusPanel = new GenericStatusPanel(2);

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout());

		this.processRealTime_butt = new JButton("Process in real time");
		this.processRealTime_butt.setEnabled(false);
		// buttonsPanel.add(this.processRealTime_butt);

		this.processBatch_butt = new JButton(SNRConstants.EXECUTE_BUTTON);
		buttonsPanel.add(this.processBatch_butt);

		this.setProcessButtonsEnabled(false);

		bottomPanel.add(buttonsPanel, BorderLayout.NORTH);
		bottomPanel.add(this.statusPanel, BorderLayout.SOUTH);

		this.add(bottomPanel, BorderLayout.SOUTH);
	}

	private void addListeners() {
		this.tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent evt) {

			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent evt) {
				SNRPluginPanel.this.handleResize();
			}
		});
		this.images_cmb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SNRPluginPanel.this.selectImage();
			}
		});
		this.addToProcess_butt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SNRPluginPanel.this.addToProcessList();
			}
		});
		this.removeFromProcess_butt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SNRPluginPanel.this.removeFromProcessList();
			}
		});
		this.processBatch_butt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				SNRPluginPanel.this.processBatch();
			}
		});
		this.hideDataSelection_mItm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SNRPluginPanel.this.handleHideDataSelection();
			}
		});
	}

	private void handleHideDataSelection() {
		if (this.hideDataSelection_mItm.getText().equals(
				OmegaGUIConstants.MENU_VIEW_HIDE_DATA_SELECTION)) {
			this.remove(this.topPanel);
			this.hideDataSelection_mItm
					.setText(OmegaGUIConstants.MENU_VIEW_SHOW_DATA_SELECTION);
		} else {
			this.add(this.topPanel, BorderLayout.NORTH);
			this.hideDataSelection_mItm
					.setText(OmegaGUIConstants.MENU_VIEW_HIDE_DATA_SELECTION);
		}
		this.revalidate();
		this.repaint();
	}

	private void handleResize() {
		this.browserSplitPane.setDividerLocation(0.5);
		this.mainSplitPane.setDividerLocation(0.25);
	}

	private void addToProcessList() {
		this.updateImagesToProcess(0);
		this.loadedDataBrowserPanel.deselect();
		this.setAddAndRemoveButtonsEnabled(false);
	}

	private void removeFromProcessList() {
		this.updateImagesToProcess(1);
		this.loadedDataBrowserPanel.deselect();
		this.queueRunBrowserPanel.deselect();
		this.setAddAndRemoveButtonsEnabled(false);
	}

	private void processBatch() {
		this.isRunningBatch = true;
		this.setAddAndRemoveButtonsEnabled(false);
		this.setProcessButtonsEnabled(false);
		this.runPanel.setFieldsEnalbed(false);
		this.snrRunner = new SNRRunner(this, this.particlesToProcess,
				this.gateway);
		this.snrThread = new Thread(this.snrRunner);
		this.snrThread.setName(this.snrRunner.getClass().getSimpleName());
		OmegaLogFileManager.registerAsExceptionHandlerOnThread(this.snrThread);
		this.snrThread.start();
	}

	private void updateImagesToProcess(final int action) {
		switch (action) {
			case 1:
				final Integer selectedIndex = this.queueRunBrowserPanel
						.getSelectedIndex();
				this.particlesToProcess.remove(selectedIndex);
				this.selections.remove(selectedIndex);
				// this.particlesToProcess
				// .remove(this.selectedParticleDetectionRun);
				// this.selections.remove(this.selectedParticleDetectionRun);
				break;
			default:
				final List<OmegaParameter> params = this.runPanel
						.getParameters();
				if (params != null) {
					final String[] errors = this.runPanel.getParametersError();
					final StringBuffer exceptionError = new StringBuffer();
					int countErrors = 0;
					for (int index = 0; index < errors.length; index++) {
						final String error = errors[index];
						if (error == null) {
							continue;
						}
						countErrors++;
						exceptionError.append(error);
						if (index != (errors.length - 1)) {
							exceptionError.append(" & ");
						}
					}
					if (countErrors > 0) {
						final StringBuffer buf = new StringBuffer();
						buf.append("Error in parameters -> ");
						buf.append(exceptionError.toString());
						try {
							this.statusPanel.updateStatus(1, buf.toString());
						} catch (final OmegaPluginExceptionStatusPanel ex) {
							OmegaLogFileManager.handlePluginException(
									this.getPlugin(), ex, true);
						}
						break;
					}
					// Lanciare eccezione o printare errore a schermo
					// throw new OmegaAlgorithmParametersTypeException(
					// this.getPlugin(), exceptionError.toString());
				}
				final Integer index = this.particlesToProcess.size();
				final Map<OmegaParticleDetectionRun, List<OmegaParameter>> detAndParams = new LinkedHashMap<OmegaParticleDetectionRun, List<OmegaParameter>>();
				detAndParams.put(this.selectedParticleDetectionRun, params);
				this.particlesToProcess.put(index, detAndParams);
				final List<OmegaElement> selection = new ArrayList<OmegaElement>();
				selection.add((OmegaElement) this.selectedImage);
				selection.add(this.selectedParticleDetectionRun);
				final Map<OmegaParticleDetectionRun, List<OmegaElement>> detAndSelections = new LinkedHashMap<OmegaParticleDetectionRun, List<OmegaElement>>();
				detAndSelections.put(this.selectedParticleDetectionRun,
						selection);
				this.selections.put(index, detAndSelections);
				break;
		}
		this.queueRunBrowserPanel.updateTree(this.particlesToProcess);
		if (this.particlesToProcess.size() == 0) {
			this.setProcessButtonsEnabled(false);
		} else {
			this.setProcessButtonsEnabled(true);
		}
	}

	private void updateRunnerEnded() {
		final List<OmegaPluginEvent> events = new ArrayList<OmegaPluginEvent>();
		if (!this.snrRunner.isJobCompleted())
			return;

		for (final Integer index : this.snrRunner.getParticleToProcess()
				.keySet()) {
			final Map<OmegaParticleDetectionRun, List<OmegaParameter>> processedParticles = this.snrRunner
					.getParticleToProcess().get(index);

			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageAvgCenterSignal = this.snrRunner
					.getResultingImageAverageCenterSignal().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageAvgPeakSignal = this.snrRunner
					.getResultingImageAveragePeakSignal().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageAvgMeanSignal = this.snrRunner
					.getResultingImageAverageMeanSignal().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageNoises = this.snrRunner
					.getResultingImageNoise().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageBackgrounds = this.snrRunner
					.getResultingImageBackground().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageAvgSNR = this.snrRunner
					.getResultingImageAverageSNR().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageMinSNR = this.snrRunner
					.getResultingImageMinimumSNR().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageMaxSNR = this.snrRunner
					.getResultingImageMaximumSNR().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageAvgErrorIndexSNR = this.snrRunner
					.getResultingImageAverageErrorIndexSNR().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageMinErrorIndexSNR = this.snrRunner
					.getResultingImageMinimumErrorIndexSNR().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaPlane, Double>> imageMaxErrorIndexSNR = this.snrRunner
					.getResultingImageMaximumErrorIndexSNR().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Integer>> localCentralSignals = this.snrRunner
					.getResultingLocalCentralSignals().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Double>> localMeanSignals = this.snrRunner
					.getResultingLocalMeanSignals().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Integer>> localSignalSizes = this.snrRunner
					.getResultingLocalSignalSizes().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Integer>> localPeakSignals = this.snrRunner
					.getResultingLocalPeakSignals().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Double>> localBackgrounds = this.snrRunner
					.getResultingLocalBackgrounds().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Double>> localNoises = this.snrRunner
					.getResultingLocalNoises().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Double>> localSNRs = this.snrRunner
					.getResultingLocalSNRs().get(index);
			final Map<OmegaParticleDetectionRun, Map<OmegaROI, Double>> localErrorIndexSNRs = this.snrRunner
					.getResultingLocalErrorIndexSNRs().get(index);
			final Map<OmegaParticleDetectionRun, Double> avgCenterSignal = this.snrRunner
					.getResultingAverageCenterSignal().get(index);
			final Map<OmegaParticleDetectionRun, Double> avgPeakSignal = this.snrRunner
					.getResultingAveragePeakSignal().get(index);
			final Map<OmegaParticleDetectionRun, Double> avgMeanSignal = this.snrRunner
					.getResultingAverageMeanSignal().get(index);
			final Map<OmegaParticleDetectionRun, Double> background = this.snrRunner
					.getResultingBackground().get(index);
			final Map<OmegaParticleDetectionRun, Double> noise = this.snrRunner
					.getResultingNoise().get(index);
			final Map<OmegaParticleDetectionRun, Double> avgSNR = this.snrRunner
					.getResultingAvgSNR().get(index);
			final Map<OmegaParticleDetectionRun, Double> maxSNR = this.snrRunner
					.getResultingMaxSNR().get(index);
			final Map<OmegaParticleDetectionRun, Double> minSNR = this.snrRunner
					.getResultingMinSNR().get(index);
			final Map<OmegaParticleDetectionRun, Double> avgErrorIndexSNR = this.snrRunner
					.getResultingAvgErrorIndexSNR().get(index);
			final Map<OmegaParticleDetectionRun, Double> minErrorIndexSNR = this.snrRunner
					.getResultingMinErrorIndexSNR().get(index);
			final Map<OmegaParticleDetectionRun, Double> maxErrorIndexSNR = this.snrRunner
					.getResultingMaxErrorIndexSNR().get(index);

			for (final OmegaParticleDetectionRun spotDetectionRun : processedParticles
					.keySet()) {
				final List<OmegaParameter> params = processedParticles
						.get(spotDetectionRun);
				final List<OmegaElement> selection = this.selections.get(index)
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> avgCenterSignals = imageAvgCenterSignal
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> avgPeakSignals = imageAvgPeakSignal
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> avgMeanSignals = imageAvgMeanSignal
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> noises = imageNoises
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> backgrounds = imageBackgrounds
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> averageSNR = imageAvgSNR
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> minimumSNR = imageMinSNR
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> maximumSNR = imageMaxSNR
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> averageErrorIndexSNR = imageAvgErrorIndexSNR
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> minimumErrorIndexSNR = imageMinErrorIndexSNR
						.get(spotDetectionRun);
				final Map<OmegaPlane, Double> maximumErrorIndexSNR = imageMaxErrorIndexSNR
						.get(spotDetectionRun);
				final Map<OmegaROI, Integer> lCentralSignals = localCentralSignals
						.get(spotDetectionRun);
				final Map<OmegaROI, Double> lMeanSignals = localMeanSignals
						.get(spotDetectionRun);
				final Map<OmegaROI, Integer> lSignalSizes = localSignalSizes
						.get(spotDetectionRun);
				final Map<OmegaROI, Integer> lPeakSignals = localPeakSignals
						.get(spotDetectionRun);
				final Map<OmegaROI, Double> lBackgrounds = localBackgrounds
						.get(spotDetectionRun);
				final Map<OmegaROI, Double> lNoises = localNoises
						.get(spotDetectionRun);
				final Map<OmegaROI, Double> lSNRs = localSNRs
						.get(spotDetectionRun);
				final Map<OmegaROI, Double> lErrorIndexSNRs = localErrorIndexSNRs
						.get(spotDetectionRun);
				final Double lavgCenterSignal = avgCenterSignal
						.get(spotDetectionRun);
				final Double lavgPeakSignal = avgPeakSignal
						.get(spotDetectionRun);
				final Double lavgMeanSignal = avgMeanSignal
						.get(spotDetectionRun);
				final Double lbackground = background.get(spotDetectionRun);
				final Double lnoise = noise.get(spotDetectionRun);
				final Double lavgSNR = avgSNR.get(spotDetectionRun);
				final Double lminSNR = minSNR.get(spotDetectionRun);
				final Double lmaxSNR = maxSNR.get(spotDetectionRun);
				final Double lavgErrorIndeXSNR = avgErrorIndexSNR
						.get(spotDetectionRun);
				final Double lminErrorIndeXSNR = minErrorIndexSNR
						.get(spotDetectionRun);
				final Double lmaxErrorIndeXSNR = maxErrorIndexSNR
						.get(spotDetectionRun);

				final OmegaPluginEventResultsSNR snrResultsEvt = new OmegaPluginEventResultsSNR(
						this.getPlugin(), selection, spotDetectionRun, params,
						avgCenterSignals, avgPeakSignals, avgMeanSignals,
						backgrounds, noises, averageSNR, minimumSNR,
						maximumSNR, averageErrorIndexSNR, minimumErrorIndexSNR,
						maximumErrorIndexSNR, lCentralSignals, lMeanSignals,
						lSignalSizes, lPeakSignals, lBackgrounds, lNoises,
						lSNRs, lErrorIndexSNRs, lavgCenterSignal,
						lavgPeakSignal, lavgMeanSignal, lbackground, lnoise,
						lavgSNR, lminSNR, lmaxSNR, lavgErrorIndeXSNR,
						lminErrorIndeXSNR, lmaxErrorIndeXSNR);
				events.add(snrResultsEvt);
				this.particlesToProcess.remove(index);
				this.selections.remove(index);
				this.queueRunBrowserPanel.updateTree(this.particlesToProcess);

				// this.getPlugin().fireEvent(snrResultsEvt);
			}
		}
		if (this.snrThread.isAlive()) {
			try {
				this.snrThread.join();
			} catch (final InterruptedException ex) {
				OmegaLogFileManager.handlePluginException(this.getPlugin(), ex,
						true);
			}
		}
		this.setEnabled(true);

		this.setProcessButtonsEnabled(true);
		this.runPanel.setFieldsEnalbed(true);
		this.resetStatusMessages();
		this.isRunningBatch = false;
		
		for (final OmegaPluginEvent event : events) {
			this.getPlugin().fireEvent(event);
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException ex) {
				OmegaLogFileManager.handlePluginException(this.getPlugin(), ex,
						true);
				// ex.printStackTrace();
			}
		}
	}

	private void resetStatusMessages() {
		try {
			this.statusPanel.updateStatus(0, "Plugin ready");
		} catch (final OmegaPluginExceptionStatusPanel ex) {
			OmegaLogFileManager.handlePluginException(this.getPlugin(), ex,
					true);
		}
	}

	private void selectImage() {
		this.runPanel.updateImageFields(null);
		this.runPanel.updateAnalysisFields(null);
		if (this.popImages)
			return;
		final int index = this.images_cmb.getSelectedIndex();
		this.selectedImage = null;
		if (index == -1) {
			this.loadedDataBrowserPanel.updateTree(null, null);
			this.queueRunBrowserPanel.updateTree(null);
			return;
		}
		if ((this.images == null) || (index >= this.images.size())) {
			this.selectedImage = this.orphanedAnalysis;
		} else {
			this.selectedImage = this.images.get(index);
		}
		if (!this.isHandlingEvent) {
			this.fireEventSelectionImage();
		}
		this.runPanel.updateImageFields((OmegaImage) this.selectedImage);

		// final List<OmegaAnalysisRun> analysisRuns = new ArrayList<>();
		// for (final OmegaAnalysisRun analysisRun : this.selectedImage
		// .getAnalysisRuns()) {
		// if (!(analysisRun instanceof OmegaParticleDetectionRun)) {
		// continue;
		// }
		// if (this.loadedAnalysisRuns.contains(analysisRun)) {
		// analysisRuns.add(analysisRun);
		// }
		// }

		this.loadedDataBrowserPanel.updateTree(
				this.selectedImage.getAnalysisRuns(), this.loadedAnalysisRuns);
		this.particlesToProcess.clear();
		this.queueRunBrowserPanel.updateTree(null);
		this.setAddAndRemoveButtonsEnabled(false);
		// this.populateTrajectoriesCombo();
		// this.trPanel.setPixelSizes(this.selectedImage.getDefaultPixels()
		// .getPixelSizeX(), this.selectedImage.getDefaultPixels()
		// .getPixelSizeY());
	}

	@Override
	public void updateParentContainer(final RootPaneContainer parent) {
		super.updateParentContainer(parent);
		this.loadedDataBrowserPanel.updateParentContainer(parent);
		this.queueRunBrowserPanel.updateParentContainer(parent);
		this.runPanel.updateParentContainer(parent);
		this.localResultsPanel.updateParentContainer(parent);
		this.globalFrameResultsPanel.updateParentContainer(parent);
		this.globalImageResultsPanel.updateParentContainer(parent);
	}

	@Override
	public void onCloseOperation() {

	}

	public void updateCombos(final List<OmegaImage> images,
			final OrphanedAnalysisContainer orphanedAnalysis,
			final List<OmegaAnalysisRun> analysisRuns) {
		this.isHandlingEvent = true;
		this.images = images;
		this.orphanedAnalysis = orphanedAnalysis;
		this.loadedAnalysisRuns = analysisRuns;

		this.populateImagesCombo();
		this.isHandlingEvent = false;
	}

	private void populateImagesCombo() {
		this.popImages = true;
		this.images_cmb.removeAllItems();
		this.selectedImage = null;
		this.images_cmb.setSelectedIndex(-1);
		if (((this.images == null) || this.images.isEmpty())
		/* && this.orphanedAnalysis.isEmpty() */) {
			this.images_cmb.setEnabled(false);
			this.loadedDataBrowserPanel.updateTree(null, null);
			this.queueRunBrowserPanel.updateTree(null);
			this.repaint();
			this.popImages = false;
			return;
		}

		this.images_cmb.setEnabled(true);

		if (this.images != null) {
			for (final OmegaImage image : this.images) {
				this.images_cmb.addItem(image.getName());
			}
		}
		// this.images_cmb.addItem(OmegaGUIConstants.PLUGIN_ORPHANED_ANALYSES);
		this.popImages = false;

		if (this.images_cmb.getItemCount() > 0) {
			this.images_cmb.setSelectedIndex(0);
		} else {
			this.images_cmb.setSelectedIndex(-1);
		}
	}

	private void fireEventSelectionImage() {
		final OmegaPluginEvent event = new OmegaPluginEventSelectionImage(
				this.getPlugin(), this.selectedImage);
		this.getPlugin().fireEvent(event);
	}

	private void fireEventSelectionParticleDetectionRun() {
		final OmegaPluginEvent event = new OmegaPluginEventSelectionAnalysisRun(
				this.getPlugin(), this.selectedParticleDetectionRun);
		this.getPlugin().fireEvent(event);
	}

	private void fireEventSelectionSNRRun() {
		final OmegaPluginEvent event = new OmegaPluginEventSelectionAnalysisRun(
				this.getPlugin(), this.selectedSNRRun);
		this.getPlugin().fireEvent(event);
	}

	public void selectImage(final OmegaAnalysisRunContainerInterface image) {
		this.isHandlingEvent = true;
		int index = -1;
		if (this.images != null) {
			index = this.images.indexOf(image);
		}
		if (index == -1) {
			final int count = this.images_cmb.getItemCount() - 1;
			this.images_cmb.setSelectedIndex(count);
		} else {
			this.images_cmb.setSelectedIndex(index);
		}
		this.images_cmb.setSelectedIndex(index);
		this.setAddAndRemoveButtonsEnabled(false);
		this.isHandlingEvent = false;
	}

	public void selectParticleDetectionRun(
			final OmegaParticleDetectionRun analysisRun) {
		this.isHandlingEvent = true;
		this.loadedDataBrowserPanel.selectTreeElement(analysisRun);
		this.updateSelectedParticleDetectionRun(analysisRun);
		this.isHandlingEvent = false;
	}

	public void selectSNRRun(final OmegaSNRRun analysisRun) {
		this.isHandlingEvent = true;
		this.loadedDataBrowserPanel.selectTreeElement(analysisRun);
		this.updateSelectedSNRRun(analysisRun);
		this.isHandlingEvent = false;
	}

	public void setGateway(final OmegaGateway gateway) {

	}

	public void updateStatus(final String s) {
		try {
			this.statusPanel.updateStatus(0, s);
		} catch (final OmegaPluginExceptionStatusPanel ex) {
			OmegaLogFileManager.handlePluginException(this.getPlugin(), ex,
					true);
		}
	}

	public void updateSelectedSNRRun(final OmegaSNRRun snrRun) {
		String c = null, z = null;
		if (this.selectedParticleDetectionRun != null) {
			for (final OmegaParameter param : ((OmegaAnalysisRun) this.selectedParticleDetectionRun)
					.getAlgorithmSpec().getParameters()) {
				if (param.getName().equals(
						OmegaConstantsAlgorithmParameters.PARAM_CHANNEL)) {
					c = param.getStringValue();
				} else if (param.getName().equals(
						OmegaConstantsAlgorithmParameters.PARAM_ZSECTION)) {
					z = param.getStringValue();
				}
			}
		}
		// TODO capire se serve
		this.selectedSNRRun = snrRun;
		this.localResultsPanel.setAnalysisRun(this.selectedSNRRun, true, c, z);
		this.globalFrameResultsPanel.setAnalysisRun(this.selectedSNRRun, false,
				true, c, z);
		this.globalImageResultsPanel.setAnalysisRun(this.selectedSNRRun,
				this.selectedParticleDetectionRun, false, false, c, z);
		if (snrRun != null) {
			// TODO update SNR fields?
			if (!this.isHandlingEvent) {
				this.fireEventSelectionParticleDetectionRun();
				this.fireEventSelectionSNRRun();
			}
			this.runPanel.updateRunFields(snrRun.getAlgorithmSpec()
					.getParameters());
		}
	}

	public void updateSelectedParticleDetectionRun(
			final OmegaParticleDetectionRun particleDetectionRun) {
		this.selectedParticleDetectionRun = particleDetectionRun;
		this.setAddAndRemoveButtonsEnabled(false);
		if (this.isRunningBatch)
			return;
		if (this.selectedParticleDetectionRun != null) {
			if (!this.isHandlingEvent) {
				this.fireEventSelectionParticleDetectionRun();
			}
			this.runPanel
					.updateAnalysisFields(this.selectedParticleDetectionRun);
			// if (this.particlesToProcess.containsKey(this.selectedImage)) {
			// this.removeFromProcess_butt.setEnabled(true);
			// this.runPanel.updateRunFields(this.particlesToProcess
			// .get(this.selectedImage));
			// } else {
			// this.addToProcess_butt.setEnabled(true);
			// this.runPanel.updateRunFieldsDefault();
			// }
			this.runPanel.updateRunFieldsDefault();
		}
	}

	protected void setAddAndRemoveButtonsEnabled(final boolean enabled) {
		this.addToProcess_butt.setEnabled(enabled);
		this.removeFromProcess_butt.setEnabled(enabled);
	}
	
	protected void setAddButtonEnabled(final boolean enabled) {
		this.addToProcess_butt.setEnabled(enabled);
	}
	
	protected void setRemoveButtonEnabled(final boolean enabled) {
		this.removeFromProcess_butt.setEnabled(enabled);
	}

	private void setProcessButtonsEnabled(final boolean enabled) {
		// this.processRealTime_butt.setEnabled(enabled);
		this.processBatch_butt.setEnabled(enabled);
	}

	public boolean checkIfThisAlgorithm(final OmegaSNRRun snrRun) {
		final OmegaAlgorithmPlugin plugin = (OmegaAlgorithmPlugin) this
				.getPlugin();
		return plugin.checkIfThisAlgorithm(snrRun);
	}

	private void updateSNRRunnerMessageStatus(final String msg,
			final boolean ended) {
		try {
			this.statusPanel.updateStatus(1, msg);
		} catch (final OmegaPluginExceptionStatusPanel ex) {
			OmegaLogFileManager.handlePluginException(this.getPlugin(), ex,
					true);
		}
		if (ended) {
			this.updateRunnerEnded();
		}
	}

	@Override
	public void updateMessageStatus(final OmegaMessageEvent evt) {
		final SNRMessageEvent specificEvent = (SNRMessageEvent) evt;
		this.updateSNRRunnerMessageStatus(specificEvent.getMessage(),
				specificEvent.isEnded());
	}

	@Override
	public void fireElementChanged() {
		this.fireEventSelectionImage();
	}
	
	public void deselectNotListener(final GenericPanel panel) {
		if (panel instanceof SNRLoadedDataBrowserPanel) {
			this.queueRunBrowserPanel.deselect();
		} else {
			this.loadedDataBrowserPanel.deselect();
		}
	}
}
