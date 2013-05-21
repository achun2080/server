package fmagic.test.runner;

import java.util.ArrayList;
import java.util.List;

import fmagic.basic.context.Context;
import fmagic.server.application.ServerManager;
import fmagic.test.application.TestManager;
import fmagic.test.container.TestContainer;
import fmagic.test.container.TestContainerMediaLocalServer;
import fmagic.test.suite.TestSuite;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT>on server side.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 11.04.2013 - Created
 * 
 */
public class TestRunnerMediaServer extends TestRunner
{
	private static final String TEST_RUNNER_NAME = "medialocal";

	private ServerManager serverAp1 = null;
	private ServerManager serverAp2 = null;
	private ServerManager serverAp3 = null;

	/**
	 * Constructor
	 * 
	 * @param testSuite
	 *            The test suite that holds this test runner, or <TT>null</TT> if
	 *            no test suite is available.
	 * 
	 * @param testSessionName
	 *            The name of the test session.
	 */
	public TestRunnerMediaServer(TestSuite testSuite, String testSessionName)
	{
		// Call super class
		super(testSuite, TEST_RUNNER_NAME, testSessionName);
	}

	@Override
	public void setup()
	{
		try
		{
			/*
			 * Please notice that each application server is configured with
			 * specific settings via configuration files.
			 */

			// Create application servers for the test
			serverAp1 = this.createApplicationServer("ap1");
			serverAp2 = this.createApplicationServer("ap2");
			serverAp3 = this.createApplicationServer("ap3");
			
			// Cleanup media directories
			this.cleanTestMediaDirectory(serverAp1.getContext());
			this.cleanTestMediaDirectory(serverAp2.getContext());
			this.cleanTestMediaDirectory(serverAp3.getContext());
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
	public void executeSingleFunctionTest(TestContainer testContainer, String methodName)
	{
		this.doSingleFunctionTest(serverAp1.getContext(), testContainer, methodName);
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
		TestContainerMediaLocalServer testMediaSingle;
		
		int plainNumberOfMediaToBeUploaded = 50;
		int cycleNumberOfFiles = 100;
		int cycleDataIdentifierFrom = 1;
		int cycleDataIdentifierToo = 40;

		try
		{
			// Room
			testMediaSingle = new TestContainerMediaLocalServer(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Room");
			testMediaSingle.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaSingle.setParameterDataIdentifierTestUpload("1234");
			testMediaSingle.setParameterDataIdentifierTestObsolete("1235");
			testMediaSingle.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaSingle.setParameterTestCycleDataIdentifierFrom(cycleDataIdentifierFrom);
			testMediaSingle.setParameterTestCycleDataIdentifierToo(cycleDataIdentifierToo);
			testMediaSingle.executeComponentTest();

			// Floor (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocalServer(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Floor");
			testMediaSingle.cleanupComponentTest();

			// Bedroom (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocalServer(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Bedroom");
			testMediaSingle.cleanupComponentTest();

			// Kitchen (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocalServer(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Kitchen");
			testMediaSingle.cleanupComponentTest();

			// Bathroom (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocalServer(context, this, false);
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
		TestContainerMediaLocalServer testMediaConcurrent;
		List<Thread> threadList = new ArrayList<Thread>();
		
		int plainNumberOfMediaToBeUploaded = 50;
		int cycleNumberOfFiles = 100;
		int cycleDataIdentifierFrom = 1;
		int cycleDataIdentifierToo = 40;

		try
		{
			// Room 1
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1234");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1235");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(cycleDataIdentifierFrom);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(cycleDataIdentifierToo);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 2
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(cycleDataIdentifierFrom);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(cycleDataIdentifierToo);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 3
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(cycleDataIdentifierFrom);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(cycleDataIdentifierToo);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 4
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(cycleDataIdentifierFrom);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(cycleDataIdentifierToo);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 5
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(cycleDataIdentifierFrom);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(cycleDataIdentifierToo);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 6
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1244");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1245");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(cycleDataIdentifierFrom);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(cycleDataIdentifierToo);
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
		TestContainerMediaLocalServer testMediaConcurrent;
		List<Thread> threadList = new ArrayList<Thread>();
		
		int plainNumberOfMediaToBeUploaded = 50;
		int cycleNumberOfFiles = 100;

		try
		{
			// Room 1
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2345");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2346");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 2
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2347");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2348");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 3
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2349");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2350");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Floor
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Floor");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10);
			threadList.add(new Thread(testMediaConcurrent));

			// Bedroom
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bedroom");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(2);
			threadList.add(new Thread(testMediaConcurrent));

			// Kitchen
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Kitchen");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10000);
			threadList.add(new Thread(testMediaConcurrent));

			// Bathroom
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bathroom");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
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
		TestContainerMediaLocalServer testMediaConcurrent;
		
		int plainNumberOfMediaToBeUploaded = 50;
		int cycleNumberOfFiles = 50;

		try
		{
			// Room 1
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2345");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2346");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 2
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2347");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2348");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 3
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2349");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2350");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Floor
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Floor");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10);
			threadList.add(new Thread(testMediaConcurrent));

			// Bedroom
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bedroom");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(2);
			threadList.add(new Thread(testMediaConcurrent));

			// Kitchen
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Kitchen");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10000);
			threadList.add(new Thread(testMediaConcurrent));

			// Bathroom
			testMediaConcurrent = new TestContainerMediaLocalServer(this.serverAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bathroom");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(cycleNumberOfFiles);
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
