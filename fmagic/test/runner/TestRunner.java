package fmagic.test.runner;

import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fmagic.application.reference.client.ClientReferenceApplication;
import fmagic.application.reference.server.ServerReferenceApplication;
import fmagic.basic.context.Context;
import fmagic.client.application.ClientManager;
import fmagic.server.application.ServerManager;
import fmagic.test.application.TestManager;
import fmagic.test.container.TestContainer;
import fmagic.test.suite.TestSuite;

/**
 * This abstract class implements the frame for testing functionality of the
 * system.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 11.04.2013 - Created
 * 
 */
abstract public class TestRunner
{
	// Organization
	private final String testRunnerName;
	private final String testSessionName;
	private final TestSuite testSuite;

	// Error protocol
	private final HashMap<String, String> assertionErrorProtocol = new HashMap<String, String>();
	private int assertionNumberOfErrors = 0;

	// Currently used application server port numbers
	private final static Set<Integer> usedServerPorts = new HashSet<Integer>();

	/**
	 * Constructor
	 * 
	 * @param testSuite
	 *            The test suite that holds this test runner, or <TT>null</TT>
	 *            if no test suite is available.
	 * 
	 * @param testRunnerName
	 *            The name of the test runner.
	 * 
	 * @param testSessionName
	 *            The name of the test session.
	 */
	public TestRunner(TestSuite testSuite, String testRunnerName,
			String testSessionName)
	{
		this.testSuite = testSuite;
		this.testRunnerName = testRunnerName;
		this.testSessionName = testSessionName;
	}

	/**
	 * Setup test environment.
	 */
	abstract public void setup();

	/**
	 * Cleanup test environment.
	 */
	abstract public void cleanup();

	/**
	 * Execute function tests on single function (method).
	 */

