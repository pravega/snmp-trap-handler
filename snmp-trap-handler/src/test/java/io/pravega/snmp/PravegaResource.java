package io.pravega.snmp;

import java.util.Collections;
import java.util.Map;

import io.pravega.local.LocalPravegaEmulator;
import io.pravega.test.common.TestUtils;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

/**
 * Runs a standalone Pravega cluster in-process.
 * <p>
 * <code>pravega.controller-uri</code> system property will contain the
 * Pravega Controller URI.
 */
public class PravegaResource implements QuarkusTestResourceLifecycleManager {

	static LocalPravegaEmulator localPravega;

	@Override
	public Map<String, String> start() {
        localPravega = LocalPravegaEmulator.builder()
                .controllerPort(TestUtils.getAvailableListenPort())
                .segmentStorePort(TestUtils.getAvailableListenPort())
                .zkPort(TestUtils.getAvailableListenPort())
                .enableRestServer(false)
                .enableAuth(false)
                .enableTls(false)
                .build();
        try {
			localPravega.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return Collections.singletonMap("pravega.controller-uri",
				localPravega.getInProcPravegaCluster().getControllerURI());
	}

	@Override
	public void stop() {
		try {
			localPravega.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getControllerUri() {
		return localPravega.getInProcPravegaCluster().getControllerURI();
	}

}
