package fmagic.basic.application;

import fmagic.basic.context.Context;

public interface DocumentationInterface
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
	 * LocaldataManager, CommandManager or others.
	 * 
	 * @param context
	 *            The context to use.
	 * 
	 * @return Returns a string containing the generated text.
	 */
	public String printManual(Context context);
}
