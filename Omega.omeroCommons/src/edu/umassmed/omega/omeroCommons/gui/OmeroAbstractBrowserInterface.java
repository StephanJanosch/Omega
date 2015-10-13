package edu.umassmed.omega.omeroCommons.gui;

import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxStatus;
import edu.umassmed.omega.commons.gui.interfaces.OmegaMessageDisplayerPanelInterface;
import edu.umassmed.omega.omeroCommons.data.OmeroDatasetWrapper;

public interface OmeroAbstractBrowserInterface extends
        OmegaMessageDisplayerPanelInterface {

	public abstract void updateDatasetSelection(final int size);

	public abstract void browseDataset(final OmeroDatasetWrapper datasetWrapper);

	public abstract void updateImagesSelection(CheckBoxStatus status);
}
