package fmagic.basic;

public interface ManagerInterface
{
	/**
	 * Print a template formatted as a properties file, containing all
	 * information of the actual resource setting regarding a specific resource
	 * type like Configuration, Notification, Persistency, CommandManager or
	 * others.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @param includingResourceIdentifiers
	 *            Please set this value to <TT>true</TT> if the resource
	 *            identifiers are to be printed, otherwise <TT>false</TT>.
	 * 
	 * @return Returns a string containing the generated text.
	 */
	public String printTemplate(Context context, boolean includingResourceIdentifiers);

	/**
	 * Print a flat list of identifiers of the actual resource setting regarding
	 * a specific resource type like Configuration, Notification, Persistency,
	 * CommandManager or others.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns a string containing the generated text.
	 */
	public String printIdentifierList(Context context);

	/**
	 * Print a Manual containing all information of the actual resource setting
	 * regarding a specific resource type like Configuration, Notification,
	 * Persistency, CommandManager or others.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns a string containing the generated text.
	 */
	public String printManual(Context context);

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
