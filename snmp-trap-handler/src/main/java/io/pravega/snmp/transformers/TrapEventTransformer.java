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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.snmp4j.CommandResponderEvent;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TransportIpAddress;

import io.pravega.snmp.objects.TrapEvent;

@ApplicationScoped
public class TrapEventTransformer implements Function<CommandResponderEvent, TrapEvent> {

	@Inject
	TrapTransformer trapTransformer;

	@Override
	public TrapEvent apply(CommandResponderEvent input) {
		TrapEvent output = new TrapEvent();
		output.peerAddress = ((TransportIpAddress) input.getPeerAddress()).getInetAddress().getHostAddress();
		output.peerPort = ((TransportIpAddress) input.getPeerAddress()).getPort();
		output.securityLevel = input.getSecurityLevel();
		output.securityModel = input.getSecurityModel();
		output.securityName = new OctetString(input.getSecurityName()).toString();
		output.trap = trapTransformer.apply(input.getPDU());
		output.messageProcessingModel = input.getMessageProcessingModel();
		return output;
	}

}
