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
package edu.umassmed.omega.omero.commons.runnable;

import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;
import edu.umassmed.omega.commons.OmegaLogFileManager;
import edu.umassmed.omega.commons.constants.OmegaConstants;
import edu.umassmed.omega.commons.constants.OmegaConstantsError;
import edu.umassmed.omega.commons.eventSystem.events.OmegaMessageEvent;
import edu.umassmed.omega.commons.gui.interfaces.OmegaMessageDisplayerPanelInterface;
import edu.umassmed.omega.omero.commons.OmeroGateway;
import edu.umassmed.omega.omero.commons.data.OmeroDatasetWrapper;
import edu.umassmed.omega.omero.commons.data.OmeroImageWrapper;
import edu.umassmed.omega.omero.commons.data.OmeroThumbnailImageInfo;

public class OmeroBrowerPanelImageLoader implements Runnable {

	private final OmegaMessageDisplayerPanelInterface displayerPanel;
	private final OmeroGateway gateway;
	private final ProjectData projectData;
	private final DatasetData datasetData;
	private final List<ImageData> images;
	private volatile ArrayList<OmeroThumbnailImageInfo> imageInfoList;
	private volatile ArrayList<OmeroImageWrapper> imageWrapperList;
	private final int imagesToLoad;
	private volatile int imagesLoaded;
	private final boolean loadThumb;
	
	private Exception ex;
	private volatile boolean isTerminated;
	
	public OmeroBrowerPanelImageLoader(
			final OmegaMessageDisplayerPanelInterface displayerPanel,
			final OmeroGateway gateway, final OmeroDatasetWrapper datasetWrap,
			final boolean loadThumb) {
		this.displayerPanel = displayerPanel;
		this.gateway = gateway;

		this.projectData = datasetWrap.getProject();
		this.datasetData = datasetWrap.getDatasetData();
		this.images = new ArrayList<ImageData>();
		for (final Object obj : this.datasetData.getImages()) {
			this.images.add((ImageData) obj);
		}

		this.loadThumb = loadThumb;

		// this.images = null;
		// try {
		// this.images = new ArrayList<ImageData>(gateway.getImages(
		// this.datasetData, null));
		// } catch (final ServerError e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		this.imageInfoList = new ArrayList<OmeroThumbnailImageInfo>();
		this.imageWrapperList = new ArrayList<OmeroImageWrapper>();

		this.imagesToLoad = this.images.size();
		this.imagesLoaded = 0;
		
		this.ex = null;
		this.isTerminated = false;
	}

	public void terminate() {
		this.isTerminated = true;
	}

	public boolean isTerminated() {
		return this.isTerminated;
	}

	public int getImageToLoad() {
		return this.imagesToLoad;
	}

	public int getImageLoaded() {
		return this.imagesLoaded;
	}

	public ArrayList<OmeroThumbnailImageInfo> getImagesInfo() {
		return this.imageInfoList;
	}

	@Override
	public void run() {
		if (this.images == null)
			return;

		for (int i = 0; i < this.images.size(); i++) {
			if (this.isTerminated) {
				break;
			}
			final int currentlyLoading = 1 + this.imagesLoaded;
			this.updateLoadingStatus(currentlyLoading);
			final ImageData imageData = this.images.get(i);

			final OmeroImageWrapper image = new OmeroImageWrapper(imageData,
					this.projectData, this.datasetData);
			this.imageWrapperList.add(image);

			if (this.loadThumb) {
				final Long pixelID = imageData.getDefaultPixels().getId();

				final List<Long> pixelIDs = new ArrayList<Long>();
				pixelIDs.add(pixelID);

				List<BufferedImage> bufferedImages = null;

				if (this.isTerminated) {
					break;
				}
				try {
					bufferedImages = this.gateway.getThumbnailSet(pixelIDs,
							OmegaConstants.THUMBNAIL_SIZE);
					this.imageInfoList.add(new OmeroThumbnailImageInfo(image,
							bufferedImages.get(0)));
				} catch (final Exception ex) {
					this.ex = ex;
					break;
				}
			}

			this.imagesLoaded++;
		}

		// if (error) {
		// JOptionPane.showMessageDialog(null,
		// OmegaConstantsError.ERROR_LOADING_THE_DS,
		// OmegaConstants.OMEGA_TITLE, JOptionPane.ERROR_MESSAGE);
		// return;
		// }

		this.updateLoadingStatus(this.imagesLoaded);
	}

	private void updateLoadingStatus(final int currentlyLoading) {
		String loadingStatus = null;
		if (this.ex != null) {
			loadingStatus = OmegaConstantsError.ERROR_LOADING_THE_DS;
		} else {
			loadingStatus = currentlyLoading + "/"
					+ OmeroBrowerPanelImageLoader.this.imagesToLoad
					+ " image(s) displayed for " + this.datasetData.getName();
		}
		final String msg = loadingStatus;
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					if (OmeroBrowerPanelImageLoader.this.loadThumb) {
						if (((OmeroBrowerPanelImageLoader.this.imagesLoaded % 10) == 0)
								|| (OmeroBrowerPanelImageLoader.this.imagesLoaded == OmeroBrowerPanelImageLoader.this.imagesToLoad)) {
							OmeroBrowerPanelImageLoader.this.displayerPanel
									.updateMessageStatus(new OmeroThumbnailMessageEvent(
											msg,
											OmeroBrowerPanelImageLoader.this.ex,
											OmeroBrowerPanelImageLoader.this.isTerminated,
											OmeroBrowerPanelImageLoader.this.imageInfoList));

						} else {
							OmeroBrowerPanelImageLoader.this.displayerPanel
									.updateMessageStatus(new OmegaMessageEvent(
											msg));
						}
					} else {
						if (OmeroBrowerPanelImageLoader.this.imagesLoaded >= OmeroBrowerPanelImageLoader.this.imagesToLoad) {
							OmeroBrowerPanelImageLoader.this.displayerPanel
									.updateMessageStatus(new OmeroWrapperMessageEvent(
											msg,
											OmeroBrowerPanelImageLoader.this.ex,
											OmeroBrowerPanelImageLoader.this.isTerminated,
											OmeroBrowerPanelImageLoader.this.imageWrapperList));
						}
					}
				}
			});
		} catch (final InvocationTargetException | InterruptedException ex) {
			OmegaLogFileManager.handleUncaughtException(ex, true);
		}
	}
}
