/* Copyright (C) 1997-2007  The Chemistry Development Kit (CDK) project
 *
 * Contact: cdk-devel@lists.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.openscience.cdk.graph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.*;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.HINReader;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.sgroup.Sgroup;
import org.openscience.cdk.sgroup.SgroupType;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.templates.TestMoleculeFactory;
import org.openscience.cdk.test.CDKTestCase;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Checks the functionality of the ConnectivityChecker
 *
 * @author steinbeck
 * @cdk.module test-standard
 * @cdk.created 2001-07-24
 */
public class ConnectivityCheckerTest extends CDKTestCase {

    public ConnectivityCheckerTest() {
        super();
    }

    /**
     * This test tests the function of the partitionIntoMolecule() method.
     */
    @Test
    public void testPartitionIntoMolecules_IAtomContainer() {
        //logger.debug(atomCon);
        AtomContainer atomCon = new org.openscience.cdk.AtomContainer();
        atomCon.add(TestMoleculeFactory.make4x3CondensedRings());
        atomCon.add(TestMoleculeFactory.makeAlphaPinene());
        atomCon.add(TestMoleculeFactory.makeSpiroRings());
        IAtomContainerSet moleculeSet = ConnectivityChecker.partitionIntoMolecules(atomCon);
        Assert.assertNotNull(moleculeSet);
        Assert.assertEquals(3, moleculeSet.getAtomContainerCount());
    }

    /**
     * Test for SF bug #903551
     */
    @Test
    public void testPartitionIntoMoleculesKeepsAtomIDs() {
        AtomContainer atomCon = new org.openscience.cdk.AtomContainer();
        Atom atom1 = new Atom("C");
        atom1.setID("atom1");
        Atom atom2 = new Atom("C");
        atom2.setID("atom2");
        atomCon.addAtom(atom1);
        atomCon.addAtom(atom2);
        IAtomContainerSet moleculeSet = ConnectivityChecker.partitionIntoMolecules(atomCon);
        Assert.assertNotNull(moleculeSet);
        Assert.assertEquals(2, moleculeSet.getAtomContainerCount());
        org.openscience.cdk.interfaces.IAtom copy1 = moleculeSet.getAtomContainer(0).getAtom(0);
        org.openscience.cdk.interfaces.IAtom copy2 = moleculeSet.getAtomContainer(1).getAtom(0);

        Assert.assertEquals(atom1.getID(), copy1.getID());
        Assert.assertEquals(atom2.getID(), copy2.getID());
    }

    /**
     * This test tests the consistency between isConnected() and
     * partitionIntoMolecules().
     */
    @Test
    public void testPartitionIntoMolecules_IsConnected_Consistency() {
        //logger.debug(atomCon);
        AtomContainer atomCon = new org.openscience.cdk.AtomContainer();
        atomCon.add(TestMoleculeFactory.make4x3CondensedRings());
        atomCon.add(TestMoleculeFactory.makeAlphaPinene());
        atomCon.add(TestMoleculeFactory.makeSpiroRings());
        IAtomContainerSet moleculeSet = ConnectivityChecker.partitionIntoMolecules(atomCon);
        Assert.assertNotNull(moleculeSet);
        Assert.assertEquals(3, moleculeSet.getAtomContainerCount());

        Assert.assertTrue(ConnectivityChecker.isConnected(moleculeSet.getAtomContainer(0)));
        Assert.assertTrue(ConnectivityChecker.isConnected(moleculeSet.getAtomContainer(1)));
        Assert.assertTrue(ConnectivityChecker.isConnected(moleculeSet.getAtomContainer(2)));
    }

