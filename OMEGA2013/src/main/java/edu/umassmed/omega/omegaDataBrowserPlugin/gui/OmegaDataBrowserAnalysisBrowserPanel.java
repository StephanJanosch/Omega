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
package edu.umassmed.omega.omegaDataBrowserPlugin.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.RootPaneContainer;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import edu.umassmed.omega.commons.data.analysisRunElements.OmegaAnalysisRun;
import edu.umassmed.omega.commons.data.analysisRunElements.OmegaAnalysisRunContainerInterface;
import edu.umassmed.omega.commons.data.coreElements.OmegaElement;
import edu.umassmed.omega.commons.data.coreElements.OmegaNamedElement;
import edu.umassmed.omega.commons.gui.GenericAnalysisInformationPanel;
import edu.umassmed.omega.commons.gui.GenericPanel;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxNode;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxNodeEditor;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxNodeRenderer;
import edu.umassmed.omega.commons.gui.checkboxTree.CheckBoxStatus;
import edu.umassmed.omega.omegaDataBrowserPlugin.OmegaDataBrowserConstants;

public class OmegaDataBrowserAnalysisBrowserPanel extends GenericPanel {

	private static final long serialVersionUID = 5212368402335926305L;

	private final OmegaDataBrowserPluginPanel browserPanel;

	private final Map<String, OmegaElement> nodeMap;
	private final DefaultMutableTreeNode root;
	private final List<OmegaAnalysisRun> loadedAnalysisRun;

	// private final Class<? extends OmegaAnalysisRun> clazz;
	private final String type;

	private JTree dataTree;

	private GenericAnalysisInformationPanel infoPanel;

	public OmegaDataBrowserAnalysisBrowserPanel(final RootPaneContainer parent,
			final OmegaDataBrowserPluginPanel browserPanel, final String type,
			final OmegaAnalysisRunContainerInterface selectedAnalysisContainer,
			final List<OmegaAnalysisRun> loadedAnalysisRun) {
		super(parent);

		this.loadedAnalysisRun = loadedAnalysisRun;
		this.browserPanel = browserPanel;

		this.type = type;

		this.root = new DefaultMutableTreeNode();
		this.root.setUserObject(OmegaDataBrowserConstants.LOADED_DATA);
		this.nodeMap = new HashMap<String, OmegaElement>();

		this.setPreferredSize(new Dimension(250, 380));
		// this.setSize(new Dimension(200, 500));
		this.setLayout(new GridLayout(2, 1));
		this.createAndAddWidgets();
		this.addListeners();
		this.updateTree(selectedAnalysisContainer);
	}

	// public Class<? extends OmegaAnalysisRun> getClazz() {
	// return this.clazz;
	// }
	public String getType() {
		return this.type;
	}

	private void createAndAddWidgets() {
		final Dimension dim = new Dimension(this.getWidth() - 20,
				(this.getHeight() - 10) / 2);

		this.dataTree = new JTree(this.root);
		this.dataTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// this.dataTreeBrowser.setRootVisible(false);
		final CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
		this.dataTree.setCellRenderer(renderer);
		this.dataTree.setCellEditor(new CheckBoxNodeEditor());

		this.dataTree.expandRow(0);
		this.dataTree.setRootVisible(false);
		this.dataTree.setEditable(true);

		final JScrollPane scrollPane = new JScrollPane(this.dataTree);
		// final String name = this.clazz.getSimpleName().replace("Omega", "")
		// .replace("Run", "");
		final String name = this.type;
		// final char[] tokens = name.toCharArray();
		// final StringBuffer buf = new StringBuffer();
		// for (final char c : tokens) {
		// if (Character.isUpperCase(c)) {
		// buf.append(" ");
		// }
		// buf.append(c);
		// }
		// buf.deleteCharAt(0);
		scrollPane.setBorder(new TitledBorder(name));
		scrollPane.setPreferredSize(dim);

		this.add(scrollPane/* , BorderLayout.NORTH */);

		this.infoPanel = new GenericAnalysisInformationPanel(
				this.getParentContainer());
		this.infoPanel.setPreferredSize(dim);

		this.add(this.infoPanel/* , BorderLayout.SOUTH */);
	}

