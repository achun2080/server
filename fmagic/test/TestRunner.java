package fmagic.test;

import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fmagic.application.seniorcitizen.server.ServerSeniorCitizen;
import fmagic.basic.Context;
import fmagic.server.ServerManager;

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
	// Test organization
	private final String testCaseName;
	private final String testSessionName;

	// Application server port numbers
	private final static Set<Integer> usedServerPorts = new HashSet<Integer>();

	/**
	 * Constructor
	 */
	public TestRunner(String testCaseName, String testSessionName)
	{
		this.testCaseName = testCaseName;
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
	protected void doSingleFunctionTest(Context context, ServerTestContainer testContainer, String methodName)
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
	abstract public void executeSingleFunctionTest(ServerTestContainer serverTestContainer, String methodName);

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
	 * The <TT>code name</TT> of the application is composed by the name of the
	 * test case, an underline character "_", and the postfix name set as a
	 * parameter to this function. For example: If the name of the test case is
	 * "mediatest" and the postfix is set to "ap1", the result code name is
	 * "mediatest_ap1".
	 * <p>
	 * The <TT>server port</TT> number the application is listen to, is set
	 * automatically, in order to ensure that all running application servers of
	 * a test suite use different port numbers. You can get the current port
	 * number, used by an application server, by invoking the getter method
	 * <TT>getServerSocketPort()</TT> of the server instance.
	 * 
	 * @param codeNamePostfix
	 *            The postfix of the designated code name of the application.
	 * 
	 * @return Returns the server instance, or <TT>null</TT> if an error
	 *         occurred.
	 */
	protected ServerManager createApplicationServer(String codeNamePostfix)
	{
		ServerManager server = null;

		try
		{
			// Compose code name
			String codeName = this.getTestCaseName() + "_" + codeNamePostfix;

			// Allocate port number
			int port = this.allocatePortNumber();

			// Create instance
			server = ServerSeniorCitizen.getTestInstance(codeName, port, 1000000, this.getTestCaseName(), this.getTestSessionName());

			// Start application server
			if (server != null) server.startApplication();
		}
		catch (Exception e)
		{
			// Be silent
		}

		// Return
		return server;
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
	 * Allocate a port number for an application server.
	 * <p>
	 * The range of port numbers that can be allocated starts with <TT>8000</TT>
	 * and ends with <TT>8999</TT>.
	 * 
	 * @return Returns the allocated port number, or <TT>0</TT> if an error
	 *         occurred.
	 */
	private synchronized int allocatePortNumber()
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
	private synchronized void deallocatePortNumber(int port)
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
	 * Getter
	 */
	public String getTestCaseName()
	{
		return testCaseName;
	}

	/**
	 * Getter
	 */
	public String getTestSessionName()
	{
		return testSessionName;
	}
}
