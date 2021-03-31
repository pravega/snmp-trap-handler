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

/**
 * A variable binding in an SNMP notification.
 */
public class VarBind {

	/**
	 * OID of variable.
	 */
	public String oid;

	/**
	 * Name of OID, if available.
	 */
	public String name;

	/**
	 * Value of variable, if not null.
	 */
	public String value;

	/**
	 * Name of named-value or OID, if value is an enumerated integer or an OID.
	 */
	public String namedValue;

}
