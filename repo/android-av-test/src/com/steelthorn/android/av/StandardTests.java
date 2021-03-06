/*******************************************************************************
 * Copyright (c) 2013 Jeff Mixon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * (or any later version, at your option)  which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jeff - initial API and implementation
 ******************************************************************************/
package com.steelthorn.android.av;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

public class StandardTests extends ScanTests
{
	private static final String TEST_PACKAGE = "com.example.android.softkeyboard";
	//private static final String TEST_PACKAGE = "com.rdio.android.ui";
	private static final String TEST_PACKAGE_PARTIAL = "com.example.android.soft";
	//private static final String TEST_PACKAGE_PARTIAL = "com.rdio.android";
	//private static final String TEST_PARTIAL_WEIGHT1 = "com.example";
	//private static final String TEST_PARTIAL_WEIGHT1 = "com.rdio";
	//private static final String TEST_PARTIAL_WEIGHT2 = "android.softkeyboard";
	//private static final String TEST_PARTIAL_WEIGHT2 = "android.ui";

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
	}

	@Test
	public void testMatch() throws Exception
	{
		IScanTarget target = new IScanTarget()
		{
			public String getName()
			{
				return "Test";
			};

			public byte[] getHashValue()
			{
				return Base64.decode("3YnpxrvKu5hZxi0m/FkpE+pUcwQ=", Base64.DEFAULT);
			}

			public byte getTargetType()
			{
				return 1;
			}

			public boolean checkThreat(IScanDefinition criteria)
			{

				return Arrays.equals(getHashValue(), criteria.getHashValue());
			}

			@Override
			public int compareTo(IScanTarget another)
			{
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public long getSize()
			{
				return getHashValue().length;
			}
		};

		IThreatInfo ti = ScanEngine.getDefaultScanEngine().scanTarget(target, new DevDefinitionProvider());

		Assert.assertNotNull(ti);

		Assert.assertTrue(ti.getConfidence() >= 1);
	}

	@Test
	public void testBasicScan() throws Exception
	{
		final DebugScanListener listener = new DebugScanListener();

		Thread t = new Thread()
		{
			public void run()
			{
				new ScanManager().performBasicScan(getContext(), listener);
			}
		};

		t.start();
		t.join();

		Assert.assertFalse(listener._lastResult.getMatchesFound());
	}

	@Test
	public void testPositivePackage() throws Exception
	{
		ITargetSource source = new InstalledTargetSource(getContext());

		DemoDefinition criteria = createDefGroup(TEST_PACKAGE);

		boolean matchFound = false;
		for (IScanTarget t : source)
		{
			matchFound = t.checkThreat(criteria);

			if (matchFound)
				break;
		}

		Assert.assertTrue(matchFound);
	}

	@Test
	public void testIncorrectPosition() throws Exception
	{
		ITargetSource source = new InstalledTargetSource(getContext());

		DemoDefinition criteria = createDefGroup(TEST_PACKAGE);
		criteria.setMatchPos(1);

		boolean matchFound = false;
		for (IScanTarget t : source)
		{
			matchFound = t.checkThreat(criteria);

			if (matchFound)
				break;
		}

		Assert.assertFalse(matchFound);
	}

	@Test
	public void testIncorrectLengthShallow() throws Exception
	{
		ITargetSource source = new InstalledTargetSource(getContext());

		DemoDefinition criteria = createDefGroup(TEST_PACKAGE);
		//criteria.setMatchPos(criteria.getMatchPosition()-1);
		criteria.setMatchSize(criteria.getMatchSize() - 1);

		boolean matchFound = false;
		for (IScanTarget t : source)
		{
			matchFound = t.checkThreat(criteria);

			if (matchFound)
				break;
		}

		Assert.assertFalse(matchFound);
	}

	@Test
	public void testIncorrectLengthDeepStillMatches() throws Exception
	{
		ITargetSource source = new InstalledTargetSource(getContext());

		DemoDefinition criteria = createDefGroup(TEST_PACKAGE);
		criteria.setMatchSize(criteria.getMatchSize() + 1);

		boolean matchFound = false;
		for (IScanTarget t : source)
		{
			matchFound = t.checkThreat(criteria);

			if (matchFound)
				break;
		}

		Assert.assertTrue(matchFound);
	}

	@Test
	public void testPartialPositive() throws Exception
	{
		ITargetSource source = new InstalledTargetSource(getContext());

		DemoDefinition criteria = createDefGroup(TEST_PACKAGE_PARTIAL);

		boolean matchFound = false;
		for (IScanTarget t : source)
		{
			matchFound = t.checkThreat(criteria);

			if (matchFound)
				break;
		}

		Assert.assertTrue(matchFound);
	}

	@Test
	public void testPartialIncorrectLength() throws Exception
	{
		ITargetSource source = new InstalledTargetSource(getContext());

		DemoDefinition criteria = createDefGroup(TEST_PACKAGE_PARTIAL);
		criteria.setMatchSize(criteria.getMatchSize() + 1);

		boolean matchFound = false;
		for (IScanTarget t : source)
		{
			matchFound = t.checkThreat(criteria);

			if (matchFound)
				break;
		}

		Assert.assertFalse(matchFound);
	}

	@Test
	public void testPartialIncorrectLengthShallow() throws Exception
	{
		ITargetSource source = new InstalledTargetSource(getContext());

		DemoDefinition criteria = createDefGroup(TEST_PACKAGE_PARTIAL);
		criteria.setMatchSize(criteria.getMatchSize() - 1);

		boolean matchFound = false;
		for (IScanTarget t : source)
		{
			matchFound = t.checkThreat(criteria);

			if (matchFound)
				break;
		}

		Assert.assertFalse(matchFound);
	}

	@Test
	public void testScanCancel() throws Exception
	{
		final IScanListener listenMock = mock(IScanListener.class);

		final ScanEngine engine = ScanEngine.getDefaultScanEngine();

		new Thread()
		{
			public void run()
			{
				engine.scan(new BasicScanContext(getContext(), listenMock), new DevDefinitionProvider());
			}
		}.start();

		engine.cancel();

		Thread.sleep(250);

		verify(listenMock).onScanCanceled(any(ScanResult.class));
	}
}
