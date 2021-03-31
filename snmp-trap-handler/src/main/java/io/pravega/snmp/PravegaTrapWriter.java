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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;

import com.google.gson.Gson;

import io.pravega.client.ClientConfig;
import io.pravega.client.EventStreamClientFactory;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ScalingPolicy;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.UTF8StringSerializer;
import io.pravega.snmp.objects.TrapEvent;
import io.pravega.snmp.transformers.TrapEventTransformer;

@ApplicationScoped
public class PravegaTrapWriter implements CommandResponder {

	private static final Logger log = LoggerFactory.getLogger(PravegaTrapWriter.class);

	@ConfigProperty(name = "pravega.controller-uri", defaultValue = "tcp://localhost:9090")
	String controllerUri;

	@ConfigProperty(name = "pravega.create-scope", defaultValue = "true")
	boolean createScope;

	@ConfigProperty(name = "pravega.scope-name", defaultValue = "alerts")
	String scopeName;

	@ConfigProperty(name = "pravega.create-stream", defaultValue = "true")
	boolean createStream;

	@ConfigProperty(name = "pravega.stream-name", defaultValue = "snmp")
	String streamName;

	@Inject
	TrapEventTransformer transformer;

	Gson gson = new Gson();

	private ClientConfig clientConfig;
	private EventStreamClientFactory factory;
	private EventWriterConfig writerConfig;
	private EventStreamWriter<String> writer;

	@PostConstruct
	public void init() throws IOException {
		log.info("Starting up PravegaTrapWriter...");
		URI controllerURI = URI.create(controllerUri);
		StreamConfiguration streamConfig = StreamConfiguration.builder()
				.scalingPolicy(ScalingPolicy.fixed(16))
				.build();
		if (createScope || createStream)
			try (StreamManager streamManager = StreamManager.create(controllerURI)) {
				if (createScope)
					streamManager.createScope(scopeName);
				if (createStream)
					streamManager.createStream(scopeName, streamName, streamConfig);
			}
		clientConfig = ClientConfig.builder()
				.controllerURI(controllerURI)
				.build();
		factory = EventStreamClientFactory.withScope(scopeName, clientConfig);
		writerConfig = EventWriterConfig.builder().build();
		writer = factory.createEventWriter(streamName, new UTF8StringSerializer(), writerConfig);
	}

	@PreDestroy
	void shutdown() throws IOException {
		log.info("Shutting down PravegaTrapWriter...");
		close();
		log.info("PravegaTrapWriter shut down.");
	}

	public void close() throws IOException {
		writer.close();
		factory.close();
	}

	@Override
	public synchronized void processPdu(CommandResponderEvent event) {
		TrapEvent trapEvent = transformer.apply(event);
		String json = gson.toJson(trapEvent);
		writer.writeEvent(trapEvent.peerAddress, json);
		event.setProcessed(true);
		log.info(json);
	}

	public String getControllerUri() {
		return controllerUri;
	}

	public boolean isCreateScope() {
		return createScope;
	}

	public String getScopeName() {
		return scopeName;
	}

	public boolean isCreateStream() {
		return createStream;
	}

	public String getStreamName() {
		return streamName;
	}

}
