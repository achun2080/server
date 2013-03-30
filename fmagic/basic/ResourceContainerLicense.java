package fmagic.basic;

/**
 * This class extends resource items to specific license functionality.
 * 
 * @author frank.wuensche (FW)
 * 
 * @changed FW 30.03.2013 - Created
 * 
 */
public class ResourceContainerLicense extends ResourceContainer
{
	/**
	 * Constructor
	 */
	ResourceContainerLicense(ResourceContainer resourceContainer)
	{
		super(resourceContainer.getRecourceIdentifier());

		this.copy(resourceContainer);
	}

	/**
	 * Check if a single license is defined for the current user.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns <TT>true</TT> if the license is defined, otherwise
	 *         <TT>false</TT>.
	 */
	public boolean hasLicense(Context context)
	{
		String licenseKey = context.getServerSession().getUserLicenseKey();
		return context.getLicenseManager().checkLicense(context, licenseKey, this);
	}

	/**
	 * Get the main value "Value.0" from a license item.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the value read, or <TT>null</TT>, if no value is
	 *         available.
	 */
	public String getValue(Context context)
	{
		String licenseKey = context.getServerSession().getUserLicenseKey();
		return context.getLicenseManager().getValue(context, licenseKey, this, 0);
	}

	/**
	 * Get a numbered value "Value.x" from a license item.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the value read, or <TT>null</TT>, if no value is
	 *         available.
	 */
	public String getValue(Context context, int valueNumber)
	{
		String licenseKey = context.getServerSession().getUserLicenseKey();
		return context.getLicenseManager().getValue(context, licenseKey, this, valueNumber);
	}

