/*
 * Copyright (c) 2015 John May <jwmay@users.sf.net>
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version. All we ask is that proper credit is given
 * for our work, which includes - but is not limited to - adding the above
 * copyright notice to the beginning of your source code files, and to any
 * copyright notice that you may distribute with programs based on this work.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 U
 */

package org.openscience.cdk.depict;

import org.junit.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.sgroup.Sgroup;
import org.openscience.cdk.sgroup.SgroupType;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AbbreviationsTest {

    private static SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());

    static IAtomContainer smi(String smi) throws Exception {
        return smipar.parseSmiles(smi);
    }

    @Test
    public void potassiumCarbonate() throws Exception {
        IAtomContainer mol = smi("[K+].[O-]C(=O)[O-].[K+]");
        Abbreviations factory = new Abbreviations();
        factory.add("[K+].[O-]C(=O)[O-].[K+] K2CO3");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(1));
        assertThat(sgroups.get(0).getSubscript(), is("K2CO3"));
        assertThat(sgroups.get(0).getAtoms().size(), is(6));
    }

    @Test
    public void phenyl() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("CCCCCCC(c1ccccc1)(c1ccccc1)c1ccccc1");
        factory.add("*c1ccccc1 Ph");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(3));
        assertThat(sgroups.get(0).getSubscript(), is("Ph"));
        assertThat(sgroups.get(0).getAtoms().size(), is(6));
        assertThat(sgroups.get(0).getBonds().size(), is(1));
        assertThat(sgroups.get(1).getSubscript(), is("Ph"));
        assertThat(sgroups.get(1).getAtoms().size(), is(6));
        assertThat(sgroups.get(1).getBonds().size(), is(1));
        assertThat(sgroups.get(2).getSubscript(), is("Ph"));
        assertThat(sgroups.get(2).getAtoms().size(), is(6));
        assertThat(sgroups.get(2).getBonds().size(), is(1));
    }

    @Test
    public void phenylShouldNotMatchBenzene() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("c1ccccc1");
        factory.add("*c1ccccc1 Ph");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(0));
    }

    @Test
    public void avoidOverZealousAbbreviations() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("Clc1ccccc1");
        factory.add("*c1ccccc1 Ph");
        assertThat(factory.apply(mol), is(0));
    }

    @Test
    public void phenylShouldNotMatchC4H6() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("Oc1ccc(O)cc1");
        factory.add("*c1ccccc1 Ph");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(0));
    }

    @Test
    public void phenylShouldAbbreviateExplicitHydrogens() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("CCCCc1ccc([H])cc1");
        factory.add("*c1ccccc1 Ph");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(1));
        assertThat(sgroups.get(0).getSubscript(), is("Ph"));
        assertThat(sgroups.get(0).getAtoms().size(), is(7));
        assertThat(sgroups.get(0).getBonds().size(), is(1));
    }

    // some SMARTS foo here :-)
    @Test
    public void phenylShouldMatchKekuleForm() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("CCCCC1=CC=CC=C1");
        factory.add("*c1ccccc1 Ph");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(1));
        assertThat(sgroups.get(0).getSubscript(), is("Ph"));
        assertThat(sgroups.get(0).getAtoms().size(), is(6));
        assertThat(sgroups.get(0).getBonds().size(), is(1));
    }

    // SMARTS foo not that good
    @Test
    public void nitroGroups() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("O=N(=O)CCCC[N+]([O-])=O");
        factory.add("*N(=O)(=O) NO2");
        factory.add("*[N+]([O-])(=O) NO2");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(2));
        assertThat(sgroups.get(0).getSubscript(), is("NO2"));
        assertThat(sgroups.get(0).getAtoms().size(), is(3));
        assertThat(sgroups.get(0).getBonds().size(), is(1));
        assertThat(sgroups.get(1).getSubscript(), is("NO2"));
        assertThat(sgroups.get(1).getAtoms().size(), is(3));
        assertThat(sgroups.get(1).getBonds().size(), is(1));
    }

    @Test
    public void abbreviationsHavePriority() throws Exception {
        Abbreviations factory = new Abbreviations();
        IAtomContainer mol = smi("c1ccccc1CCC");
        factory.add("*CCC Pr");
        factory.add("*CC Et");
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(1));
        assertThat(sgroups.get(0).getSubscript(), is("Pr"));
    }

    @Test
    public void dontOverwriteExistingSgroups() throws Exception {
        Abbreviations factory = new Abbreviations();
        factory.add("*CCC Bu");
        IAtomContainer mol = smi("c1ccccc1CCC");
        Sgroup sgroup = new Sgroup();
        sgroup.addAtom(mol.getAtom(6));
        sgroup.addAtom(mol.getAtom(7));
        sgroup.addAtom(mol.getAtom(8));
        sgroup.setType(SgroupType.CtabAbbreviation);
        sgroup.setSubscript("n-Bu");
        mol.setProperty(CDKConstants.CTAB_SGROUPS, Collections.singletonList(sgroup));
        List<Sgroup> sgroups = factory.generate(mol);
        assertThat(sgroups.size(), is(0));
    }

    @Test
    public void loadFromFile() throws Exception {
        Abbreviations factory = new Abbreviations();
        assertThat(factory.loadFromFile("obabel_superatoms.smi"), is(27));
        assertThat(factory.loadFromFile("/org/openscience/cdk/depict/obabel_superatoms.smi"), is(27));
    }

}