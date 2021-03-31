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

import org.snmp4j.smi.VariableBinding;

import io.pravega.snmp.objects.VarBind;

@ApplicationScoped
public class VarBindTransformer implements Function<VariableBinding, VarBind> {

	@Override
	public VarBind apply(VariableBinding input) {
		VarBind output = new VarBind();

		output.oid = input.getOid().toDottedString();

		String name = input.getOid().toString();
		if (!name.equals(output.oid))
			output.name = name;

		if (input.getVariable().getSyntax() != 5) // ASN.1 NULL type tag
			output.value = input.toValueString();

		return output;
	}

}