	private void addListeners() {
		this.dataTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(final MouseEvent evt) {
				OmegaDataBrowserAnalysisBrowserPanel.this.handleMouseClick(
						evt.getX(), evt.getY());
			}
		});
		this.dataTree.getModel().addTreeModelListener(new TreeModelListener() {
			@Override
			public void treeNodesChanged(final TreeModelEvent evt) {
				OmegaDataBrowserAnalysisBrowserPanel.this
						.handleTreeNodesChanged(
								(DefaultTreeModel) evt.getSource(),
								evt.getTreePath(), evt.getChildren());
			}

			@Override
			public void treeNodesInserted(final TreeModelEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void treeNodesRemoved(final TreeModelEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void treeStructureChanged(final TreeModelEvent e) {
				// TODO Auto-generated method stub
			}
		});
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent evt) {
				OmegaDataBrowserAnalysisBrowserPanel.this.handleResize();
			}
		});
	}

	private void handleResize() {
		final int width = this.getWidth() - 20;
		final int height = this.getHeight() - 10;

		// this.scrollPane.setPreferredSize(new Dimension(width, height / 2));
		this.infoPanel.resizePanel(width, height / 2);

		this.revalidate();
		this.repaint();
	}
	
	public void selectTreeElement(final OmegaNamedElement element) {
		if (this.dataTree.getRowCount() == 0)
			return;
		final String name = element.getName();
		final String string = "[" + element.getElementID() + "] " + name;
		final TreePath path = this.dataTree.getNextMatch(string, 0,
				Position.Bias.Forward);
		this.dataTree.expandPath(path);
		this.dataTree.setSelectionPath(path);
	}

	private void handleMouseClick(final int x, final int y) {
		final TreePath path = OmegaDataBrowserAnalysisBrowserPanel.this.dataTree
				.getPathForLocation(x, y);
		if (path == null)
			return;
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		final String s = node.toString();
		final OmegaElement element = OmegaDataBrowserAnalysisBrowserPanel.this.nodeMap
				.get(s);
		// final CheckBoxNode c = (CheckBoxNode) node.getUserObject();
		// if (c.getStatus() == CheckBoxStatus.DESELECTED) {
		// OmegaDataBrowserAnalysisBrowserPanel.this.browserPanel
		// .setSelectedSubAnalysisContainer(null);
		// } else
		if (element instanceof OmegaAnalysisRunContainerInterface) {
			OmegaDataBrowserAnalysisBrowserPanel.this.browserPanel
					.setSelectedSubAnalysisContainer((OmegaAnalysisRunContainerInterface) element);
			this.infoPanel.updateContent((OmegaAnalysisRun) element);
		}
	}

	private void handleTreeNodesChanged(final DefaultTreeModel model,
			final TreePath treePath, final Object[] children) {
		DefaultMutableTreeNode node = null;
		CheckBoxNode c = null; // = (CheckBoxNode)node.getUserObject();
		if ((children != null) && (children.length == 1)) {
			node = (DefaultMutableTreeNode) children[0];
			c = (CheckBoxNode) node.getUserObject();
			final DefaultMutableTreeNode n = (DefaultMutableTreeNode) treePath
					.getLastPathComponent();
			model.nodeChanged(n);
		} else {
			node = (DefaultMutableTreeNode) model.getRoot();
			// c = (CheckBoxNode) node.getUserObject();
		}
		// model.nodeChanged(node);

		if (c == null)
			return;

		OmegaDataBrowserAnalysisBrowserPanel.this.updateLoadedAnalysis(node,
				c.getStatus());
	}

	private void updateLoadedAnalysis(final DefaultMutableTreeNode node,
			final CheckBoxStatus status) {
		final String s = node.toString();
		final OmegaElement element = this.nodeMap.get(s);
		final OmegaAnalysisRun analysis = (OmegaAnalysisRun) element;
		if (status == CheckBoxStatus.SELECTED) {
			this.loadedAnalysisRun.add(analysis);
		} else if (status == CheckBoxStatus.DESELECTED) {
			this.loadedAnalysisRun.remove(analysis);
			this.browserPanel.deselectAllChildren(analysis);
			OmegaDataBrowserAnalysisBrowserPanel.this.browserPanel
					.fireEventAnalysisRunDeselection();
		}
		this.browserPanel.fireDataChangedEvent();
	}
	
	public void deselectAll() {
		// this.deselectNode(this.root);
		for (int i = 0; i < this.root.getChildCount(); i++) {
			this.deselectNode((DefaultMutableTreeNode) this.root.getChildAt(i));
		}
	}
	
	private void deselectNode(final DefaultMutableTreeNode node) {
		final CheckBoxNode c = (CheckBoxNode) node.getUserObject();
		c.setStatus(CheckBoxStatus.DESELECTED);
		for (int i = 0; i < node.getChildCount(); i++) {
			this.deselectNode((DefaultMutableTreeNode) node.getChildAt(i));
		}
	}

	public void updateTree(
			final OmegaAnalysisRunContainerInterface selectedAnalysisContainer) {
		this.dataTree.setRootVisible(true);

		String s = null;
		CheckBoxStatus status = null;
		this.root.removeAllChildren();
		((DefaultTreeModel) this.dataTree.getModel()).reload();
		this.nodeMap.clear();
		if (selectedAnalysisContainer != null) {
			for (final OmegaAnalysisRun analysisRun : selectedAnalysisContainer
					.getAnalysisRuns()) {
				if (this.type != analysisRun.getDynamicDisplayName()) {
					continue;
				}
				// if (!this.clazz.isInstance(analysisRun)) {
				// continue;
				// }
				final DefaultMutableTreeNode node = new DefaultMutableTreeNode();
				s = "[" + analysisRun.getElementID() + "] "
						+ analysisRun.getName();
				this.nodeMap.put(s, analysisRun);
				status = this.loadedAnalysisRun.contains(analysisRun)
						? CheckBoxStatus.SELECTED : CheckBoxStatus.DESELECTED;
				node.setUserObject(new CheckBoxNode(s, status));
				this.root.add(node);
			}
		}

		// this.setPreferredSize(this.dataTree.getSize());
		this.dataTree.expandRow(0);
		this.dataTree.setRootVisible(false);
		this.dataTree.repaint();
		this.infoPanel.updateContent(null);
	}

	@Override
	public void updateParentContainer(final RootPaneContainer parent) {
		super.updateParentContainer(parent);
		this.infoPanel.updateParentContainer(parent);
	}
}
