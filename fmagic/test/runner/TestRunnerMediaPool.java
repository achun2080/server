package fmagic.test.runner;

import java.util.List;

import fmagic.server.application.ServerManager;
import fmagic.test.container.TestContainer;
import fmagic.test.container.TestContainerMediaPool;
import fmagic.test.suite.TestSuite;

/**
 * This class implements testing functionality regarding the
 * <TT>Media Manager</TT> using a media pool.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 29.04.2013 - Created
 * 
 */
public class TestRunnerMediaPool extends TestRunner
{
	private static final String TEST_RUNNER_NAME = "mediapool";

	private ServerManager serverAp1 = null;

	/**
	 * Constructor
	 * 
	 * @param testSuite
	 *            The test suite that holds this test runner, or <TT>null</TT>
	 *            if no test suite is available.
	 * 
	 * @param testSessionName
	 *            The name of the test session.
	 */
	public TestRunnerMediaPool(TestSuite testSuite, String testSessionName)
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
			 * Please notice that each application is configured with specific
			 * settings via configuration files.
			 */

			// Create application servers for the test
			serverAp1 = this.createApplicationServer("ap1");
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
			// Release application servers
			if (serverAp1 != null) this.releaseApplicationServer(serverAp1);
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
			this.doComponentTest1();
			this.doComponentTest2();
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
	 * Component Test: Test Case 1
	 * 
	 * One application server refers too four media server, combined to a media
	 * pool. All servers are running and online.
	 */
	private void doComponentTest1()
	{
		try
		{
			TestContainerMediaPool testContainer;

			int plainNumberOfMediaToBeUploaded = 100;

			/*
			 * Please notice that each application is configured with specific
			 * settings via configuration files.
			 */

			// Create and start servers
			ServerManager serverMs1 = this.createApplicationServer("ms1", 8021);
			ServerManager serverMs2 = this.createApplicationServer("ms2", 8022);
			ServerManager serverMs3 = this.createApplicationServer("ms3", 8023);
			ServerManager serverMs4 = this.createApplicationServer("ms4", 8024);
			ServerManager serverAp12340 = this.createApplicationServer("ap1234-", 8010);

			// Cleanup media directories
			this.cleanTestMediaDirectory(serverMs1.getContext());
			this.cleanTestMediaDirectory(serverMs2.getContext());
			this.cleanTestMediaDirectory(serverMs3.getContext());
			this.cleanTestMediaDirectory(serverMs4.getContext());
			this.cleanTestMediaDirectory(serverAp12340.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp12340.getContext(), this, false);
			testContainer.setParameterServer(serverAp12340);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp12340.getContext(), this, false);
			testContainer.setParameterServer(serverAp12340);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Hall");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp12340.getContext(), this, false);
			testContainer.setParameterServer(serverAp12340);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Office");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp12340.getContext(), this, false);
			testContainer.setParameterServer(serverAp12340);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Canteen");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp12340.getContext(), this, false);
			testContainer.setParameterServer(serverAp12340);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(3001);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(plainNumberOfMediaToBeUploaded);
			testContainer.executeComponentTest();

			// Release all servers
			if (serverAp12340 != null) this.releaseApplicationServer(serverAp12340);
			if (serverMs1 != null) this.releaseApplicationServer(serverMs1);
			if (serverMs2 != null) this.releaseApplicationServer(serverMs2);
			if (serverMs3 != null) this.releaseApplicationServer(serverMs3);
			if (serverMs4 != null) this.releaseApplicationServer(serverMs4);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Component Test: Test Case 2
	 * 
	 * A media pool of 4 media servers is filled one by one with a subset of
	 * media. That means, each media server of the pool holds some media files
	 * only, not never all of them. After filling the particular media servers
	 * they are bound together, and all media are read in general, all the same
	 * from which media server they are loaded.
	 */
	private void doComponentTest2()
	{
		try
		{
			TestContainerMediaPool testContainer;

			int mediaServer1DataIdentifierFrom = 1;
			int mediaServer1DataIdentifierToo = 20;
			int mediaServer2DataIdentifierFrom = 21;
			int mediaServer2DataIdentifierToo = 40;
			int mediaServer3DataIdentifierFrom = 41;
			int mediaServer3DataIdentifierToo = 60;
			int mediaServer4DataIdentifierFrom = 61;
			int mediaServer4DataIdentifierToo = 80;

			/*
			 * Please notice that each application is configured with specific
			 * settings via configuration files.
			 */

			/*
			 * Create and start all media servers
			 */

			ServerManager serverMs1 = this.createApplicationServer("ms1", 8021);
			ServerManager serverMs2 = this.createApplicationServer("ms2", 8022);
			ServerManager serverMs3 = this.createApplicationServer("ms3", 8023);
			ServerManager serverMs4 = this.createApplicationServer("ms4", 8024);
			ServerManager serverMs5 = this.createApplicationServer("ms5", 8025);

			// Cleanup media directories
			this.cleanTestMediaDirectory(serverMs1.getContext());
			this.cleanTestMediaDirectory(serverMs2.getContext());
			this.cleanTestMediaDirectory(serverMs3.getContext());
			this.cleanTestMediaDirectory(serverMs4.getContext());
			this.cleanTestMediaDirectory(serverMs5.getContext());

			/*
			 * Fill first media server
			 */

			ServerManager serverAp10000 = this.createApplicationServer("ap1----", 8010);
			this.cleanTestMediaDirectory(serverAp10000.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp10000.getContext(), this, false);
			testContainer.setParameterServer(serverAp10000);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(mediaServer1DataIdentifierFrom);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(mediaServer1DataIdentifierToo);
			testContainer.executeComponentTest();

			// Release application server
			if (serverAp10000 != null) this.releaseApplicationServer(serverAp10000);

			/*
			 * Fill second media server
			 */

			ServerManager serverAp02000 = this.createApplicationServer("ap-2---", 8010);
			this.cleanTestMediaDirectory(serverAp02000.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp02000.getContext(), this, false);
			testContainer.setParameterServer(serverAp02000);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(mediaServer2DataIdentifierFrom);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(mediaServer2DataIdentifierToo);
			testContainer.executeComponentTest();

			// Release application server
			if (serverAp02000 != null) this.releaseApplicationServer(serverAp02000);

			/*
			 * Fill third media server
			 */

			ServerManager serverAp00300 = this.createApplicationServer("ap--3--", 8010);
			this.cleanTestMediaDirectory(serverAp00300.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp00300.getContext(), this, false);
			testContainer.setParameterServer(serverAp00300);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(mediaServer3DataIdentifierFrom);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(mediaServer3DataIdentifierToo);
			testContainer.executeComponentTest();

			// Release application server
			if (serverAp00300 != null) this.releaseApplicationServer(serverAp00300);

			/*
			 * Fill fourth media server
			 */

			ServerManager serverAp00040 = this.createApplicationServer("ap---4-", 8010);
			this.cleanTestMediaDirectory(serverAp00040.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp00040.getContext(), this, false);
			testContainer.setParameterServer(serverAp00040);
			testContainer.setParameterPlainResourceGroup("Factory");
			testContainer.setParameterPlainResourceName("Doorway");
			testContainer.setParameterPlainDataIdentifierTestUploadStartFrom(mediaServer4DataIdentifierFrom);
			testContainer.setParameterPlainNumberOfMediaToBeUploaded(mediaServer4DataIdentifierToo);
			testContainer.executeComponentTest();

			// Release application server
			if (serverAp00040 != null) this.releaseApplicationServer(serverAp00040);

			/*
			 * Connect to media pool with all four media servers and try to read
			 * all media files. All media files should be available in the media
			 * pool
			 */

			ServerManager serverAp12340 = this.createApplicationServer("ap1234-", 8010);
			this.cleanTestMediaDirectory(serverAp12340.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp12340.getContext(), this, false);
			testContainer.setParameterServer(serverAp12340);
			testContainer.functionReadFileFromMediaPool("Factory", "Doorway", mediaServer1DataIdentifierFrom, mediaServer4DataIdentifierToo);

			// Release application server
			if (serverAp12340 != null) this.releaseApplicationServer(serverAp12340);

			/*
			 * Connect to a media pool with one more media server that doesn't
			 * hold any media files because it is new. The new media server is
			 * set as the main media server, that means, it is asked primarily.
			 * However, all media files should be available in the media pool.
			 * But because of the media files are never found on the main media
			 * server, the synchronizing mechanism is activated, and all media
			 * files are synchronized to the new media server.
			 */

			ServerManager serverAp12345 = this.createApplicationServer("ap12345", 8010);
			this.cleanTestMediaDirectory(serverAp12345.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp12345.getContext(), this, false);
			testContainer.setParameterServer(serverAp12345);
			testContainer.functionReadFileFromMediaPool("Factory", "Doorway", mediaServer1DataIdentifierFrom, mediaServer4DataIdentifierToo);

			// Release application server
			if (serverAp12345 != null) this.releaseApplicationServer(serverAp12345);

			/*
			 * After synchronizing all media files to all media server they
			 * should be available on each single server.
			 */

			/*
			 * Read all media from the first media server
			 */

			serverAp10000 = this.createApplicationServer("ap1----", 8010);
			this.cleanTestMediaDirectory(serverAp10000.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp10000.getContext(), this, false);
			testContainer.setParameterServer(serverAp10000);
			testContainer.functionReadFileFromMediaPool("Factory", "Doorway", mediaServer1DataIdentifierFrom, mediaServer4DataIdentifierToo);

			// Release application server
			if (serverAp10000 != null) this.releaseApplicationServer(serverAp10000);

			/*
			 * Read all media from the second media server
			 */

			serverAp02000 = this.createApplicationServer("ap-2---", 8010);
			this.cleanTestMediaDirectory(serverAp02000.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp02000.getContext(), this, false);
			testContainer.setParameterServer(serverAp02000);
			testContainer.functionReadFileFromMediaPool("Factory", "Doorway", mediaServer1DataIdentifierFrom, mediaServer4DataIdentifierToo);

			// Release application server
			if (serverAp02000 != null) this.releaseApplicationServer(serverAp02000);

			/*
			 * Read all media from the third media server
			 */

			serverAp00300 = this.createApplicationServer("ap--3--", 8010);
			this.cleanTestMediaDirectory(serverAp00300.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp00300.getContext(), this, false);
			testContainer.setParameterServer(serverAp00300);
			testContainer.functionReadFileFromMediaPool("Factory", "Doorway", mediaServer1DataIdentifierFrom, mediaServer4DataIdentifierToo);

			// Release application server
			if (serverAp00300 != null) this.releaseApplicationServer(serverAp00300);

			/*
			 * Read all media from the fourth media server
			 */

			serverAp00040 = this.createApplicationServer("ap---4-", 8010);
			this.cleanTestMediaDirectory(serverAp00040.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp00040.getContext(), this, false);
			testContainer.setParameterServer(serverAp00040);
			testContainer.functionReadFileFromMediaPool("Factory", "Doorway", mediaServer1DataIdentifierFrom, mediaServer4DataIdentifierToo);

			// Release application server
			if (serverAp00040 != null) this.releaseApplicationServer(serverAp00040);

			/*
			 * Read all media from the fifth media server
			 */

			ServerManager serverAp00005 = this.createApplicationServer("ap----5", 8010);
			this.cleanTestMediaDirectory(serverAp00005.getContext());

			// Run test on test container
			testContainer = new TestContainerMediaPool(serverAp00005.getContext(), this, false);
			testContainer.setParameterServer(serverAp00005);
			testContainer.functionReadFileFromMediaPool("Factory", "Doorway", mediaServer1DataIdentifierFrom, mediaServer4DataIdentifierToo);

			// Release application server
			if (serverAp00005 != null) this.releaseApplicationServer(serverAp00005);

			/*
			 * Release all media servers
			 */
			if (serverMs1 != null) this.releaseApplicationServer(serverMs1);
			if (serverMs2 != null) this.releaseApplicationServer(serverMs2);
			if (serverMs3 != null) this.releaseApplicationServer(serverMs3);
			if (serverMs4 != null) this.releaseApplicationServer(serverMs4);
			if (serverMs5 != null) this.releaseApplicationServer(serverMs5);
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
		try
		{
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
		try
		{
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
		try
		{
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
