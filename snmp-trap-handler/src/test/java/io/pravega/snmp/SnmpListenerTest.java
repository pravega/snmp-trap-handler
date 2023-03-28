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
package io.pravega.snmp;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.google.gson.Gson;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import io.pravega.snmp.objects.Trap;
import io.pravega.snmp.objects.TrapEvent;
import io.pravega.snmp.transformers.TrapTransformer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(PravegaResource.class)
public class SnmpListenerTest {

	@Inject
	TrapTransformer trapTransformer;

	static EventStreamClientFactory factory;
	static EventStreamReader<String> reader;
	static URI controllerURI;

	@BeforeAll
	public static void createReaderGroupAndReader() throws Exception {
		controllerURI = URI.create(ConfigProvider.getConfig().getValue("pravega.controller-uri", String.class));
		ClientConfig clientConfig = ClientConfig.builder()
				.controllerURI(controllerURI)
				.build();

		try (ReaderGroupManager rgMgr = ReaderGroupManager.withScope("alerts", clientConfig)) {
			ReaderGroupConfig rgConfig = ReaderGroupConfig.builder()
					.stream("alerts/snmp")
					.disableAutomaticCheckpoints()
					.build();
			rgMgr.createReaderGroup("trapReader", rgConfig);
		}

		factory = EventStreamClientFactory.withScope("alerts", clientConfig);
		reader = factory.createReader(UUID.randomUUID().toString(), "trapReader",
				new UTF8StringSerializer(), ReaderConfig.builder().build());
	}

	@AfterAll
	public static void shutdown() throws Exception {
		reader.close();
		factory.close();
	}

	@Test
	public void canListenForV1Trap() throws IOException, InterruptedException {
		UdpAddress targetAddress = (UdpAddress) GenericAddress.parse("udp:127.0.0.1/162");

		// Create Target
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setVersion(SnmpConstants.version1);
		target.setAddress(targetAddress);
		target.setTimeout(5000);
		target.setRetries(2);

		// Create V1 Trap
		PDUv1 pdu = new PDUv1();
		pdu.setType(PDU.V1TRAP);
		pdu.setEnterprise(new OID(".1.3.6.1.2.1.1.8"));
		pdu.setGenericTrap(PDUv1.ENTERPRISE_SPECIFIC);
		pdu.setSpecificTrap(1);
		pdu.setAgentAddress(new IpAddress("127.0.0.1"));

		// Send the PDU
		TransportMapping<UdpAddress> transport2 = new DefaultUdpTransportMapping();
		transport2.listen();
		Snmp snmp2 = new Snmp(transport2);

		// send the PDU
		snmp2.send(pdu, target);
		snmp2.close();
		transport2.close();

		String receivedTrapJson = reader.readNextEvent(10000).getEvent();

		Gson gson = new Gson();
		TrapEvent trapEvent = gson.fromJson(receivedTrapJson, TrapEvent.class);
		Trap expectedTrap = trapTransformer.apply(pdu);
		Assert.assertEquals(gson.toJson(expectedTrap), gson.toJson(trapEvent.trap));
	}

	@Test
	public void canListenForV2Trap() throws IOException, InterruptedException {
		String ipAddress = "127.0.0.1";
		int port = 162;

		Snmp snmp = new Snmp(new DefaultUdpTransportMapping(new UdpAddress("127.0.0.1/169")));
		snmp.listen();
		PDU pdu = new PDU();
		pdu.setType(PDU.TRAP);
		pdu.add(new VariableBinding(SnmpConstants.sysUpTime));
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(".1.3.6.1.2.1.1.6")));
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(ipAddress)));
		pdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.6"), new OctetString("Major")));
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setVersion(SnmpConstants.version2c);
		target.setAddress(new UdpAddress(ipAddress + "/" + port));
		target.setRetries(2);
		target.setTimeout(5000);

		snmp.send(pdu, target);
		snmp.close();

		String receivedTrapJson = reader.readNextEvent(10000).getEvent();

		Gson gson = new Gson();
		TrapEvent trapEvent = gson.fromJson(receivedTrapJson, TrapEvent.class);
		Trap expectedTrap = trapTransformer.apply(pdu);
		Assert.assertEquals(gson.toJson(expectedTrap), gson.toJson(trapEvent.trap));
	}

}
