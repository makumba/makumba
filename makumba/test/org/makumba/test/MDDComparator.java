package org.makumba.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.providers.datadefinition.mdd.MDDProvider;

/**
 * Compares MDDs provided by two separate MDD providers<br>
 * Rename, simplify, and convert to test for going thru the MDD corpus
 * 
 * @author manu
 * @version $Id: MDDComparator.java,v 1.1 Apr 28, 2010 9:39:39 AM manu Exp $
 */
public class MDDComparator {

    private static final String TEMP = "temp";

    public static void main(String... args) {

        // create a temporary directory that will be deleted on exit
        File tempDir = new File(TEMP);
        tempDir.mkdir();

        try {

            // extract the MDDs so the providers can access them in a normal fashion
            File f = new File(MDDComparator.class.getResource("mdd-corpus.zip").getPath());
            ZipFile zf = new ZipFile(f);
            extractMDDsFile(zf, tempDir);

            // we go through the corpus, one application at a time
            // for this we first extract all files, then consider only a given application sub-set

            String[] apps = tempDir.list();
            for (int i = 0; i < apps.length; i++) {
                System.out.println("== Reading corpus MDDs of application " + apps[i]);
                File app = new File(TEMP + File.separator + apps[i]);
                if (!app.isDirectory()) {
                    continue;
                }

                // RecordInfo.setWebappRoot(app.getPath());
                MDDProvider.setWebappRoot(app.getPath());

                Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry ze = entries.nextElement();
                    if (!ze.getName().endsWith(".mdd")) {
                        continue;
                    }
                    if (!ze.getName().startsWith(apps[i])) {
                        continue;
                    }

                    String type = ze.getName().substring((apps[i] + "/WEB-INF/classes/dataDefinitions/").length(),
                        ze.getName().lastIndexOf(".")).replaceAll("/", ".");

                    // System.out.println("==== Reading MDD " + type);
                    // DataDefinition dd1 = RecordInfo.getRecordInfo(type);
                    DataDefinition dd1 = null;
                    DataDefinition dd2 = null;
                    try {
                        dd1 = MDDProvider.getMDD(type);
                    } catch (Throwable t) {
                        System.err.println("MDDProvider error on " + type + " : " + t.getMessage());
                    }
                    try {
                        // dd2 = RecordInfo.getRecordInfo(type);
                    } catch (Throwable t) {
                        System.err.println("RecordInfo error on " + type + " : " + t.getMessage());
                    }

                    if (dd1 != null && dd2 != null) {
                        compare(dd1, dd2);
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tempDir != null) {
            try {
                FileUtils.deleteDirectory(tempDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Extract all corpus MDDs in a zip file
     */
    private static void extractMDDsFile(ZipFile zf, File tempDir) throws IOException {

        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            if (!(ze.getName().endsWith(".mdd") || ze.getName().endsWith(".idd"))) {
                continue;
            }

            BufferedInputStream bis = new BufferedInputStream(zf.getInputStream(ze));
            int size;
            byte[] buffer = new byte[2048];
            int n = ze.getName().lastIndexOf(File.separator);
            File dir = new File(tempDir.getPath() + File.separator + ze.getName().substring(0, n));
            dir.mkdirs();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dir.getPath() + File.separator
                    + ze.getName().substring(n)), buffer.length);
            while ((size = bis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }

            bos.flush();
            bos.close();
            bis.close();
        }

    }

    /**
     * Compares the structure of two DataDefinition-s and their underlying FieldDefinition-s
     */
    private static void compare(DataDefinition dd1, DataDefinition dd2) {

        String t = dd1.getName();
        check("getName", t, null, dd1.getName(), dd2.getName());
        check("lastModified", t, null, dd1.lastModified(), dd2.lastModified());
        check("getIndexPointerFieldName", t, null, dd1.getIndexPointerFieldName(), dd2.getIndexPointerFieldName());
        check("getCreationDateFieldName", t, null, dd1.getCreationDateFieldName(), dd2.getCreationDateFieldName());
        check("getLastModificationDateFieldName", t, null, dd1.getLastModificationDateFieldName(),
            dd2.getLastModificationDateFieldName());
        check("getSetMemberFieldName", t, null, dd1.getSetMemberFieldName(), dd2.getSetMemberFieldName());
        check("getSetOwnerFieldName", t, null, dd1.getSetOwnerFieldName(), dd2.getSetOwnerFieldName());
        check("isTemporary", t, null, dd1.isTemporary(), dd2.isTemporary());
        check("getTitleFieldName", t, null, dd1.getTitleFieldName(), dd2.getTitleFieldName());
        check("getParentField", t, null, dd1.getParentField(), dd2.getParentField());
        check("getSetOwnerFieldName", t, null, dd1.getSetOwnerFieldName(), dd2.getSetOwnerFieldName());
        check("getSetOwnerFieldName", t, null, dd1.getSetOwnerFieldName(), dd2.getSetOwnerFieldName());

        // fields
        Vector<String> v1 = dd1.getFieldNames();
        Vector<String> v2 = dd2.getFieldNames();
        check("getFieldNames", t, null, v1, v2);

        for (String f : v1) {

            FieldDefinition fd1 = dd1.getFieldDefinition(f);
            FieldDefinition fd2 = dd2.getFieldDefinition(f);

            // basic info
            check("getName()", t, f, fd1.getName(), fd2.getName());
            check("getType()", t, f, fd1.getType(), fd2.getType());
            check("getDefaultValue()", t, f, fd1.getDefaultValue(), fd2.getDefaultValue());
            check("getNull()", t, f, fd1.getNull(), fd2.getNull());
            check("getDataDefinition().getName()", t, f, fd1.getDataDefinition().getName(),
                fd2.getDataDefinition().getName());

            if (fd1.getOriginalFieldDefinition() != null && fd2.getOriginalFieldDefinition() != null) {
                check("getOriginalFieldDefinition().getName()", t, f, fd1.getOriginalFieldDefinition().getName(),
                    fd2.getOriginalFieldDefinition().getName());
            } else if (fd1.getOriginalFieldDefinition() != null && fd2.getOriginalFieldDefinition() == null) {
                System.err.println("getOriginalFieldDefinition null for fd2");
            } else if (fd2.getOriginalFieldDefinition() != null && fd1.getOriginalFieldDefinition() == null) {
                System.err.println("getOriginalFieldDefinition null for fd1");
            }

            check("isIndexPointerField()", t, f, fd1.isIndexPointerField(), fd2.isIndexPointerField());
            check("getEmptyValue()", t, f, fd1.getEmptyValue(), fd2.getEmptyValue());
            check("hasDescription()", t, f, fd1.hasDescription(), fd2.hasDescription());
            check("getDescription()", t, f, fd1.getDescription(), fd2.getDescription());
            check("getIntegerType()", t, f, fd1.getIntegerType(), fd2.getIntegerType());
            check("getDataType()", t, f, fd1.getDataType(), fd2.getDataType());
            check("getJavaType()", t, f, fd1.getJavaType(), fd2.getJavaType());
            check("isFixed()", t, f, fd1.isFixed(), fd2.isFixed());
            check("isNotNull()", t, f, fd1.isNotNull(), fd2.isNotNull());
            check("isNotEmpty()", t, f, fd1.isNotEmpty(), fd2.isNotEmpty());
            check("isUnique()", t, f, fd1.isUnique(), fd2.isUnique());
            check("isDateType()", t, f, fd1.isDateType(), fd2.isDateType());
            check("isNumberType()", t, f, fd1.isNumberType(), fd2.isNumberType());
            check("isIntegerType()", t, f, fd1.isIntegerType(), fd2.isIntegerType());
            check("isRealType()", t, f, fd1.isRealType(), fd2.isRealType());
            check("isBinaryType()", t, f, fd1.isBinaryType(), fd2.isBinaryType());
            check("isBooleanType()", t, f, fd1.isBooleanType(), fd2.isBooleanType());
            check("isRealType()", t, f, fd1.isRealType(), fd2.isRealType());
            check("isFileType()", t, f, fd1.isFileType(), fd2.isFileType());
            check("isSetType()", t, f, fd1.isSetType(), fd2.isSetType());
            check("isSetEnumType()", t, f, fd1.isSetEnumType(), fd2.isSetEnumType());
            check("isEnumType()", t, f, fd1.isEnumType(), fd2.isEnumType());
            check("isInternalSet()", t, f, fd1.isInternalSet(), fd2.isInternalSet());
            check("isComplexSet()", t, f, fd1.isComplexSet(), fd2.isComplexSet());
            check("isPointer()", t, f, fd1.isPointer(), fd2.isPointer());
            check("isStringType()", t, f, fd1.isStringType(), fd2.isStringType());
            check("getNotNullErrorMessage()", t, f, fd1.getNotNullErrorMessage(), fd2.getNotNullErrorMessage());
            check("getNotANumberErrorMessage()", t, f, fd1.getNotANumberErrorMessage(), fd2.getNotANumberErrorMessage());
            check("getNotUniqueErrorMessage()", t, f, fd1.getNotUniqueErrorMessage(), fd2.getNotUniqueErrorMessage());
            check("getNotEmptyErrorMessage()", t, f, fd1.getNotEmptyErrorMessage(), fd2.getNotEmptyErrorMessage());
            check("getNotUniqueErrorMessage()", t, f, fd1.getNotUniqueErrorMessage(), fd2.getNotUniqueErrorMessage());
            check("getNotIntErrorMessage()", t, f, fd1.getNotIntErrorMessage(), fd2.getNotIntErrorMessage());
            check("getNotRealErrorMessage()", t, f, fd1.getNotRealErrorMessage(), fd2.getNotRealErrorMessage());
            check("getNotBooleanErrorMessage()", t, f, fd1.getNotBooleanErrorMessage(), fd2.getNotBooleanErrorMessage());
            check("isDefaultField()", t, f, fd1.isDefaultField(), fd2.isDefaultField());
            check("shouldEditBySingleInput()", t, f, fd1.shouldEditBySingleInput(), fd2.shouldEditBySingleInput());

            try {
                check("getSubtable().getName()", t, f, fd1.getSubtable().getName(), fd2.getSubtable().getName());

            } catch (Throwable t1) {
            }
            try {

                check("getPointedType().getName()", t, f, fd1.getPointedType().getName(),
                    fd2.getPointedType().getName());
            } catch (Throwable t1) {
            }
            try {

                check("getDefaultString()", t, f, fd1.getDefaultString(), fd2.getDefaultString());
            } catch (Throwable t1) {
            }
            try {

                check("getWidth()", t, f, fd1.getWidth(), fd2.getWidth());
            } catch (Throwable t1) {
            }
            try {

                check("getForeignTable().getName()", t, f, fd1.getForeignTable().getName(),
                    fd2.getForeignTable().getName());
            } catch (Throwable t1) {
            }
            try {

                check("getTitleField", t, f, fd1.getTitleField(), fd2.getTitleField());
            } catch (Throwable t1) {
            }
            try {

                check("getDefaultInt", t, f, fd1.getDefaultInt(), fd2.getDefaultInt());
            } catch (Throwable t1) {
            }
            try {
                check("getValues", t, f, fd1.getValues(), fd2.getValues());
            } catch (Throwable t1) {
            }
            try {

                check("getEnumeratorSize", t, f, fd1.getEnumeratorSize(), fd2.getEnumeratorSize());
            } catch (Throwable t1) {
            }
            try {

                check("getDeprecatedValues", t, f, fd1.getDeprecatedValues(), fd2.getDeprecatedValues());
            } catch (Throwable t1) {
            }

            /*
            getNameAt
            getIntAt
            getNameFor

            checkValue
            checkInsert
            isAssignableFrom

            addValidationRule
            addValidationRule
            getValidationRules

             */

        }

    }

    private static boolean check(String property, String typeName, String fieldName, Object v1, Object v2) {
        if (v1 == null && v2 == null) {
            return true;
        }
        if (!v1.equals(v2)) {
            System.err.println("Property '" + property + "' of type " + typeName
                    + (fieldName != null ? " of field '" + fieldName + "'" : "") + " not the same: new provider '" + v1
                    + "' vs. old provider '" + v2 + "'");
            return false;
        }
        return true;
    }

}
