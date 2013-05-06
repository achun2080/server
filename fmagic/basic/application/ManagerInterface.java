package fmagic.basic.application;

import fmagic.basic.context.Context;

public interface ManagerInterface extends DocumentationInterface
{
	/**
	 * Check on integrity errors of resource identifiers.
	 * <p>
	 * This method is invoked once after all resource files were read by the
	 * initializing process.
	 * <p>
	 * Please check those resource identifiers only, you are responsible for, e.
	 * g. the notification manager should check <TT>Notification.*.*.*.*.*</TT>
	 * identifiers only.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean validateResources(Context context);

	/**
	 * Read configuration parameters and check them.
	 * <p>
	 * This method is invoked once after all resource files were read by the
	 * initializing process.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean readConfiguration(Context context);

	/**
	 * Clean environment.
	 * <p>
	 * This method is invoked periodically by the system services.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns <TT>true</TT> if an error was found, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean cleanEnvironment(Context context);
}
