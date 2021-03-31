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
