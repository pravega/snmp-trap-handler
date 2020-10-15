package io.pravega.snmp.objects;

/**
 * A received SNMP Trap event.
 */
public class TrapEvent {

	/**
	 * IANA managed and IETF standards-track Security Models, from 0 to 255
	 * inclusive.
	 * <p>
	 * 0 for any, 1 for SMTPv1, 2 for SMTPv2c, 3 for User-based Security
	 * Model (USM).
	 */
	public int securityModel;

	/**
	 * A Level of Security at which SNMP messages can be
	 * sent or with which operations are being processed;
	 * in particular, one of:
	 * <p><ul>
	 * <li>noAuthNoPriv - without authentication and without privacy,
	 * <li>authNoPriv   - with authentication but without privacy,
	 * <li>authPriv     - with authentication and with privacy.
	 * </ul><p>
	 * These three values are ordered such that
	 * noAuthNoPriv is less than authNoPriv and
	 * authNoPriv is less than authPriv.
	 * <p>
	 * 1 for noAuthNoPriv, 2 for authNoPriv, 3 for authPriv.
	 */
	public int securityLevel;

	/**
	 * Community name or the name representing the user on whose behalf the
	 * message was received.
	 */
	public String securityName;

	/**
	 * IP address of trap sender.
	 */
	public String peerAddress;

	/**
	 * TCP/UDP port used by trap sender.
	 */
	public int peerPort;

	/**
	 * The received SNMP Trap notification.
	 */
	public Trap trap;

	/**
	 * Version of the SNMP protocol message processor used.
	 * <p>
	 * 0 for SNMPv1, 1 for SNMPv2c, 2 for SNMPv2u and SNMPv2*, 3 for SNMPv3.
	 */
	public int messageProcessingModel;

}
