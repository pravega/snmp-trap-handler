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
