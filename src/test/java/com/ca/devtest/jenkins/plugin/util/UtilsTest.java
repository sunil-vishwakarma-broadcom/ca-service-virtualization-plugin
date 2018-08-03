/******************************************************************************
 *
 * Copyright (c) 2018 CA.  All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated
 * in any way except as authorized by the applicable license agreement,
 * without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT
 * WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN
 * NO EVENT WILL CA BE LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY
 * LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE USE OF THIS SOFTWARE,
 * INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR
 * DAMAGE.
 *
 * This file is made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 ******************************************************************************/

package com.ca.devtest.jenkins.plugin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import hudson.AbortException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Utils}.
 *
 * @author mykdm01
 */

public class UtilsTest {

	private Path baseDir;

	@Before
	public void prepare() throws IOException {
		baseDir = createTempStructure();
	}

	@Test
	public void testGetFilesMatchingWildcard_SearchAllSubfolders() throws IOException {
		List<String> paths = Utils
				.getFilesMatchingWildcard("/**/*.mar", baseDir.toString());
		assertEquals(paths.size(), 2);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));
		assertTrue(paths.get(1).endsWith(".mar"));
		assertTrue(paths.get(1).contains("/fff3"));

		paths = Utils
				.getFilesMatchingWildcard("**/*.mar", baseDir.toString());
		assertEquals(paths.size(), 2);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));
		assertTrue(paths.get(1).endsWith(".mar"));
		assertTrue(paths.get(1).contains("/fff3"));

		paths = Utils
				.getFilesMatchingWildcard("\\**/*.mar", baseDir.toString());
		assertEquals(paths.size(), 2);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));
		assertTrue(paths.get(1).endsWith(".mar"));
		assertTrue(paths.get(1).contains("/fff3"));
	}

	@Test
	public void testGetFilesMatchingWildcard_SearchInSpecificFolder() throws IOException {
		List<String> paths = Utils
				.getFilesMatchingWildcard("/*.mar", baseDir.toString());
		assertEquals(paths.size(), 1);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));

		paths = Utils
				.getFilesMatchingWildcard("\\*.mar", baseDir.toString());
		assertEquals(paths.size(), 1);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));

		paths = Utils
				.getFilesMatchingWildcard("*.mar", baseDir.toString());
		assertEquals(paths.size(), 1);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));
	}

	@Test
	public void testGetFilesMatchingWildcard_SearchForPartialName() throws IOException {
		List<String> paths = Utils
				.getFilesMatchingWildcard("/fff1*.mar", baseDir.toString());
		assertEquals(paths.size(), 1);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));

		paths = Utils
				.getFilesMatchingWildcard("/?ff1*.mar", baseDir.toString());
		assertEquals(paths.size(), 1);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));
	}

	@Test
	public void testGetFilesMatchingWildcard_SearchForPartialName2() throws IOException {
		List<String> paths = Utils
				.getFilesMatchingWildcard("/f*f1*.mar", baseDir.toString());
		assertEquals(paths.size(), 1);
		assertTrue(paths.get(0).endsWith(".mar"));
		assertTrue(paths.get(0).startsWith("fff1"));
	}

	@Test(expected = AbortException.class)
	public void testGetFilesMatchingWildcard_NothingMatches() throws IOException {
		List<String> paths = Utils
				.getFilesMatchingWildcard("*.test", baseDir.toString());
	}

	@Test(expected = AbortException.class)
	public void testGetFilesMatchingWildcard_NotWildcard() throws IOException {
		List<String> paths = Utils
				.getFilesMatchingWildcard("test", baseDir.toString());
		assertEquals(paths.size(), 1);
		assertTrue(paths.get(0).equals("test"));
	}

	@Test(expected = AbortException.class)
	public void testGetFilesMatchingWildcard_Null() throws IOException {
		List<String> paths = Utils
				.getFilesMatchingWildcard(null, null);
		assertEquals(paths.size(), 0);
	}

	private Path createTempStructure() throws IOException {
		Path tempDir = Files.createTempDirectory("temp");
		tempDir.toFile().deleteOnExit();
		Path inner = Files.createTempDirectory(tempDir, "inner");
		inner.toFile().deleteOnExit();
		File p1 = File.createTempFile("fff1", ".mar", tempDir.toFile());
		p1.deleteOnExit();
		File p2 = File.createTempFile("fff2", ".txt", tempDir.toFile());
		p2.deleteOnExit();
		File p3 = File.createTempFile("fff3", ".mar", inner.toFile());
		p3.deleteOnExit();
		File p4 = File.createTempFile("fff4", ".txt", inner.toFile());
		p4.deleteOnExit();
		return tempDir;
	}

}
