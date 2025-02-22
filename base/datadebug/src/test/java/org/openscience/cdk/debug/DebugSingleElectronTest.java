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
package org.openscience.cdk.debug;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.test.interfaces.AbstractSingleElectronTest;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.ISingleElectron;

/**
 * Checks the functionality of the {@link DebugSingleElectron}.
 *
 * @cdk.module test-datadebug
 */
public class DebugSingleElectronTest extends AbstractSingleElectronTest {

    @BeforeClass
    public static void setUp() {
        setTestObjectBuilder(DebugSingleElectron::new);
    }

    @Test
    public void testDebugSingleElectron() {
        ISingleElectron radical = new DebugSingleElectron();
        Assert.assertNull(radical.getAtom());
        Assert.assertEquals(1, radical.getElectronCount().intValue());
    }

    @Test
    public void testDebugSingleElectron_IAtom() {
        IAtom atom = newChemObject().getBuilder().newInstance(IAtom.class, "N");
        ISingleElectron radical = new DebugSingleElectron(atom);
        Assert.assertEquals(1, radical.getElectronCount().intValue());
        Assert.assertEquals(atom, radical.getAtom());
        Assert.assertTrue(radical.contains(atom));
    }
}
