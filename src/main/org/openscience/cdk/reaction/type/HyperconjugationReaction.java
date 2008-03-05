/*
 *  $RCSfile$
 *  $Author: egonw $
 *  $Date: 2006-03-29 10:27:08 +0200 (Wed, 29 Mar 2006) $
 *  $Revision: 5855 $
 *
 *  Copyright (C) 2008 Miguel Rojas <miguelrojasch@users.sf.net>
 *
 *  Contact: cdk-devel@lists.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.reaction.type;


import java.util.Iterator;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMapping;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.interfaces.IReaction;
import org.openscience.cdk.interfaces.IReactionSet;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.reaction.IReactionProcess;
import org.openscience.cdk.reaction.ReactionEngine;
import org.openscience.cdk.reaction.ReactionSpecification;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.manipulator.BondManipulator;

/**
 * <p>HyperconjugationReaction is the stabilizing interaction that results 
 * from the interaction of the electrons in a s-bond (for our case only C-H)
 * with an adjacent empty (or partially filled) p-orbital.</p>
 * <p>Based on the valence bond model of bonding, hyperconjugation can be described as 
 * "double bond - no bond resonance"</p>
 * <p>This reaction could be represented like</p>
 * <pre>[C+]-C => C=C + [H+] </pre>
 * 
 * <pre>
 *  IMoleculeSet setOfReactants = DefaultChemObjectBuilder.getInstance().newMoleculeSet();
 *  setOfReactants.addMolecule(new Molecule());
 *  IReactionProcess type = new HyperconjugationReaction();
 *  Object[] params = {Boolean.FALSE};
    type.setParameters(params);
 *  IReactionSet setOfReactions = type.initiate(setOfReactants, null);
 *  </pre>
 * 
 * <p>We have the possibility to localize the reactive center. Good method if you
 * want to localize the reaction in a fixed point</p>
 * <pre>atoms[0].setFlag(CDKConstants.REACTIVE_CENTER,true);</pre>
 * <p>Moreover you must put the parameter Boolean.TRUE</p>
 * <p>If the reactive center is not localized then the reaction process will
 * try to find automatically the possible reactive center.</p>
 * 
 * 
 * @author         Miguel Rojas
 * 
 * @cdk.created    2006-07-04
 * @cdk.module     reaction
 * @cdk.svnrev  $Revision: 9162 $
 * @cdk.set        reaction-types
 * 
 **/
public class HyperconjugationReaction extends ReactionEngine implements IReactionProcess{
	private LoggingTool logger;
	private CDKAtomTypeMatcher atMatcher;

