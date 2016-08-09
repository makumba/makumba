package org.makumba.devel.eclipse.mdd;

import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.AbstractNullSafeConverter;
import org.eclipse.xtext.conversion.impl.AbstractToStringConverter;
import org.eclipse.xtext.parsetree.AbstractNode;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;

public class MDDValueConverter extends DefaultTerminalConverters {

	@ValueConverter(rule = "SIGNED_INT")
	public IValueConverter<Integer> signed_int() {
		return new AbstractToStringConverter<Integer>() {
			@Override
			public Integer internalToValue(String string, AbstractNode node) {
				return Integer.valueOf(string);
			}
		};
	}
	
	@Override
	public IValueConverter<String> STRING() {
		// TODO Auto-generated method stub
		return new AbstractNullSafeConverter<String>() {
			@Override
			protected String internalToValue(String string, AbstractNode node) {
				try {
					return string.substring(1, string.length() - 1);
				} catch(IllegalArgumentException e) {
					throw new ValueConverterException(e.getMessage(), node, e);
				}
			}

			@Override
			protected String internalToString(String value) {
				return '"' + value + '"';
			}
		};
	}
	
}
