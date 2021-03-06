/*******************************************************************************
 * Copyright (C) 2014 University of Massachusetts Medical School
 * Alessandro Rigano (Program in Molecular Medicine)
 * Caterina Strambio De Castillia (Program in Molecular Medicine)
 *
 * Created by the Open Microscopy Environment inteGrated Analysis (OMEGA) team:
 * Alex Rigano, Caterina Strambio De Castillia, Jasmine Clark, Vanni Galli,
 * Raffaello Giulietti, Loris Grossi, Eric Hunter, Tiziano Leidi, Jeremy Luban,
 * Ivo Sbalzarini and Mario Valle.
 *
 * Key contacts:
 * Caterina Strambio De Castillia: caterina.strambio@umassmed.edu
 * Alex Rigano: alex.rigano@umassmed.edu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package edu.umassmed.omega.commons.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.RootPaneContainer;

import edu.umassmed.omega.commons.constants.OmegaGUIConstants;
import edu.umassmed.omega.commons.gui.interfaces.GenericPluginPanelInterface;
import edu.umassmed.omega.commons.plugins.OmegaPlugin;

public abstract class GenericPluginPanel extends GenericPanel implements
        GenericPluginPanelInterface {

	private static final long serialVersionUID = -8723953179147120178L;

	private final OmegaPlugin plugin;

	private final int index;
	private boolean isAttached;

	private JMenuBar menu;
	private JMenu fileMenu, windowMenu, help;
	private JMenuItem closeMItem, aboutMItem, toggleWindowMItem;

	public GenericPluginPanel(final RootPaneContainer parent) {
		this(parent, null, -1);
	}

	public GenericPluginPanel(final RootPaneContainer parent,
	        final OmegaPlugin plugin, final int index) {
		super(parent);
		this.plugin = plugin;
		this.index = index;
		this.createMenu();
		this.addListeners();
	}

	public int getIndex() {
		return this.index;
	}

	private void createMenu() {
		this.menu = new JMenuBar();

		this.fileMenu = new JMenu(OmegaGUIConstants.MENU_FILE);
		this.aboutMItem = new JMenuItem(OmegaGUIConstants.MENU_FILE_ABOUT);
		this.fileMenu.add(this.aboutMItem);
		this.fileMenu.add(new JSeparator());
		this.closeMItem = new JMenuItem(OmegaGUIConstants.MENU_FILE_CLOSE);
		this.fileMenu.add(this.closeMItem);
		this.menu.add(this.fileMenu);

		if (this.plugin == null)
			return;

		this.windowMenu = new JMenu(OmegaGUIConstants.MENU_VIEW);
		this.toggleWindowMItem = new JMenuItem(
		        OmegaGUIConstants.MENU_WORKSPACE_DOCK_SINGLE);
		this.windowMenu.add(this.toggleWindowMItem);
		this.menu.add(this.windowMenu);
	}

	@Override
	public JMenuBar getMenu() {
		return this.menu;
	}

	public void setIsAttached(final boolean isAttached) {
		if (this.plugin == null)
			return;
		this.isAttached = isAttached;
		if (isAttached) {
			this.toggleWindowMItem
			        .setText(OmegaGUIConstants.MENU_WORKSPACE_UNDOCK_SINGLE);
		} else {
			this.toggleWindowMItem
			        .setText(OmegaGUIConstants.MENU_WORKSPACE_DOCK_SINGLE);
		}
	}

	public boolean isAttached() {
		if (this.plugin == null)
			return true;
		return this.isAttached;
	}

	private void addListeners() {
		this.closeMItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				GenericPluginPanel.this.onCloseOperationFromMenu();
			}
		});
		if (this.plugin == null)
			return;
		this.toggleWindowMItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				final RootPaneContainer parent = GenericPluginPanel.this
				        .getParentContainer();
				final long oldValue = -1;
				final long newValue = GenericPluginPanel.this.index;
				if (parent instanceof JFrame) {
					final JFrame frame = (JFrame) parent;
					frame.firePropertyChange(
							OmegaGUIConstants.EVENT_PROPERTY_TOGGLEWINDOW,
					        oldValue, newValue);
				} else if (parent instanceof JInternalFrame) {
					final JInternalFrame intFrame = (JInternalFrame) parent;
					intFrame.firePropertyChange(
					        OmegaGUIConstants.EVENT_PROPERTY_TOGGLEWINDOW,
							oldValue, newValue);
				}
			}
		});
	}

	public void onCloseOperationFromMenu() {
		final RootPaneContainer parent = this.getParentContainer();
		if (parent instanceof JFrame) {
			final JFrame frame = (JFrame) parent;
			final WindowEvent evt = new WindowEvent(frame,
			        WindowEvent.WINDOW_CLOSING);
			frame.dispatchEvent(evt);
		} else if (parent instanceof JInternalFrame) {
			final JInternalFrame intFrame = (JInternalFrame) parent;
			intFrame.doDefaultCloseAction();
		}
	}

	public OmegaPlugin getPlugin() {
		return this.plugin;
	}
}
