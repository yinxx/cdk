/*
 * Copyright (c) 2016 John May <jwmay@users.sf.net>
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

package org.openscience.cdk.smiles;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class CxSmilesGeneratorTest {

    @Test
    public void emptyCXSMILES() {
        CxSmilesState state = new CxSmilesState();
        assertThat(CxSmilesGenerator.generate(state, SmiFlavor.CxSmiles, new int[0], new int[0]),
                   is(""));
    }

    @Test
    public void multicenter() {
        CxSmilesState state = new CxSmilesState();
        state.positionVar = new HashMap<>();
        state.positionVar.put(0, Arrays.asList(4, 5, 6, 7));
        state.positionVar.put(2, Arrays.asList(4, 6, 5, 7));
        assertThat(CxSmilesGenerator.generate(state, SmiFlavor.CxMulticenter, new int[0], new int[]{7, 6, 5, 4, 3, 2, 1, 0}),
                   is(" |m:5:0.1.2.3,7:0.1.2.3|"));
    }

    @Test
    public void coords2d() {
        CxSmilesState state = new CxSmilesState();
        state.atomCoords = Arrays.asList(new double[]{0, 1.5, 0},
                                         new double[]{0, 3, 0},
                                         new double[]{1.5, 1.5, 0});
        assertThat(CxSmilesGenerator.generate(state, SmiFlavor.CxCoordinates, new int[0], new int[]{1, 2, 0}),
                   is(" |(1.5,1.5,;,1.5,;,3,)|"));
    }

    @Test
    public void sgroups() {
        CxSmilesState state = new CxSmilesState();
        state.sgroups = new ArrayList<>(1);
        state.sgroups.add(new CxSmilesState.PolymerSgroup("n", Arrays.asList(2,3), "n", "ht"));
        state.sgroups.add(new CxSmilesState.PolymerSgroup("n", Arrays.asList(5), "m", "ht"));
        assertThat(CxSmilesGenerator.generate(state, SmiFlavor.CxPolymer, new int[0], new int[]{7, 6, 5, 4, 3, 2, 1, 0}),
                   is(" |Sg:n:2:m:ht,Sg:n:4,5:n:ht|"));
    }

    @Test
    public void radicals() {
        CxSmilesState state = new CxSmilesState();
        state.atomRads = new HashMap<>();
        state.atomRads.put(2, CxSmilesState.Radical.Monovalent);
        state.atomRads.put(6, CxSmilesState.Radical.Monovalent);
        state.atomRads.put(4, CxSmilesState.Radical.Divalent);
        assertThat(CxSmilesGenerator.generate(state, SmiFlavor.CxSmiles, new int[0], new int[]{7, 6, 5, 4, 3, 2, 1, 0}),
                   is(" |^1:1,5,^2:3|"));
    }

}