    /**
     * This test makes sure that it is checked that the partitionIntoMolecules()
     * method keeps LonePairs and SingleElectrons with its associated atoms.
     */
    @Test
    public void testDontDeleteSingleElectrons() {
        AtomContainer atomCon = new org.openscience.cdk.AtomContainer();
        // make two molecules; one with an LonePair, the other with a SingleElectron
        IAtomContainer mol1 = new AtomContainer();
        Atom atom1 = new Atom("C");
        mol1.addAtom(atom1);
        LonePair lp1 = new LonePair(atom1);
        mol1.addLonePair(lp1);
        // mol2
        IAtomContainer mol2 = new AtomContainer();
        Atom atom2 = new Atom("C");
        mol2.addAtom(atom2);
        SingleElectron se2 = new SingleElectron(atom2);
        mol2.addSingleElectron(se2);

        atomCon.add(mol1);
        atomCon.add(mol2);

        // now partition
        IAtomContainerSet moleculeSet = ConnectivityChecker.partitionIntoMolecules(atomCon);
        Assert.assertNotNull(moleculeSet);
        Assert.assertEquals(2, moleculeSet.getAtomContainerCount());

        Assert.assertTrue(ConnectivityChecker.isConnected(moleculeSet.getAtomContainer(0)));
        Assert.assertTrue(ConnectivityChecker.isConnected(moleculeSet.getAtomContainer(1)));

        // make sure
        Assert.assertEquals(1, moleculeSet.getAtomContainer(0).getAtomCount());
        Assert.assertEquals(1, moleculeSet.getAtomContainer(0).getElectronContainerCount());
        Assert.assertEquals(1, moleculeSet.getAtomContainer(1).getAtomCount());
        Assert.assertEquals(1, moleculeSet.getAtomContainer(1).getElectronContainerCount());
        // we don't know which partition contains the LP and which the electron
        Assert.assertTrue(moleculeSet.getAtomContainer(0).getConnectedSingleElectronsCount(
                moleculeSet.getAtomContainer(0).getAtom(0)) == 0
                || moleculeSet.getAtomContainer(1).getConnectedSingleElectronsCount(
                moleculeSet.getAtomContainer(1).getAtom(0)) == 0);
        Assert.assertTrue(moleculeSet.getAtomContainer(0).getConnectedLonePairsCount(
                moleculeSet.getAtomContainer(0).getAtom(0)) == 0
                || moleculeSet.getAtomContainer(1).getConnectedLonePairsCount(
                moleculeSet.getAtomContainer(1).getAtom(0)) == 0);
    }

    /**
     * This test tests the algorithm behind isConnected().
     */
    @Test
    public void testIsConnected_IAtomContainer() {
        IAtomContainer spiro = TestMoleculeFactory.makeSpiroRings();
        Assert.assertTrue(ConnectivityChecker.isConnected(spiro));
    }

    @Test
    public void testIsConnectedArtemisinin1() throws InvalidSmilesException {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        IAtomContainer container = sp.parseSmiles("C1CN2CCN(CCCN(CCN(C1)Cc1ccccn1)CC2)C");
        Assert.assertTrue(ConnectivityChecker.isConnected(container));
    }

    /**
     * @cdk.bug 2126904
     */
    @Test
    public void testIsConnectedFromHINFile() throws Exception {
        String filename = "connectivity1.hin";
        InputStream ins = this.getClass().getResourceAsStream(filename);
        ISimpleChemObjectReader reader = new HINReader(ins);
        ChemFile content = (ChemFile) reader.read((ChemObject) new ChemFile());
        List<IAtomContainer> cList = ChemFileManipulator.getAllAtomContainers(content);
        IAtomContainer ac = cList.get(0);

        Assert.assertTrue("Molecule appears not to be connected", ConnectivityChecker.isConnected(ac));
    }

    /**
     * @cdk.bug 2126904
     */
    @Test
    public void testIsConnectedFromSDFile() throws Exception {
        String filename = "mdeotest.sdf";
        InputStream ins = this.getClass().getResourceAsStream(filename);
        ISimpleChemObjectReader reader = new MDLV2000Reader(ins);
        ChemFile content = (ChemFile) reader.read((ChemObject) new ChemFile());
        List<IAtomContainer> cList = ChemFileManipulator.getAllAtomContainers(content);
        IAtomContainer ac = cList.get(0);

        Assert.assertTrue("Molecule appears not to be connected", ConnectivityChecker.isConnected(ac));
    }

    @Test
    public void testPartitionExtendedTetrahedral() throws Exception {
        SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
        IAtomContainer container = smipar.parseSmiles("CC=[C@]=CC.C");
        IAtomContainerSet containerSet = ConnectivityChecker.partitionIntoMolecules(container);
        assertThat(containerSet.getAtomContainerCount(), is(2));
        assertTrue(containerSet.getAtomContainer(0).stereoElements().iterator().hasNext());
    }

    /**
     * @cdk.bug 2784209
     */
    @Test
    public void testNoAtomsIsConnected() {
        IAtomContainer container = new AtomContainer();
        Assert.assertTrue("Molecule appears not to be connected", ConnectivityChecker.isConnected(container));
    }

