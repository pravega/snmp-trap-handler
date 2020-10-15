package io.pravega.snmp.objects;

import java.util.List;

/**
 * SNMP notification for v1 and v2c/v3 trap representations.
 */
public class Trap {

	/**
	 * One of: GET, SET, GETNEXT, GETBULK, INFORM, RESPONSE, REPORT, TRAP, V1TRAP.
	 * <p>
	 * Expected to be TRAP or V1TRAP.
	 */
	public String type;

	// v2c/v3 Trap PDU
	public Long requestId;
	public Integer errorStatus;
	public Integer errorIndex;

	// v1 Trap PDU
	public String enterprise;
	public String agentAddress;
	public Integer genericTrap;
	public Integer specificTrap;
	public Long timestamp;

	// Scoped PDU
	public String contextEngineId;
	public String contextName;

	public List<VarBind> variableBindings;

}