	/**
	 * Constructor of the HyperconjugationReaction object
	 *
	 */
	public HyperconjugationReaction(){
		logger = new LoggingTool(this);
		atMatcher = CDKAtomTypeMatcher.getInstance(
				NoNotificationChemObjectBuilder.getInstance(),
				CDKAtomTypeMatcher.REQUIRE_EXPLICIT_HYDROGENS
			);
	}
	/**
	 *  Gets the specification attribute of the HyperconjugationReaction object
	 *
	 *@return    The specification value
	 */
	public ReactionSpecification getSpecification() {
		return new ReactionSpecification(
				"http://almost.cubic.uni-koeln.de/jrg/Members/mrc/reactionDict/reactionDict#Hyperconjugation",
				this.getClass().getName(),
				"$Id: HyperconjugationReaction.java,v 1.6 2006/04/01 08:26:47 mrc Exp $",
				"The Chemistry Development Kit");
	}
	/**
	 *  Initiate process.
	 *  It is needed to call the addExplicitHydrogensToSatisfyValency
	 *  from the class tools.HydrogenAdder.
	 *
	 *@param  reactants         reactants of the reaction.
	 *@param  agents            agents of the reaction (Must be in this case null).
	 *
	 *@exception  CDKException  Description of the Exception
	 */
	public IReactionSet initiate(IMoleculeSet reactants, IMoleculeSet agents) throws CDKException{

		logger.debug("initiate reaction: HyperconjugationReaction");
		
		if (reactants.getMoleculeCount() != 1) {
			throw new CDKException("HyperconjugationReaction only expects one reactant");
		}
		if (agents != null) {
			throw new CDKException("HyperconjugationReaction don't expects agents");
		}
		
		IReactionSet setOfReactions = reactants.getBuilder().newReactionSet();
		IMolecule reactant = reactants.getMolecule(0);
		
		/* if the parameter hasActiveCenter is not fixed yet, set the active centers*/
		if(!(Boolean)paramsMap.get("hasActiveCenter")){
			setActiveCenters(reactant);
		}
		Iterator<IAtom> atoms = reactants.getMolecule(0).atoms();
        while (atoms.hasNext()) {
			IAtom atomi = atoms.next();
			if(atomi.getFlag(CDKConstants.REACTIVE_CENTER) && atomi.getFormalCharge() == 1){
				
				Iterator<IBond> bondis = reactant.getConnectedBondsList(atomi).iterator();
				
				while (bondis.hasNext()) {
		            IBond bondi = bondis.next();
		            
					if(bondi.getFlag(CDKConstants.REACTIVE_CENTER)&& bondi.getOrder() == IBond.Order.SINGLE){
						
						IAtom atomj = bondi.getConnectedAtom(atomi);
						if(atomj.getFlag(CDKConstants.REACTIVE_CENTER) && atomj.getFormalCharge() == 0){

							Iterator<IBond> bondjs = reactant.getConnectedBondsList(atomj).iterator();
							while (bondjs.hasNext()) {
					            IBond bondj = bondjs.next();
					            
					            if(bondj.equals(bondi))
					            	continue;
	
					            if(bondj.getFlag(CDKConstants.REACTIVE_CENTER) && bondj.getOrder() == IBond.Order.SINGLE){
									
					            	IAtom atomk = bondj.getConnectedAtom(atomj);
									if(atomk.getFlag(CDKConstants.REACTIVE_CENTER) && atomk.getSymbol().equals("H") 
											&& atomk.getFormalCharge() == 0 ){
										
										IReaction reaction = DefaultChemObjectBuilder.getInstance().newReaction();
										reaction.addReactant(reactant);
										
										/* positions atoms and bonds */
										int atomiP = reactant.getAtomNumber(atomi);
										int atomjP = reactant.getAtomNumber(atomj);
										int atomkP = reactant.getAtomNumber(atomk);
										int bondiP = reactant.getBondNumber(bondi);
										int bondjP = reactant.getBondNumber(bondj);
									
										/* action */
										IAtomContainer reactantCloned;
										try {
											reactantCloned = (IMolecule)reactant.clone();
										} catch (CloneNotSupportedException e) {
											throw new CDKException("Could not clone IMolecule!", e);
										}

										BondManipulator.increaseBondOrder(reactantCloned.getBond(bondiP));

										int charge = reactantCloned.getAtom(atomiP).getFormalCharge();
										reactantCloned.getAtom(atomiP).setFormalCharge(charge-1);

										charge = reactantCloned.getAtom(atomkP).getFormalCharge();
										reactantCloned.getAtom(atomkP).setFormalCharge(charge+1);
										
										reactantCloned.removeBond(reactantCloned.getBond(bondjP));

//										// check if resulting atom type is reasonable
//										IAtomType type = atMatcher.findMatchingAtomType(reactantCloned, reactantCloned.getAtom(atomiP));
//										if (type == null)continue;
//										type = atMatcher.findMatchingAtomType(reactantCloned, reactantCloned.getAtom(atomkP));
//										if (type == null)continue;
										
										/* mapping */
										IMapping mapping = atomi.getBuilder().newMapping(atomi, reactantCloned.getAtom(atomiP));
								        reaction.addMapping(mapping);
								        mapping = atomi.getBuilder().newMapping(atomj, reactantCloned.getAtom(atomjP));
								        reaction.addMapping(mapping);
								        mapping = atomi.getBuilder().newMapping(atomk, reactantCloned.getAtom(atomkP));
								        reaction.addMapping(mapping);
								        mapping = atomi.getBuilder().newMapping(bondi, reactantCloned.getBond(bondiP));
								        reaction.addMapping(mapping);
							        
										IMoleculeSet moleculeSet = ConnectivityChecker.partitionIntoMolecules(reactantCloned);
										for(int z = 0; z < moleculeSet.getAtomContainerCount() ; z++){
											reaction.addProduct(moleculeSet.getMolecule(z));
										}
										
										setOfReactions.addReaction(reaction);
									}
								}
							}
						}
					}
				}
			}
		}

		
		return setOfReactions;	
		
		
	}
	/**
	 * set the active center for this molecule. 
	 * The active center will be those which correspond with [A+]-B([H]). 
	 * <pre>
	 * A: Atom with charge
	 * -: Singlebond
	 * B: Atom
	 *  </pre>
	 * 
	 * @param reactant The molecule to set the activity
	 * @throws CDKException 
	 */
	private void setActiveCenters(IMolecule reactant) throws CDKException {
		Iterator<IAtom> atoms = reactant.atoms();
        while (atoms.hasNext()) {
			IAtom atomi = atoms.next();
			if(atomi.getFormalCharge() == 1){
				
				Iterator<IBond> bondis = reactant.getConnectedBondsList(atomi).iterator();
				
				while (bondis.hasNext()) {
		            IBond bondi = bondis.next();
		            
					if(bondi.getOrder() == IBond.Order.SINGLE){
						
						IAtom atomj = bondi.getConnectedAtom(atomi);
						if(atomj.getFormalCharge() == 0){

							Iterator<IBond> bondjs = reactant.getConnectedBondsList(atomj).iterator();
							while (bondjs.hasNext()) {
					            IBond bondj = bondjs.next();
					            
					            if(bondj.equals(bondi))
					            	continue;
	
					            if(bondj.getOrder() == IBond.Order.SINGLE){
									
					            	IAtom atomk = bondj.getConnectedAtom(atomj);
									if(atomk.getSymbol().equals("H")){ 
											atomi.setFlag(CDKConstants.REACTIVE_CENTER,true);
											atomj.setFlag(CDKConstants.REACTIVE_CENTER,true);
											atomk.setFlag(CDKConstants.REACTIVE_CENTER,true);
											bondi.setFlag(CDKConstants.REACTIVE_CENTER,true);
											bondj.setFlag(CDKConstants.REACTIVE_CENTER,true);
									}
					            }
							}
						}

					}
				}
			}
		}
	}
}