	/**
	 * Single Function Test
	 */
	protected void doSingleFunctionTest(Context context, TestContainer testContainer, String methodName)
	{
		// Validate parameter
		if (context == null) return;
		if (testContainer == null) return;
		if (methodName == null) return;

		// Set context to test container
		testContainer.setContext(context);

		// Invoke method via reflection
		try
		{
			// Get method by reflection and invoke it
			Method method = testContainer.getClass().getMethod(methodName, new Class[] {});
			method.invoke(testContainer, new Object[] {});
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Execute a single function test.
	 */
	abstract public void executeSingleFunctionTest(TestContainer serverTestContainer, String methodName);

	/**
	 * Execute component tests.
	 */
	abstract public void executeComponentTest();

	/**
	 * Execute concurrent tests.
	 */
	abstract public void executeConcurrentTest();

	/**
	 * Execute stress tests.
	 */
	abstract public void executeStressTest();

	/**
	 * Execute integration tests.
	 */
	abstract public void collectIntegrationTestContainer(List<Thread> threadList);

	/**
	 * Create an application server and start it.
	 * <p>
	 * The <TT>server port</TT> number the application is listen to, is set
	 * automatically, in order to ensure that all running application servers of
	 * a test suite use different port numbers. You can get the current port
	 * number, used by an application server, by invoking the getter method
	 * <TT>getServerSocketPort()</TT> of the server instance.
	 * 
	 * @param codeName
	 *            The code name of the application.
	 * 
	 * @return Returns the server instance, or <TT>null</TT> if an error
	 *         occurred.
	 */
	protected ServerManager createApplicationServer(String codeName)
	{
		ServerManager server = null;

		try
		{
			// Allocate port number
			int port = this.allocatePortNumber();

			// Create instance
			server = ServerReferenceApplication.getTestInstance(codeName, port, 1000000, this.getTestRunnerName(), this.getTestSessionName());

			// Error
			if (server == null)
			{
				String additionalText = "--> Test runner name: '" + this.getTestRunnerName() + "'";
				additionalText += "\n--> Test session name: '" + this.getTestSessionName() + "'";
				additionalText += "\n--> Code name of client: '" + codeName + "'";
				TestManager.addErrorToErrorProtocolLists(this, "Error on creating appplication server", additionalText);
				return null;
			}
			
			// Start application server

			// Start application server
			server.startApplication();
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return server;
	}

	/**
	 * Create a client application and start it.
	 * 
	 * @param codeName
	 *            The code name of the application.
	 * 
	 * @return Returns the client instance, or <TT>null</TT> if an error
	 *         occurred.
	 */
	protected ClientManager createApplicationClient(String codeName)
	{
		ClientManager client = null;

		try
		{
			// Create instance
			client = ClientReferenceApplication.getTestInstance(codeName, this.getTestRunnerName(), this.getTestSessionName());

			// Error
			if (client == null)
			{
				String additionalText = "--> Test runner name: '" + this.getTestRunnerName() + "'";
				additionalText += "\n--> Test session name: '" + this.getTestSessionName() + "'";
				additionalText += "\n--> Code name of client: '" + codeName + "'";
				TestManager.addErrorToErrorProtocolLists(this, "Error on creating client appplication", additionalText);
				return null;
			}
			
			// Start client application
			client.startApplication();
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return client;
	}

	/**
	 * Stop an application server and release it.
	 * 
	 * @param server
	 *            The application server to be considered.
	 */
	protected void releaseApplicationServer(ServerManager server)
	{
		try
		{
			if (server != null)
			{
				// Get allocated port of the server
				int port = server.getServerSocketPort();

				// Stop the server
				server.stopApplication();

				// Deallocate port number of the server
				this.deallocatePortNumber(port);
			}
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Stop a client application and release it.
	 * 
	 * @param client
	 *            The client application to be considered.
	 */
	protected void releaseApplicationClient(ClientManager client)
	{
		try
		{
			if (client != null)
			{
				client.stopApplication();
			}
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Getter
	 */
	public String getTestRunnerName()
	{
		return testRunnerName;
	}

	/**
	 * Getter
	 */
	public String getTestSessionName()
	{
		return testSessionName;
	}

	/**
	 * Getter
	 */
	public TestSuite getTestSuite()
	{
		return testSuite;
	}

	/**
	 * Getter
	 */
	public HashMap<String, String> getAssertionErrorProtocol()
	{
		return assertionErrorProtocol;
	}

	/**
	 * Getter
	 */
	public int getAssertionNumberOfErrors()
	{
		return assertionNumberOfErrors;
	}

	/**
	 * Setter
	 */
	public void increaseAssertionNumberOfErrors()
	{
		this.assertionNumberOfErrors++;
	}

	/**
	 * Allocate a port number for an application server.
	 * <p>
	 * The range of port numbers that can be allocated starts with <TT>8000</TT>
	 * and ends with <TT>8999</TT>.
	 * 
	 * @return Returns the allocated port number, or <TT>0</TT> if an error
	 *         occurred.
	 */
	public synchronized int allocatePortNumber()
	{
		try
		{
			for (int port = 8000; port <= 8999; port++)
			{
				if (TestRunner.usedServerPorts.contains(port)) continue;

				if (this.isSocketUsed(port)) continue;

				TestRunner.usedServerPorts.add(port);

				return port;
			}
		}
		catch (Exception e)
		{
			// Be silent
		}

		// No port available yet
		return 0;
	}

	/**
	 * Check if a socket is currently used.
	 * 
	 * @param port
	 *            The port to check.
	 * 
	 * @return Returns <TT>true</TT> if the socket is currently used, otherwise
	 *         <TT>false</TT>.
	 */
	private boolean isSocketUsed(int port)
	{
		boolean portTaken = false;

		ServerSocket socket = null;

		try
		{
			socket = new ServerSocket(port);
		}
		catch (Exception e)
		{
			portTaken = true;
		}
		finally
		{
			if (socket != null)
			{
				try
				{
					socket.close();
				}
				catch (Exception e)
				{
					// Be silent
				}
			}
		}

		return portTaken;
	}

	/**
	 * Deallocate a port number, used by an application server.
	 */
	public synchronized void deallocatePortNumber(int port)
	{
		try
		{
			TestRunner.usedServerPorts.remove(port);
		}
		catch (Exception e)
		{
			// Be silent
		}
	}

	/**
	 * Print the error protocol of a test runner.
	 * 
	 * @return Returns the content of the error protocol as string or
	 *         <TT>null</TT> if no errors were found.
	 */
	public String printAssertionErrorProtocol()
	{
		return TestManager.printAssertionErrorProtocol(this.assertionNumberOfErrors, this.assertionErrorProtocol);
	}
}
