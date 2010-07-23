/*
 * Created on Jul 23, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;

import com.sun.faces.facelets.compiler.UIInstructions;
import com.sun.faces.facelets.component.UIRepeat;

public class UIRepeatListComponent extends UIRepeat {

	public UIRepeatListComponent() {
		// example forcing a value on the UIRepeat
		setValue(new Object[] { "a", "b" });
	}

	public void analyze() {

		System.out.println(this.getClass());
		
		final Map<UIComponent, Map<String, String>> expressions = new HashMap<UIComponent, Map<String,String>>();
		
		// iterate over all the children and find the value expressions they declare
		this.visitTree(VisitContext.createVisitContext(getFacesContext()), new VisitCallback() {
			
			@Override
			public VisitResult visit(VisitContext context, UIComponent target) {
				
				// FIXME this is highly Mojarra-dependent and quite a hack
				if(target instanceof UIInstructions) {
					UIInstructions plain = (UIInstructions) target;
					String txt = plain.toString();
					// see if it has some EL
					int n = txt.indexOf("#{");
					if(n > -1) {
						txt = txt.substring(n + 2);
						int e = txt.indexOf("}");
						if(e > -1) {
							txt = txt.substring(0, e);
							Map<String, String> r = new HashMap<String, String>();
							r.put(txt, txt);
							expressions.put(target, r);
						}
					}
				} else {
					expressions.put(target, findExpressions(target));
				}

				return VisitResult.ACCEPT;
			}
		});
		
		for(UIComponent c : expressions.keySet()) {
			System.out.println("** Child component " + c.getClass());
			for(String p : expressions.get(c).keySet()) {
				System.out.println("  ** Property '" + p + "' with value expression '" + expressions.get(c).get(p) + "'");
			}
		}
		
		
		// check whether we have not computed the queries of this mak:list group
		// before
		// if so, retrieve them from cache

		// look for all children mak:lists and start making their queries
		// look for all children mak:values and for all children that contain #{
		// mak:expr(QL) }, add the expressions as projection to the enclosing
		// mak:list query

		// look for all children that contain #{ label.field } where label is
		// defined in a mak:list's FROM, add label and label.field to the
		// projections of that mak:list

		// cache the queries of this mak:list group.

		// execute the queries and prepare the DataModel
		// use setValue() to give the DataModel to the UIRepeat
	}

	/**
	 * Finds the properties of this {@link UIComponent} that have a
	 * {@link ValueExpression}.<br>
	 * 
	 * TODO: we can't really cache this since the programmer can change the view
	 * without restarting the whole servlet context. We may be able to find out
	 * about a change in the view though and introduce a caching mechanism then.

	 * @return a map of the property names and their expression
	 */
	protected Map<String, String> findExpressions(UIComponent component) {
		Map<String, String> result = new HashMap<String, String>();

		try {
			PropertyDescriptor[] pd = Introspector.getBeanInfo(component.getClass())
					.getPropertyDescriptors();
			for (PropertyDescriptor p : pd) {
				// we try to see if this is a ValueExpression by probing it
				ValueExpression ve = this.getValueExpression(p.getName());
				if (ve != null) {
					result.put(p.getName(), trimExpression(ve
							.getExpressionString()));
				}
			}

		} catch (IntrospectionException e) {
			// TODO better error handling
			e.printStackTrace();
		}
		return result;
	}

	private String trimExpression(String expr) {
		return expr.substring(2, expr.length() - 1);
	}
}
