package edu.umassmed.omega.trackingMeasuresDiffusivityPlugin;

import java.util.Date;
import java.util.GregorianCalendar;

import edu.umassmed.omega.commons.constants.OmegaConstantsMathSymbols;
import edu.umassmed.omega.commons.constants.OmegaGUIConstants;

public class TMDConstants {

	public static final String SELECT_IMAGE = OmegaGUIConstants.SELECT_IMAGE;
	public static final String SELECT_TRACKS_SPOT = OmegaGUIConstants.SELECT_TRACKS_SPOT;
	public static final String SELECT_TRACKS_LINKING = OmegaGUIConstants.SELECT_TRACKS_LINKING;
	public static final String SELECT_TRACKS_ADJ = OmegaGUIConstants.SELECT_TRACKS_ADJ;
	public static final String SELECT_TRACKS_SEGM = OmegaGUIConstants.SELECT_TRACKS_SEGM;
	public static final String SELECT_TRACK_MEASURES = OmegaGUIConstants.SELECT_TRACK_MEASURES;

	// DIFFUSIVITY
	// GLOBAL
	public static final String GRAPH_NAME_UNCERT_D = "D2 Uncertainty";
	public static final String GRAPH_LAB_Y_UNCERT_D = "D2 Uncertainty [a.u.]";
	public static final String GRAPH_NAME_UNCERT_SMSS = "Slope MSS Uncertainty";
	public static final String GRAPH_LAB_Y_UNCERT_SMSS = "Slope MSS Uncertainty [a.u.]";
	public static final String GRAPH_NAME_DIFF = "Diffusion Coefficient";
	public static final String GRAPH_LAB_Y_DIFF = "D2 ["
			+ OmegaConstantsMathSymbols.MU + "m^2/s]";
	public static final String GRAPH_NAME_MSD = "Slope of log-log MSD Plot";
	public static final String GRAPH_LAB_Y_MSD = "Slope of log(MSD) / log(deltaT) [a.u.]";
	public static final String GRAPH_NAME_MSS = "Slope MSS";
	public static final String GRAPH_LAB_Y_MSS = "Gamma [a.u.]";

	// TODO TO FIX
	public static final String PLUGIN_NAME = "OMEGA Trajectory Editor";
	public static final String PLUGIN_SNAME = "OMEGA TE";
	public static final String PLUGIN_DESC = "This is the default Trajectory Editor plugin provided by OMEGA. It utilizes the Trajectory Browser graphical user interface to facilitate the visualization and selection of individual trajectories in the context of the image of origin, their inspection and the editing of individual links when necessary.";
	public static final String PLUGIN_ALGO_DESC = "This is a simple manual trajectory re-linking algorithm designed to edit individual particle-particle links in cases in which linking errors are introduced by the Single Particle Tracking algorithm. Such errors are common and typically result from particle blinking, temporary focal plane shifts or the crossing of trajectories with temporary particle fusion.";
	public static final Date PLUGIN_PUBL = new GregorianCalendar(2014, 12, 1)
	        .getTime();

}