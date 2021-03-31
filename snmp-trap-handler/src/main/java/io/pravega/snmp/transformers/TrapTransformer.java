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
package io.pravega.snmp.transformers;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.smi.OID;

import io.pravega.snmp.objects.Trap;

@ApplicationScoped
public class TrapTransformer implements Function<PDU, Trap> {

	@Inject
	VarBindTransformer varBindTransformer;

	@Override
	public Trap apply(PDU input) {
		Trap output = new Trap();

		output.type = PDU.getTypeString(input.getType());

		if (input.getType() != PDU.V1TRAP) {
			output.requestId = input.getRequestID().toLong();
			output.errorStatus = input.getErrorStatus();
			output.errorIndex = input.getErrorIndex();
		}

		if (input instanceof PDUv1) {
			PDUv1 v1 = (PDUv1) input;

			OID enterprise = v1.getEnterprise();
			if (enterprise != null)
				output.enterprise = v1.getEnterprise().toDottedString();

			output.agentAddress = v1.getAgentAddress().toString();
			output.genericTrap = v1.getGenericTrap();
			output.specificTrap = v1.getSpecificTrap();
			output.timestamp = v1.getTimestamp();
		} else if (input instanceof ScopedPDU) {
			ScopedPDU scoped = (ScopedPDU) input;
			output.contextEngineId = scoped.getContextEngineID().toString();
			output.contextName = scoped.getContextName().toString();
		}

		output.variableBindings = input.getVariableBindings().stream()
				.map(varBindTransformer::apply)
				.collect(Collectors.toList());

		return output;
	}

}
