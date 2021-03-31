/* Copyright Pravega Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
