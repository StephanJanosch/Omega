package main.java.edu.umassmed.omega.omero.ij.testConvert;

import ij.ImagePlus;

import java.io.IOException;

import main.java.edu.umassmed.omega.commons.data.imageDBConnectionElements.OmegaGateway;
import main.java.edu.umassmed.omega.omero.commons.OmeroGateway;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.omero.OMEROService;
import omero.ServerError;

import org.scijava.convert.ConvertService;
import org.scijava.service.Service;

public class OmeroImageJTestConvert {

	private final ImageJ imagej;
	private final OMEROService ome;
	private final ConvertService conv;

	public OmeroImageJTestConvert() {
		// Thread.currentThread().setContextClassLoader(IJ.getClassLoader());
		this.imagej = new ImageJ();
		final Service serv = this.imagej.get(OMEROService.class);
		this.ome = (OMEROService) serv;
		this.conv = this.imagej.convert();
	}

	public ImagePlus convert(final Long imageID, final OmegaGateway gateway) {
		if (!(gateway instanceof OmeroGateway))
			return null;
		Dataset dataset = null;
		try {
			dataset = this.ome.downloadImage(
			        ((OmeroGateway) gateway).getClient(), imageID);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (final ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (dataset == null)
			return null;
		return this.conv.convert(dataset, ImagePlus.class);
	}
}