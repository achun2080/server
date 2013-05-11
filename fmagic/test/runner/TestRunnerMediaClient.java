package fmagic.test.runner;

import java.util.List;

import fmagic.basic.context.Context;
import fmagic.basic.resource.ResourceManager;
import fmagic.client.application.ClientManager;
import fmagic.test.application.TestManager;
import fmagic.test.container.TestContainer;
import fmagic.test.container.TestContainerMediaLocal;
import fmagic.test.suite.TestSuite;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT> on client side.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 24.04.2013 - Created
 * 
 */
public class TestRunnerMediaClient extends TestRunner
{
	private static final String TESTCASE_NAME = "mediatestlocal";

	private ClientManager clientAp1 = null;
	private ClientManager clientAp2 = null;
	private ClientManager clientAp3 = null;

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
	public TestRunnerMediaClient(TestSuite testSuite, String testSessionName)
	{
		// Call super class
		super(testSuite, TESTCASE_NAME, testSessionName);

		// Clear test session directory
		TestManager.cleanTestSessionDirectory(this);
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

			// Create client applications for the test
			clientAp1 = this.createApplicationClient("cl1");
			clientAp2 = this.createApplicationClient("cl2");
			clientAp3 = this.createApplicationClient("cl3");
			
			// Set test parameter to local data of the clients
			Context context;
			
			context = clientAp1.getContext();
			context.getLocaldataManager().writeProperty(context, ResourceManager.localdata(context, "Media", "ClientEncodingKeyNumber"), "1");
			context.getLocaldataManager().writeProperty(context, ResourceManager.localdata(context, "Media", "ClientEncodingKeyList"), "1:ar34ab12 , 2:z778e15h");
			
			context = clientAp2.getContext();
			context.getLocaldataManager().writeProperty(context, ResourceManager.localdata(context, "Media", "ClientEncodingKeyNumber"), "3");
			context.getLocaldataManager().writeProperty(context, ResourceManager.localdata(context, "Media", "ClientEncodingKeyList"), "1:ar34ab12Xxx , 2:z778e15hYyy , 3:ui12ij45Zzz , 4:BB5678ab125ghiIHG");
			
			context = clientAp3.getContext();
			context.getLocaldataManager().writeProperty(context, ResourceManager.localdata(context, "Media", "ClientEncodingKeyNumber"), "5");
			context.getLocaldataManager().writeProperty(context, ResourceManager.localdata(context, "Media", "ClientEncodingKeyList"), "1:ar34ab12Aaa , 2:z778e15hYBbb , 3:ui12ij45ZCcc , 4:AA5678ab125ghiIHGDdd, 5:12hszs99uejrEee");

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
			// Release client applications
			this.releaseApplicationClient(clientAp1);
			this.releaseApplicationClient(clientAp2);
			this.releaseApplicationClient(clientAp3);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void executeSingleFunctionTest(TestContainer testContainer, String methodName)
	{
		this.doSingleFunctionTest(clientAp1.getContext(), testContainer, methodName);
	}

	@Override
	public void executeComponentTest()
	{
		try
		{
			if (clientAp1 != null) this.doComponentTest(clientAp1.getContext());
			if (clientAp2 != null) this.doComponentTest(clientAp2.getContext());
			if (clientAp3 != null) this.doComponentTest(clientAp3.getContext());
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
		TestContainerMediaLocal testMediaSingle;

		try
		{
			// Room
			testMediaSingle = new TestContainerMediaLocal(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Room");
			testMediaSingle.setParameterNumberOfMediaToBeUploaded(50);
			testMediaSingle.setParameterDataIdentifierTestUpload("5001");
			testMediaSingle.setParameterDataIdentifierTestObsolete("5002");
			testMediaSingle.setParameterTestCycleNumberOfFiles(100);
			testMediaSingle.setParameterTestCycleDataIdentifierFrom(1);
			testMediaSingle.setParameterTestCycleDataIdentifierToo(40);
			testMediaSingle.executeComponentTest();

			// Floor (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocal(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Floor");
			testMediaSingle.cleanupComponentTest();

			// Bedroom (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocal(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Bedroom");
			testMediaSingle.cleanupComponentTest();

			// Kitchen (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocal(context, this, false);
			testMediaSingle.setParameterResourceGroup("Apartment");
			testMediaSingle.setParameterResourceName("Kitchen");
			testMediaSingle.cleanupComponentTest();

			// Bathroom (don't test, cleanup only)
			testMediaSingle = new TestContainerMediaLocal(context, this, false);
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
		return;
	}

	/**
	 * Concurrent Test
	 */
	private void doStressTest()
	{
		return;
	}

	/**
	 * Integration Test
	 */
	private void defineIntegrationTest(List<Thread> threadList)
	{
		TestContainerMediaLocal testMediaConcurrent;

		try
		{
			// Room 1
			testMediaConcurrent = new TestContainerMediaLocal(this.clientAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(50);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2345");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2346");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(100);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 2
			testMediaConcurrent = new TestContainerMediaLocal(this.clientAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(50);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2347");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2348");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(100);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Room 3
			testMediaConcurrent = new TestContainerMediaLocal(this.clientAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Room");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(50);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("2349");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("2350");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(100);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(40);
			threadList.add(new Thread(testMediaConcurrent));

			// Floor
			testMediaConcurrent = new TestContainerMediaLocal(this.clientAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Floor");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(50);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1236");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1237");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(100);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10);
			threadList.add(new Thread(testMediaConcurrent));

			// Bedroom
			testMediaConcurrent = new TestContainerMediaLocal(this.clientAp2.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bedroom");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(50);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1238");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1239");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(100);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(2);
			threadList.add(new Thread(testMediaConcurrent));

			// Kitchen
			testMediaConcurrent = new TestContainerMediaLocal(this.clientAp3.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Kitchen");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(50);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1240");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1241");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(100);
			testMediaConcurrent.setParameterTestCycleDataIdentifierFrom(1);
			testMediaConcurrent.setParameterTestCycleDataIdentifierToo(10000);
			threadList.add(new Thread(testMediaConcurrent));

			// Bathroom
			testMediaConcurrent = new TestContainerMediaLocal(this.clientAp1.getContext(), this, true);
			testMediaConcurrent.setParameterResourceGroup("Apartment");
			testMediaConcurrent.setParameterResourceName("Bathroom");
			testMediaConcurrent.setParameterNumberOfMediaToBeUploaded(50);
			testMediaConcurrent.setParameterDataIdentifierTestUpload("1242");
			testMediaConcurrent.setParameterDataIdentifierTestObsolete("1243");
			testMediaConcurrent.setParameterTestCycleNumberOfFiles(100);
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
