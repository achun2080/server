package fmagic.test;

import java.util.ArrayList;
import java.util.List;

import fmagic.basic.Context;
import fmagic.server.ServerManager;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT>.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 11.04.2013 - Created
 * 
 */
public class TestRunnerMedia extends TestRunner
{
	private static final String TESTCASE_NAME = "mediatest";

	private ServerManager serverAp1 = null;
	private ServerManager serverAp2 = null;
	private ServerManager serverAp3 = null;

	/**
	 * Constructor
	 */
	public TestRunnerMedia(String testSessionName)
	{
		super(TESTCASE_NAME, testSessionName);
	}

	@Override
	public void setup()
	{
		try
		{
			// Clear test session directory
			TestManager.cleanTestSessionDirectory(this);
			
			/*
			 * Please notice that each application server is configured with
			 * specific settings via configuration files.
			 */

			// Create application servers for the test
			serverAp1 = this.createApplicationServer("ap1");
			serverAp2 = this.createApplicationServer("ap2");
			serverAp3 = this.createApplicationServer("ap3");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void cleanup()
	{
		try
		{
			this.releaseApplicationServer(serverAp1);
			this.releaseApplicationServer(serverAp2);
			this.releaseApplicationServer(serverAp3);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void executeSingleFunctionTest(ServerTestContainer serverTestContainer, String methodName)
	{
		this.doSingleFunctionTest(serverAp1.getContext(), serverTestContainer, methodName);
	}

	@Override
	public void executeComponentTest()
	{
		try
		{
			if (serverAp1 != null) this.doComponentTest(serverAp1.getContext());
			if (serverAp2 != null) this.doComponentTest(serverAp2.getContext());
			if (serverAp3 != null) this.doComponentTest(serverAp3.getContext());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void executeConcurrentTest()
	{
		try
		{
			this.doConcurrentTest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void executeStressTest()
	{
		try
		{
			this.doStressTest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void collectIntegrationTestContainer(List<Thread> threadList)
	{
		try
		{
			this.defineIntegrationTest(threadList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Component Test
	 */
	private void doComponentTest(Context context)
	{
		ServerTestContainerMedia testMediaSingle;

		try
		{
			// Room
			testMediaSingle = new ServerTestContainerMedia(context, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Room");
			testMediaSingle.setParameterDataIdentifierTestUpload("1234");
			testMediaSingle.setParameterDataIdentifierTestObsolete("1235");
			testMediaSingle.setParameterTestCycleNumberOfFiles(100);
			testMediaSingle.setParameterTestCycleDataIdentifierFrom(1);
			testMediaSingle.setParameterTestCycleDataIdentifierToo(40);
			testMediaSingle.executeComponentTest();

			// Floor (don't test, cleanup only)
			testMediaSingle = new ServerTestContainerMedia(context, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Floor");
			testMediaSingle.cleanupComponentTest();

			// Bedroom (don't test, cleanup only)
			testMediaSingle = new ServerTestContainerMedia(context, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Bedroom");
			testMediaSingle.cleanupComponentTest();

			// Kitchen (don't test, cleanup only)
			testMediaSingle = new ServerTestContainerMedia(context, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Kitchen");
			testMediaSingle.cleanupComponentTest();

			// Bathroom (don't test, cleanup only)
			testMediaSingle = new ServerTestContainerMedia(context, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Bathroom");
			testMediaSingle.cleanupComponentTest();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Concurrent Test
	 */
	private void doConcurrentTest()
	{
		ServerTestContainerMedia testMediaConcurrent;
		List<Thread> threadList = new ArrayList<Thread>();

		int nuOfFiles = 1000;

		try
		{
			// Room 1
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1234");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1235");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 2
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp2.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 3
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp3.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 4
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 5
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp2.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 6
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp3.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1244");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1245");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Start all threads parallel
			TestManager.threadListStart(threadList);

			// Wait for the end of all threads
			TestManager.threadListJoin(threadList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Concurrent Test
	 */
	private void doStressTest()
	{
		ServerTestContainerMedia testMediaConcurrent;
		List<Thread> threadList = new ArrayList<Thread>();

		int nuOfFiles = 1000;

		try
		{
			// Room 1
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2345");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2346");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 2
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp2.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2347");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2348");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 3
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp3.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2349");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2350");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Floor
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Floor");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10);
			threadList.add(new Thread(testMediaConcurrent));

			// Bedroom
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp2.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bedroom");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(2);
			threadList.add(new Thread(testMediaConcurrent));

			// Kitchen
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp3.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Kitchen");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10000);
			threadList.add(new Thread(testMediaConcurrent));

			// Bathroom
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bathroom");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(500);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(1000);
			threadList.add(new Thread(testMediaConcurrent));

			// Start all threads parallel
			TestManager.threadListStart(threadList);

			// Wait for the end of all threads
			TestManager.threadListJoin(threadList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Integration Test
	 */
	private void defineIntegrationTest(List<Thread> threadList)
	{
		ServerTestContainerMedia testMediaConcurrent;

		int nuOfFiles = 1000;

		try
		{
			// Room 1
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2345");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2346");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 2
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp2.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2347");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2348");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 3
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp3.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2349");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2350");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Floor
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Floor");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10);
			threadList.add(new Thread(testMediaConcurrent));

			// Bedroom
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp2.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bedroom");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(2);
			threadList.add(new Thread(testMediaConcurrent));

			// Kitchen
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp3.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Kitchen");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10000);
			threadList.add(new Thread(testMediaConcurrent));

			// Bathroom
			testMediaConcurrent = new ServerTestContainerMedia(this.serverAp1.getContext(), true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bathroom");
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(nuOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(500);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(1000);
			threadList.add(new Thread(testMediaConcurrent));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