	/**
	 * Get the main value "Value.0" from a license item as an integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the value read, or <TT>null</TT>, if no value is
	 *         available.
	 */
	public Integer getValueAsInteger(Context context)
	{
		String licenseKey = context.getServerSession().getUserLicenseKey();
		String value = context.getLicenseManager().getValue(context, licenseKey, this, 0);
		if (value == null) return null;

		try
		{
			int integerValue = Integer.parseInt(value);
			return integerValue;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get a numbered value "Value.x" from a license item as integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the value read, or <TT>null</TT>, if no value is
	 *         available.
	 */
	public Integer getValueAsInteger(Context context, int valueNumber)
	{
		String licenseKey = context.getServerSession().getUserLicenseKey();
		String value = context.getLicenseManager().getValue(context, licenseKey, this, valueNumber);
		if (value == null) return null;

		try
		{
			int integerValue = Integer.parseInt(value);
			return integerValue;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get the main value "Value.0" from a license item as an boolean value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the value read, or <TT>null</TT>, if no value is
	 *         available.
	 */
	public Boolean getValueAsBoolean(Context context)
	{
		String licenseKey = context.getServerSession().getUserLicenseKey();
		String value = context.getLicenseManager().getValue(context, licenseKey, this, 0);
		if (value == null) return null;

		if (value.equalsIgnoreCase("true")) return true;
		if (value.equalsIgnoreCase("false")) return false;
		return null;
	}

	/**
	 * Get a numbered value "Value.x" from a license item as boolean value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the value read, or <TT>null</TT>, if no value is
	 *         available.
	 */
	public Boolean getValueAsBoolean(Context context, int valueNumber)
	{
		String licenseKey = context.getServerSession().getUserLicenseKey();
		String value = context.getLicenseManager().getValue(context, licenseKey, this, valueNumber);
		if (value == null) return null;

		if (value.equalsIgnoreCase("true")) return true;
		if (value.equalsIgnoreCase("false")) return false;
		return null;
	}

	/**
	 * Compare the main value "Value.0" of a license item to an integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueToCompareWith
	 *            The integer value to compare with.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is greater than the value to compare with, or
	 *         <TT>false</TT> if the license value is lower or equal to the
	 *         value to compare with, or <TT>null</TT> if no license value is
	 *         available or the license value is not an integer value.
	 */
	public Boolean isGreaterThan(Context context, int valueToCompareWith)
	{
		Integer licenseValue = this.getValueAsInteger(context, 0);
		if (licenseValue == null) return null;

		if (licenseValue > valueToCompareWith) return true;
		return false;
	}

	/**
	 * Compare a numbered value "Value.x" of a license item to an integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueToCompareWith
	 *            The integer value to compare with.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is greater than the value to compare with, or
	 *         <TT>false</TT> if the license value is lower or equal to the
	 *         value to compare with, or <TT>null</TT> if no license value is
	 *         available or the license value is not an integer value.
	 */
	public Boolean isGreaterThan(Context context, int valueToCompareWith, int valueNumber)
	{
		Integer licenseValue = this.getValueAsInteger(context, valueNumber);
		if (licenseValue == null) return null;

		if (licenseValue > valueToCompareWith) return true;
		return false;
	}

	/**
	 * Compare the main value "Value.0" of a license item to an integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueToCompareWith
	 *            The integer value to compare with.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is lower than the value to compare with, or
	 *         <TT>false</TT> if the license value is greater or equal to the
	 *         value to compare with, or <TT>null</TT> if no license value is
	 *         available or the license value is not an integer value.
	 */
	public Boolean isLowerThan(Context context, int valueToCompareWith)
	{
		Integer licenseValue = this.getValueAsInteger(context, 0);
		if (licenseValue == null) return null;

		if (licenseValue < valueToCompareWith) return true;
		return false;
	}

	/**
	 * Compare a numbered value "Value.x" of a license item to an integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueToCompareWith
	 *            The integer value to compare with.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is lower than the value to compare with, or
	 *         <TT>false</TT> if the license value is greater or equal to the
	 *         value to compare with, or <TT>null</TT> if no license value is
	 *         available or the license value is not an integer value.
	 */
	public Boolean isLowerThan(Context context, int valueToCompareWith, int valueNumber)
	{
		Integer licenseValue = this.getValueAsInteger(context, valueNumber);
		if (licenseValue == null) return null;

		if (licenseValue < valueToCompareWith) return true;
		return false;
	}

	/**
	 * Compare the main value "Value.0" of a license item to an integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueToCompareWith
	 *            The integer value to compare with.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is equal to the value to compare with, or
	 *         <TT>false</TT> if the license value is not equal to the value to
	 *         compare with, or <TT>null</TT> if no license value is available
	 *         or the license value is not an integer value.
	 */
	public Boolean isEqual(Context context, int valueToCompareWith)
	{
		Integer licenseValue = this.getValueAsInteger(context, 0);
		if (licenseValue == null) return null;

		if (licenseValue == valueToCompareWith) return true;
		return false;
	}

	/**
	 * Compare a numbered value "Value.x" of a license item to an integer value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueToCompareWith
	 *            The integer value to compare with.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is equal to the value to compare with, or
	 *         <TT>false</TT> if the license value is not equal to the value to
	 *         compare with, or <TT>null</TT> if no license value is available
	 *         or the license value is not an integer value.
	 */
	public Boolean isEqual(Context context, int valueToCompareWith, int valueNumber)
	{
		Integer licenseValue = this.getValueAsInteger(context, valueNumber);
		if (licenseValue == null) return null;

		if (licenseValue == valueToCompareWith) return true;
		return false;
	}

	/**
	 * Compare the main value "Value.0" of a license item to a boolean value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is set to "true", or <TT>false</TT> if the license
	 *         value is set to "false", or <TT>null</TT> if no license value is
	 *         available or the license value is not a boolean value.
	 */
	public Boolean isTrue(Context context)
	{
		Boolean licenseValue = this.getValueAsBoolean(context, 0);
		if (licenseValue == null) return null;

		if (licenseValue == true) return true;
		return false;
	}

	/**
	 * Compare the numbered value "Value.x" of a license item to a boolean
	 * value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is set to "true", or <TT>false</TT> if the license
	 *         value is set to "false", or <TT>null</TT> if no license value is
	 *         available or the license value is not a boolean value.
	 */
	public Boolean isTrue(Context context, int valueNumber)
	{
		Boolean licenseValue = this.getValueAsBoolean(context, valueNumber);
		if (licenseValue == null) return null;

		if (licenseValue == true) return true;
		return false;
	}

	/**
	 * Compare the main value "Value.0" of a license item to a boolean value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is set to "false", or <TT>false</TT> if the license
	 *         value is set to "true", or <TT>null</TT> if no license value is
	 *         available or the license value is not a boolean value.
	 */
	public Boolean isFalse(Context context)
	{
		Boolean licenseValue = this.getValueAsBoolean(context, 0);
		if (licenseValue == null) return null;

		if (licenseValue == false) return true;
		return false;
	}

	/**
	 * Compare the numbered value "Value.x" of a license item to a boolean
	 * value.
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param valueNumber
	 *            The number of the "Value" attribute to be read.
	 * 
	 * @return Returns the result of comparing, that is <TT>true</TT> if the
	 *         license value is set to "false", or <TT>false</TT> if the license
	 *         value is set to "true", or <TT>null</TT> if no license value is
	 *         available or the license value is not a boolean value.
	 */
	public Boolean isFalse(Context context, int valueNumber)
	{
		Boolean licenseValue = this.getValueAsBoolean(context, valueNumber);
		if (licenseValue == null) return null;

		if (licenseValue == false) return true;
		return false;
	}

	/**
	 * Refills credits of an <TT>user</TT> license budget. The credit amount of
	 * the actual user is initialized to a new value. Existing credits were
	 * overridden.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to set.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be added, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean budgetRefillUserCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Refills credits of a <TT>site</TT> license budget. The credit amount of
	 * the actual site is initialized to a new value. Existing credits were
	 * overridden.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to set.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be added, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean budgetRefillSiteCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Consuming of credits of an <TT>user</TT> license budget. The credit
	 * amount of the actual user is decreased.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to consume.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be consumed, or
	 *         <TT>false</TT> if the limit of available credits would be
	 *         exceeded by consuming, or if an error occurred.
	 */
	public boolean budgetConsumeUserCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Consuming of credits of a <TT>site</TT> license budget. The credit amount
	 * of the actual site is decreased.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to consume.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be consumed, or
	 *         <TT>false</TT> if the limit of available credits would be
	 *         exceeded by consuming, or if an error occurred.
	 */
	public boolean budgetConsumeSiteCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Cancels consumed credits of an <TT>user</TT> license budget. The credit
	 * amount of the actual user is increased again. Please use this function to
	 * explicitly cancel credits, that were consumed before, e. g. because an
	 * error occurred before.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be added to the budget.
	 * 
	 * @param cancelText
	 *            A comment describing the background of canceling, e. g. the
	 *            reason.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be added, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean budgetCancelUserCredits(Context context, int credits, String cancelText)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Cancels consumed credits of an <TT>site</TT> license budget. The credit
	 * amount of the actual site is increased again. Please use this function to
	 * explicitly cancel credits, that were consumed before, e. g. because an
	 * error occurred before.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be added to the budget.
	 * 
	 * @param cancelText
	 *            A comment describing the background of canceling, e. g. the
	 *            reason.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be added, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean budgetCancelSiteCredits(Context context, int credits, String cancelText)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Add credits to an <TT>user</TT> license budget. The credit amount of the
	 * actual user is increased.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be added to the budget.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be added, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean budgetAddUserCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Add credits to a <TT>site</TT> license budget. The credit amount of the
	 * actual site is increased.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be added to the budget.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be added, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean budgetAddSiteCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Checks if a specific amount of credits of the <TT>user</TT> license
	 * budget of the actual user is available. The budget itself is <u>not</u>
	 * attached by invoking this function.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be checked.
	 * 
	 * @return Returns <TT>true</TT> if the credits are available, or
	 *         <TT>false</TT> if the amount doesn't match the number of credits
	 *         currently available, or if an error occurred.
	 */
	public boolean budgetHasUserCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Checks if a specific amount of credits of a <TT>site</TT> license budget
	 * of the actual site is available. The budget itself is <u>not</u> attached
	 * by invoking this function.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be checked.
	 * 
	 * @return Returns <TT>true</TT> if the credits are available, or
	 *         <TT>false</TT> if the amount doesn't match the number of credits
	 *         currently available, or if an error occurred.
	 */
	public boolean budgetHasSiteCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Get the current amount of credits of an <TT>user</TT> license budget. You
	 * get the amount of credits of the actual user.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the number of credits that are available.
	 */
	public int budgetGetUserCredits(Context context)
	{
		// TODO

		// Return
		return 0;
	}

	/**
	 * Get the current amount of credits of a <TT>site</TT> license budget. You
	 * get the amount of credits of the actual site.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the number of credits that are available.
	 */
	public int budgetGetsiteCredits(Context context)
	{
		// TODO

		// Return
		return 0;
	}

	/**
	 * Book credits to an <TT>user</TT> license account. The account amount of
	 * the actual user is increased.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be booked to the account.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be booked, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean accountBookUserCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Book credits to a <TT>site</TT> license account. The account amount of
	 * the actual site is increased.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be booked to the account.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be booked, or
	 *         <TT>false</TT> if an error occurred.
	 */
	public boolean accountBookSiteCredits(Context context, int credits)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Cancels booked credits of an <TT>user</TT> license account. The credit
	 * amount of the actual user is decreased. Please use this function to
	 * explicitly cancel credits, that were booked before, e. g. because an
	 * error occurred before.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be subtracted from the account.
	 * 
	 * @param cancelText
	 *            A comment describing the background of canceling, e. g. the
	 *            reason.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be subtracted, or
	 *         <TT>false</TT> if an error occurred. If the actual amount of
	 *         credits is lower than the number of credits to be subtracted, the
	 *         amount is set to <TT>0</TT>.
	 */
	public boolean accountCancelUserCredits(Context context, int credits, String cancelText)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Cancels booked credits of a <TT>site</TT> license account. The credit
	 * amount of the actual site is decreased. Please use this function to
	 * explicitly cancel credits, that were booked before, e. g. because an
	 * error occurred before.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @param credits
	 *            The number of credits to be subtracted from the account.
	 * 
	 * @param cancelText
	 *            A comment describing the background of canceling, e. g. the
	 *            reason.
	 * 
	 * @return Returns <TT>true</TT> if the credits could be subtracted, or
	 *         <TT>false</TT> if an error occurred. If the actual amount of
	 *         credits is lower than the number of credits to be subtracted, the
	 *         amount is set to <TT>0</TT>.
	 */
	public boolean accountCancelSiteCredits(Context context, int credits, String cancelText)
	{
		// Validate parameters
		if (credits <= 0) return false;

		// TODO

		// Return
		return true;
	}

	/**
	 * Get the current amount of credits of an <TT>user</TT> license account.
	 * You get the amount of credits of the actual user.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the sum of credits that are booked.
	 */
	public int accountGetUserCredits(Context context)
	{
		// TODO

		// Return
		return 0;
	}

	/**
	 * Get the current amount of credits of a <TT>site</TT> license account. You
	 * get the amount of credits of the actual site.
	 * <p>
	 * Please notice:
	 * <TT>This function is not implemented yet. You may invoke it to integrate it in your business functions, but it doesn't work yet.</TT>
	 * 
	 * @param context
	 *            Application context.
	 * 
	 * @return Returns the sum of credits that are booked.
	 */
	public int accountGetSiteCredits(Context context)
	{
		// TODO

		// Return
		return 0;
	}
}
