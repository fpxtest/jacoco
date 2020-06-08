/*******************************************************************************
 * Copyright (c) 2009, 2021 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.agent.rt.internal.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.OverlappingFileLockException;

import org.jacoco.core.runtime.AgentOptions;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link FileOutput}.
 */
public class FileOutputTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void startup_should_create_empty_execfile() throws Exception {
		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());

		FileOutput controller = new FileOutput();
		controller.startup(options, new RuntimeData());

		assertTrue("Execution data file should be created", destFile.exists());
		assertEquals("Execution data file should be empty", 0,
				destFile.length());
	}

	@Test
	public void shutdown_should_write_execdata() throws Exception {
		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());

		FileOutput controller = new FileOutput();
		controller.startup(options, new RuntimeData());
		controller.writeExecutionData(false);
		controller.shutdown();

		assertTrue("Execution data file should be created", destFile.exists());
		assertTrue("Execution data file should have contents",
				destFile.length() > 0);
	}

	@Test(expected = IOException.class)
	public void startup_should_throw_IOException_when_execfile_cannot_be_created()
			throws Exception {
		AgentOptions options = new AgentOptions();
		options.setDestfile(folder.newFolder("folder").getAbsolutePath());
		FileOutput controller = new FileOutput();

		// Startup should fail as the file can not be created:
		controller.startup(options, new RuntimeData());
	}

	@Test(expected = OverlappingFileLockException.class)
	public void startup_should_throws_OverlappingFileLockException_when_execfile_is_permanently_locked()
			throws Exception {
		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());

		FileOutputStream out = new FileOutputStream(destFile);
		try {
			out.getChannel().lock();
			FileOutput controller = new FileOutput();
			controller.startup(options, new RuntimeData());
		} finally {
			out.close();
		}
	}

	@Test(expected = InterruptedIOException.class)
	public void startup_should_throws_InterruptedIOException_when_execfile_is_locked_and_thread_is_interrupted()
			throws Exception {
		File destFile = folder.newFile("jacoco.exec");
		AgentOptions options = new AgentOptions();
		options.setDestfile(destFile.getAbsolutePath());

		FileOutputStream out = new FileOutputStream(destFile);
		try {
			out.getChannel().lock();
			FileOutput controller = new FileOutput();
			Thread.currentThread().interrupt();
			controller.startup(options, new RuntimeData());
		} finally {
			out.close();
		}
	}
}
