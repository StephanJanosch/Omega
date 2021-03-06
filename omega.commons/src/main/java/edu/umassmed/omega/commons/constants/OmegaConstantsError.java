package edu.umassmed.omega.commons.constants;

public class OmegaConstantsError {

	// ***ERRORS***
	public final static String ERROR_PORT_IS_NUMBER = "Port must be a number!";
	public final static String ERROR_CANNOT_CONNECT_TO_OMERO = "cannot connect to server";
	public final static String ERROR_LOADING_THE_DS = "Unable to load the Dataset!";
	public final static String ERROR_UNABLE_TO_DISPLAY_IMAGE = "Unable to display the image!";
	public final static String ERROR_SAVE_IMAGE = "Unable to save the image!";
	public final static String ERROR_C_Z_MUST_BE_NUMBERS = "C and Z must be numbers!";
	public final static String ERROR_SPT_MAX_VALUE = "This value must be a number!";
	public final static String ERROR_INIT_SPT_RUN = "Error during the initialization of the SPT algorithm!";
	public final static String ERROR_DURING_SPT_RUN = "Error during the run of the SPT algorithm!";
	public final static String ERROR_SPT_SAVE_RESULTS = "Error saving the SPT results!";
	public final static String ERROR_NOTRAJECTORIES = "Unable to load any trajectory!";
	public final static String ERROR_NO_SPT_INFORMATION = "Unable to load the image's information coming from the SPT module!";
	public final static String ERROR_NOPATTERNS = "Unable to load any pattern!";
	public final static String ERROR_LOADING_SEGMENTATION = "Error during the segmentation loading!";
	public final static String ERROR_DRAWING = "Unable to draw the Trajectory!";
	public final static String ERROR_SAVE_LABELS = "Unable to save the trajectories labels!";
	public final static String ERROR_SAVE_CSV = "Unable to save the CSV file!";
	public final static String ERROR_TS_NOT_ENOUGH_POINTS = "Each trajectory must have at least 100 points to be processed!";
	public final static String ERROR_NODLL = "No Omega DLL found or DLL error: ";
	public final static String ERROR_OPENBIS_CONNECTION_FAIL = "Unable to connect to openBIS, please check your settings!";
	public final static String ERROR_OPENBIS_UPLOAD = "Unable to upload the dataset to openBIS!";
	public final static String ERROR_OPENBIS_DOWNLOAD = "Unable to download the dataset from openBIS!";
	public final static String ERROR_OPENBIS_LISTDATASETS = "Unable to load the dataset's list from openBIS!";
	public final static String ERROR_STATISTICAL_CALCULATION = "Something went wrong during the statistical calculation.\nStats not available.";
	public final static String ERROR_INTERPOLATION_CALCULATION = "Something went wrong during the bilinear interpolation.";
	public final static String ERROR_INTERPOLATION_CALCULATION_SNR = "The SNR is out of range. Impossible to interpolate.";
	public final static String ERROR_INTERPOLATION_CALCULATION_L = "The length is out of range. Impossible to interpolate.";

}
