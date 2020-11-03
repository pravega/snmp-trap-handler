package io.pravega.snmp.objects;

/**
 * A variable binding in an SNMP notification.
 */
public class VarBind {

	/**
	 * OID of variable.
	 */
	public String oid;

	/**
	 * Name of OID, if available.
	 */
	public String name;

	/**
	 * Value of variable, if not null.
	 */
	public String value;

	/**
	 * Name of named-value or OID, if value is an enumerated integer or an OID.
	 */
	public String namedValue;

}
