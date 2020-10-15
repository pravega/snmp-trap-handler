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
