# snmp-trap-handler
Write SNMP traps to a Pravega stream.

Listens on all addresses on UDP port 162 for SNMP v1, v2c and v3 traps, and writes all received SNMP messages to a Pravega stream in JSON format.

Writes are ordered by the IP address of each SNMP agent sending in traps.

User-based Security Model (USM) not yet supported.

## Configuration
Defaults can be overriden by creating a file <code>config/application.properties</code> in the current working directory of a deployed instance.

| Property | Description | Default |
| --- | --- | --- |
| <code>snmp.listen-address</code> | Protocol (udp or tcp), address and port to listen on for SNMP traps | <code>udp:0.0.0.0/162</code> |
| <code>snmp.thread-count</code> | Number of threads in worker pool for SNMP processing | <code>10</code> |
| <code>pravega.controller-uri</code> | URI of Pravega Controller | <code>tcp://localhost:9090</code> |
| <code>pravega.create-scope</code> | Whether or not to create the configured scope | <code>true</code> |
| <code>pravega.scope-name</code> | Name of the scope to use | <code>alerts</code> |
| <code>pravega.create-stream</code> | Whether or not to create the configured stream | <code>true</code> |
| <code>pravega.stream-name</code> | Name of the stream to write traps to | <code>snmp</code> |
