package org.makumba.devel.eclipse.mdd.ui.codegenerator;

import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.makumba.devel.eclipse.mdd.MDDUtils;
import org.makumba.devel.eclipse.mdd.MDD.DataDefinition;
import org.makumba.devel.eclipse.mdd.MDD.Declaration;
import org.makumba.devel.eclipse.mdd.MDD.FieldDeclaration;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class MakumbaJSPCodeGenerator {

	@Inject
	public IQualifiedNameProvider nameProvider;

	public static String[] GeneratorTypes = { "New", "Edit", "List", "View" };

	public String getNewFormCode(DataDefinition dd) {
		String fullName = nameProvider.getQualifiedName(dd);
		String ddShortType = fullName.contains(".") ? fullName.substring(fullName.lastIndexOf("."), fullName.length())
				: fullName;
		String result = "<h1>New " + ddShortType + "</h1>\n";
		result += "<mak:newForm type=\"" + fullName + "\" action=\"" + ddShortType.toLowerCase() + "View.jsp?"
				+ ddShortType.toLowerCase() + "=${" + ddShortType.toLowerCase() + "Pointer}\" name=\""
				+ ddShortType.toLowerCase() + "\" method=\"post\">\n";
		result += " <table>\n";
		for (Declaration d : Iterables.filter(dd.getD(), MDDUtils.NonSetComplex)) {
			FieldDeclaration fd = (FieldDeclaration) d;
			result += getInputCode(fd);
		}
		result += "  <tr>\n";
		result += "   <td>  <input type=\"submit\" value=\"Add\" accessKey=\"A\">  <input type=\"reset\" accessKey=\"R\">  <input type=\"reset\" value=\"Cancel\" accessKey=\"C\" onClick=\"javascript:back();\">  </td>\n";
		result += "  </tr>\n";
		result += " </table>\n";
		result += "</mak:newForm>\n";
		return result;

	}

	public String getViewCode(DataDefinition dd) {
		String object = nameProvider.getQualifiedName(dd);
		String name = object.contains(".") ? object.substring(object.lastIndexOf("."), object.length()) : object;
		String result = "";
		String label = getDataTypeLabel(object);

		result += "<mak:object from=\"" + object + " " + label + "\" where=\"" + label + "=$" + name.toLowerCase()
				+ "\">\n";
		result += " <mak:value expr=\"" + label + "\" printVar=\"" + name.toLowerCase() + "Pointer\" />\n";

		result += " <h1>View " + name + "</h1>\n";
		result += " <table>\n";
		for (Declaration d : Iterables.filter(dd.getD(), MDDUtils.NonSetComplex)) {
			FieldDeclaration fd = (FieldDeclaration) d;
			result += "  <tr>\n";
			result += "   <th>" + getFieldLabel(fd) + "</th>\n";
			result += "   <td><mak:value expr=\"" + label + "." + fd.getName() + "\"/></td>\n";
			result += "  </tr>\n";
		}
		result += " </table>\n";
		for (Declaration d : Iterables.filter(dd.getD(), MDDUtils.SetComplex)) {
			FieldDeclaration fd = (FieldDeclaration) d;
			result += getListCode(MDDUtils.getChildrenOf(fd), label + "." + fd.getName(), false);
		}
		result += "</mak:object>\n";
		return result;
	}

	public String getEditFormCode(DataDefinition dd) {
		String object = nameProvider.getQualifiedName(dd);
		String name = object.contains(".") ? object.substring(object.lastIndexOf("."), object.length()) : object;
		String label = getDataTypeLabel(object);
		String result = "";

		result += "<mak:object from=\"" + object + " " + label + "\" where=\"" + label + "=$" + name.toLowerCase()
				+ "\">\n";
		result += " <mak:value expr=\"" + label + "\" printVar=\"" + name.toLowerCase() + "Pointer\" />\n";

		result += " <h1>Edit " + name + "</h1>\n";

		String action = name.toLowerCase() + "View.jsp?" + name.toLowerCase() + "=${" + name.toLowerCase() + "Pointer}";
		result += getEditFormCode(dd.getD(), label, action);
		for (Declaration d : Iterables.filter(dd.getD(), MDDUtils.SetComplex)) {
			FieldDeclaration fd = (FieldDeclaration) d;
			String subLabel = getDataTypeLabel(fd.getName());
			result += " <h2>" + fd.getName() + "</h2>\n";
			result += " <mak:list from=\"" + label + "." + fd.getName() + " " + subLabel + "\" >\n";
			result += getEditFormCode(MDDUtils.getChildrenOf(fd), subLabel, action);
			result += " </mak:list>\n";
			result += "\n";
			result += getAddFormCode(MDDUtils.getChildrenOf(fd), label, fd.getName(), action);
		}
		result += "</mak:object>\n";
		return result;
	}

	public String getEditFormCode(Iterable<Declaration> declarations, String object, String action) {
		String result = "";
		result += "<mak:editForm object=\"" + object + "\" action=\"" + action + "\" method=\"post\">\n";
		result += " <table>\n";
		for (Declaration d : Iterables.filter(declarations, MDDUtils.NonSetComplex)) {
			FieldDeclaration fd = (FieldDeclaration) d;
			result += getInputCode(fd);
		}
		result += "  <tr>\n";
		result += "   <td>  <input type=\"submit\" value=\"Save changes\" accessKey=\"A\">  <input type=\"reset\" accessKey=\"R\">  <input type=\"reset\" value=\"Cancel\" accessKey=\"C\" onClick=\"javascript:back();\">  </td>\n";
		result += "  </tr>\n";
		result += " </table>\n";
		result += "</mak:editForm>\n";
		return result;
	}

	public String getListCode(DataDefinition dd) {
		String fullName = nameProvider.getQualifiedName(dd);
		return getListCode(dd.getD(), fullName, true);
	}

	private String getListCode(Iterable<Declaration> declarations, String object, boolean showActions) {
		String name = object.contains(".") ? object.substring(object.lastIndexOf("."), object.length()) : object;
		String label = getDataTypeLabel(object);
		String result = "<h1>List " + name + "s</h1>\n";
		result += " <table>\n";
		result += "  <tr>\n";
		String resultList = "  <mak:list from=\"" + object + " " + label + "\">\n";
		resultList += "   <tr>\n";
		for (Declaration d : Iterables.filter(declarations, MDDUtils.NonPointerOrSetFiledFilter)) {
			FieldDeclaration fd = (FieldDeclaration) d;
			result += "   <th>" + getFieldLabel(fd) + "</th>\n";
			resultList += "    <td><mak:value expr=\"" + label + "." + fd.getName() + "\"/></td>\n";
		}
		if (showActions) {
			resultList += "    <td><a href=\"" + name.toLowerCase() + "Edit.jsp?" + name.toLowerCase()
					+ "=<mak:value expr=\"" + label + "\" />\">[Edit]</a></td>\n";
			result += "   <th>Action</th>\n";
		}
		resultList += "   </tr>\n";
		resultList += "  </mak:list>\n";
		result += "  </tr>\n";
		result += resultList;
		result += " </table>\n";
		return result;
	}

	public String getAddFormCode(Iterable<Declaration> declarations, String object, String field, String action) {
		String result = "";
		result += "<mak:addForm object=\"" + object + "\" field=\"" + field + "\" action=\"" + action
				+ "\" method=\"post\">\n";
		result += " <table>\n";
		for (Declaration d : Iterables.filter(declarations, MDDUtils.NonSetComplex)) {
			FieldDeclaration fd = (FieldDeclaration) d;
			result += getInputCode(fd);
		}
		result += "  <tr>\n";
		result += "   <td>  <input type=\"submit\" value=\"Add\" accessKey=\"A\">  <input type=\"reset\" accessKey=\"R\">  <input type=\"reset\" value=\"Cancel\" accessKey=\"C\" onClick=\"javascript:back();\">  </td>\n";
		result += "  </tr>\n";
		result += " </table>\n";
		result += "</mak:addForm>\n";
		return result;
	}

	public String getInputCode(FieldDeclaration fd) {
		String result = "";
		result += "  <tr>\n";
		result += "   <th><label for=\"" + fd.getName() + "\">" + getFieldLabel(fd) + "</label></th>\n";
		result += "   <td><mak:input field=\"" + fd.getName() + "\" styleId=\"" + fd.getName() + "\" /></td>\n";
		result += "  </tr>\n";
		return result;
	}

	public String getFieldLabel(FieldDeclaration fd) {
		return (fd.getFieldComment() == null || fd.getFieldComment().equals("") ? fd.getName() : fd.getFieldComment());
	}

	public String getDataTypeLabel(String fullName) {
		String[] fragments = fullName.split("\\.");
		String result = "";
		for (String s : fragments) {
			if (s.length() > 0) {
				result += s.substring(0, 1);
			}
		}
		return result.toLowerCase();
	}

	public String getGeneratedCode(DataDefinition dd, String type) {
		if (type != null) {
			if (type.equals("New")) {
				return getNewFormCode(dd);
			}
			if (type.equals("Edit")) {
				return getEditFormCode(dd);
			}
			if (type.equals("List")) {
				return getListCode(dd);
			}
			if (type.equals("View")) {
				return getViewCode(dd);
			}
		}
		return "";
	}

}