    @Test
    public void copySgroups() throws Exception {
        String filename = "sgroup-split.mol";
        try (InputStream ins = this.getClass().getResourceAsStream(filename);
             ISimpleChemObjectReader reader = new MDLV2000Reader(ins)
        ) {
            ChemFile content = (ChemFile) reader.read((ChemObject) new ChemFile());
            List<IAtomContainer> cList = ChemFileManipulator.getAllAtomContainers(content);
            IAtomContainer ac = cList.get(0);
            IAtomContainerSet containerSet = ConnectivityChecker.partitionIntoMolecules(ac);
            Assert.assertEquals(2, containerSet.getAtomContainerCount());
            IAtomContainer container1 = containerSet.getAtomContainer(0);
            IAtomContainer container2 = containerSet.getAtomContainer(1);
            IAtomContainer h2o = container1.getAtomCount() <= 3 ? container1 : container2;
            Assert.assertNull(h2o.getProperty(CDKConstants.CTAB_SGROUPS));
            IAtomContainer otherContainer = h2o == container1 ? container2 : container1;
            List<Sgroup> sgroups = otherContainer.getProperty(CDKConstants.CTAB_SGROUPS);
            Assert.assertEquals(1, sgroups.size());
            Sgroup sgroup = sgroups.get(0);
            Assert.assertEquals(SgroupType.CtabStructureRepeatUnit, sgroup.getType());
            Set<IAtom> atoms = sgroup.getAtoms();
            Assert.assertEquals(2, atoms.size());

        }
    }

    @Test
    public void splitSgroups() throws IOException, CDKException {
        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
        try (InputStream in = getClass().getResourceAsStream("sgroup-frags.mol");
             MDLV2000Reader mdlr = new MDLV2000Reader(in)) {
            IAtomContainer mol = mdlr.read(builder.newAtomContainer());
            IAtomContainerSet acset = ConnectivityChecker.partitionIntoMolecules(mol);
            Assert.assertEquals(2, acset.getAtomContainerCount());
            SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.CxSmiles);
            Assert.assertEquals("CCOC |Sg:n:1,2:1:|", smigen.create(acset.getAtomContainer(0)));
            Assert.assertEquals("C(NC)C |Sg:n:0,1:1:|", smigen.create(acset.getAtomContainer(1)));
        }
    }

    @Test
    public void splitSgroupsParent() throws IOException, CDKException {
        IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
        try (InputStream in = getClass().getResourceAsStream("sgroup-mix.mol");
             MDLV2000Reader mdlr = new MDLV2000Reader(in)) {
            IAtomContainer mol = mdlr.read(builder.newAtomContainer());
            IAtomContainerSet acset = ConnectivityChecker.partitionIntoMolecules(mol);
            Assert.assertEquals(2, acset.getAtomContainerCount());
            SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.CxSmiles);
            IAtomContainer part1 = acset.getAtomContainer(0);
            IAtomContainer part2 = acset.getAtomContainer(1);
            Assert.assertEquals("C1CCCCC1 |Sg:c:0,1,2,3,4,5::|",
                    smigen.create(part1));
            Assert.assertEquals("CO |Sg:c:0,1::|",
                    smigen.create(part2));
            List<Sgroup> sgroups1 = part1.getProperty(CDKConstants.CTAB_SGROUPS);
            List<Sgroup> sgroups2 = part2.getProperty(CDKConstants.CTAB_SGROUPS);
            Assert.assertEquals(1, sgroups1.size());
            Assert.assertEquals(0, sgroups1.get(0).getParents().size());
            Assert.assertEquals(1, sgroups2.size());
            Assert.assertEquals(0, sgroups2.get(0).getParents().size());
            List<Sgroup> orgSgroups = mol.getProperty(CDKConstants.CTAB_SGROUPS);
            assertThat(orgSgroups.size(), is(3));
            assertNonEmptySgrpParent(orgSgroups);
        }
    }

    // check at least one of the sgroups has a non-empty parent
    private void assertNonEmptySgrpParent(List<Sgroup> orgSgroups) {
        boolean found = false;
        for (Sgroup orgSgroup : orgSgroups) {
            if (!orgSgroup.getParents().isEmpty())
                found = true;
        }
        assertTrue(found);
    }

}
