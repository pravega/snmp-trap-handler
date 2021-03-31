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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import io.quarkus.runtime.StartupEvent;

/**
 * Listens for SNMPv1, SMTPv2c and SMTPv3 traps, by default on UDP port 162
 * on all addresses, and writes all received SNMP messages to Pravega in
 * JSON format.
 * <p>
 * snmp4j doesn't provide whole message bytes, and only PDU BER byte stream
 * can be reconstructed, therefore JSON transformation is convenient here.
 * <p>
 * User-based Security Model (USM) not yet supported.
 */
@ApplicationScoped
public class SnmpListener {

	private static final Logger log = LoggerFactory.getLogger(SnmpListener.class);

	@ConfigProperty(name = "snmp.listen-address", defaultValue = "udp:0.0.0.0/162")
	String snmpListenAddress;

	@ConfigProperty(name = "snmp.thread-count", defaultValue = "10")
	int snmpThreadCount;

	PravegaTrapWriter pravegaTrapWriter;

	@Inject
	public SnmpListener(PravegaTrapWriter pravegaTrapWriter) {
		this.pravegaTrapWriter = pravegaTrapWriter;
		// dummy call to injected client proxy to trigger lazy initialization
		pravegaTrapWriter.getControllerUri();
	}

	void startup(@Observes StartupEvent se) {
		log.info("================================================================================");
		log.info("Starting up SnmpListener...");
		log.info("snmp.listen-address = {}", snmpListenAddress);
		log.info("snmp.thread-count = {}", snmpThreadCount);
		log.info("pravega.controller-uri = {}", pravegaTrapWriter.getControllerUri());
		log.info("pravega.create-scope = {}", pravegaTrapWriter.isCreateScope());
		log.info("pravega.scope-name = {}", pravegaTrapWriter.getScopeName());
		log.info("pravega.create-stream = {}", pravegaTrapWriter.isCreateStream());
		log.info("pravega.stream-name = {}", pravegaTrapWriter.getStreamName());
		log.info("================================================================================");
	}

	@PostConstruct
	public void init() throws IOException {
		initSnmp();
	}

	@PreDestroy
	void shutdown() throws IOException {
		log.info("Shutting down SnmpListener...");
		close();
		log.info("SnmpListener shut down.");
	}

	private Snmp snmp;
	private Address listenAddress;
	private MultiThreadedMessageDispatcher dispatcher;
	private ThreadPool threadPool;

	private void initSnmp() throws IOException {
		listenAddress = GenericAddress.parse(snmpListenAddress);
		TransportMapping<?> transport;
		if (listenAddress instanceof UdpAddress) {
			transport = new DefaultUdpTransportMapping(
					(UdpAddress) listenAddress, true);
		} else {
			transport = new DefaultTcpTransportMapping(
					(TcpAddress) listenAddress);
		}
		threadPool = ThreadPool.create("SnmpTrapHandler", snmpThreadCount);
		dispatcher = new MultiThreadedMessageDispatcher(threadPool,
				new MessageDispatcherImpl());
		dispatcher.addMessageProcessingModel(new MPv1());
		dispatcher.addMessageProcessingModel(new MPv2c());
		dispatcher.addMessageProcessingModel(new MPv3());
		snmp = new Snmp(dispatcher, transport);
		snmp.addCommandResponder(pravegaTrapWriter);
		snmp.listen();
//		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
//		SecurityModels.getInstance().addSecurityModel(usm);
//		snmp.getUSM().addUser(new OctetString("MD5DES"),
//				new UsmUser(new OctetString("MD5DES"),
//				AuthMD5.ID,
//				new OctetString("MD5DESUserAuthPassword"),
//				PrivDES.ID,
//				new OctetString("MD5DESUserPrivPassword")));
	}

	public void close() throws IOException {
		snmp.close();
	}

}
