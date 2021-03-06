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
package edu.umassmed.omega.commons.data.coreElements;

import java.util.ArrayList;
import java.util.List;

public class OmegaExperimenter extends OmegaPerson implements OmeroElement {

	private static String DISPLAY_NAME = "Experimenter";
	
	private final List<OmegaExperimenterGroup> groups;
	
	private Long omeroId;
	
	public OmegaExperimenter(final String firstName, final String lastName) {
		super(firstName, lastName);
		this.omeroId = 1L;
		this.groups = new ArrayList<OmegaExperimenterGroup>();
	}
	
	public OmegaExperimenter(final String firstName, final String lastName,
			final List<OmegaExperimenterGroup> groups) {
		super(firstName, lastName);
		this.omeroId = 1L;
		this.groups = groups;
	}
	
	public List<OmegaExperimenterGroup> getGroups() {
		return this.groups;
	}
	
	public void addGroup(final OmegaExperimenterGroup group) {
		this.groups.add(group);
	}
	
	public boolean containsGroup(final OmegaExperimenterGroup group) {
		return this.groups.contains(group);
	}
	
	// public boolean containsGroup(final long id, final boolean gatewayId) {
	// for (final OmegaExperimenterGroup group : this.groups) {
	// if (!gatewayId) {
	// if (group.getElementID() == id)
	// return true;
	// } else {
	// if (group.getOmeroId() == id)
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// public OmegaExperimenterGroup getGroup(final long id,
	// final boolean gatewayId) {
	// for (final OmegaExperimenterGroup group : this.groups) {
	// if (!gatewayId) {
	// if (group.getElementID() == id)
	// return group;
	// } else {
	// if (group.getOmeroId() == id)
	// return group;
	// }
	// }
	// return null;
	// }
	
	@Override
	public Long getOmeroId() {
		return this.omeroId;
	}
	
	@Override
	public void setOmeroId(final Long omeroId) {
		this.omeroId = omeroId;
	}

	public static String getStaticDisplayName() {
		return OmegaExperimenter.DISPLAY_NAME;
	}
	
	@Override
	public String getDynamicDisplayName() {
		return OmegaExperimenter.getStaticDisplayName();
	}
}
