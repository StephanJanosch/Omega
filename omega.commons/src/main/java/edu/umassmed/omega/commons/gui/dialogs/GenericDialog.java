package edu.umassmed.omega.commons.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.RootPaneContainer;
import javax.swing.WindowConstants;

public abstract class GenericDialog extends JDialog {
	private static final long serialVersionUID = -3182818498954948942L;

	private RootPaneContainer parentContainer;

	public GenericDialog(final RootPaneContainer parentContainer,
	        final String title, final boolean modal) {
		this.parentContainer = parentContainer;

		this.setTitle(title);
		this.setModal(modal);

		this.setLayout(new BorderLayout());

		this.createAndAddWidgets();

		this.addListeners();

		this.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.pack();
	}

	protected abstract void createAndAddWidgets();

	protected abstract void addListeners();

	public void setPosition() {
		Point parentLocOnScren = null;
		Dimension parentSize = null;
		try {
			if (this.parentContainer instanceof JInternalFrame) {
				final JInternalFrame intFrame = (JInternalFrame) this.parentContainer;
				parentLocOnScren = intFrame.getLocationOnScreen();
				parentSize = intFrame.getSize();
			} else {
				final JFrame frame = (JFrame) this.parentContainer;
				parentLocOnScren = frame.getLocationOnScreen();
				parentSize = frame.getSize();
			}
		} catch (final IllegalComponentStateException ex) {
			parentLocOnScren = new Point(0, 0);
			parentSize = new Dimension(500, 500);
		}

		final int x = parentLocOnScren.x;
		final int y = parentLocOnScren.y;
		final int xOffset = (parentSize.width / 2) - (this.getSize().width / 2);
		final int yOffset = (parentSize.height / 2)
		        - (this.getSize().height / 2);
		int newX = x + xOffset;
		int newY = y + yOffset;
		if (newX < 0) {
			newX = 0;
		}
		if (newY < 0) {
			newY = 0;
		}
		final Point dialogPos = new Point(newX, newY);
		this.setLocation(dialogPos);
	}

	@Override
	public void setVisible(final boolean isVisible) {
		if (isVisible) {
			this.setPosition();
		}
		super.setVisible(isVisible);
	}

	public void updateParentContainer(final RootPaneContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	protected RootPaneContainer getParentContainer() {
		return this.parentContainer;
	}
}
