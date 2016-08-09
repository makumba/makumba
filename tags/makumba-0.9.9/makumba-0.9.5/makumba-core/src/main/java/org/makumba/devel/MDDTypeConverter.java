package org.makumba.devel;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.commons.NamedResources;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.Database;
import org.makumba.db.makumba.MakumbaTransactionProvider;
import org.makumba.db.makumba.sql.SQLDBConnection;
import org.makumba.db.makumba.sql.TableManager;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.mdd.DataDefinitionImpl;
import org.makumba.providers.datadefinition.mdd.FieldType;

/**
 * This class provides a developer tool to convert from single relational or enum types (intEnum, ptr) to set types
 * (setIntEnum, set). Currently, only support for the intEnum is provided.
 * 
 * @author Rudolf Mayer
 * @version $Id: MDDTypeConverter.java,v 1.1 Apr 5, 2010 12:39:54 AM rudi Exp $
 */
public class MDDTypeConverter {

    public static void main(String[] args) throws IOException, URISyntaxException, CloneNotSupportedException,
            SQLException, LogicException {
        // create CLI options
        Options options = new Options();

        Option mddNameOption = new Option("m", "mdd", true, "the name of the mdd to refacator in");
        mddNameOption.setRequired(true);
        Option fieldNameOption = new Option("f", "field", true, "the name of the field to change");
        fieldNameOption.setRequired(true);
        Option targetTypeOption = new Option("t", "targetType", true, "the target type of the field");
        targetTypeOption.setRequired(true);
        Option sqlOnlyOption = new Option("s", "sqlOnly", false,
                "only generate the SQL conversion scripts, don't do any MDD modifications");
        targetTypeOption.setRequired(true);

        options.addOption(mddNameOption);
        options.addOption(fieldNameOption);
        options.addOption(targetTypeOption);
        options.addOption(sqlOnlyOption);

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new PosixParser();
        CommandLine line = null;

        try {
            line = parser.parse(options, args);
        } catch (ParseException p) {
            System.out.println("Error parsing the options for the MDD field type converter: " + p.getMessage());
            System.out.println();
            formatter.printHelp("java " + MDDTypeConverter.class.getName() + " [OPTION]... [FILE]...", options);
            System.exit(-1);
        }

        String mddName = line.getOptionValue("m");
        String fieldName = line.getOptionValue("f", "");
        String targetTypeString = line.getOptionValue("t", "");
        boolean sqlOnly = line.hasOption("s");

        if (sqlOnly) {
            Logger.getLogger("org.makumba.devel").info(
                "Generating only SQL scripts, not doing any MDD/DB modifications");
        }

        // check if we have a correct field type
        FieldType.valueOf(targetTypeString.toUpperCase());

        DataDefinition mdd = DataDefinitionProvider.getInstance().getDataDefinition(mddName);
        FieldDefinition fd = mdd.getFieldDefinition(fieldName);

        // check if we have a correct source and target field type
        if (fd.getIntegerType() == FieldDefinition._intEnum) {
            if (!targetTypeString.equals("set int")) {
                throw new LogicException("Unknown target field type " + targetTypeString + " for source type "
                        + fd.getDataType() + ". Valid type is 'set int'.");
            }
        } else if (fd.getIntegerType() == FieldDefinition._ptr) {
            if (!targetTypeString.equals("set")) {
                throw new LogicException("Unknown target field type " + targetTypeString + " for source type "
                        + fd.getDataType() + ". Valid type is 'set'.");
            }
        } else {
            throw new LogicException("Unknown source field type " + fd.getType());
        }

        Database d = MakumbaTransactionProvider.getDatabase(Configuration.getDefaultDataSourceName());

        if (!sqlOnly) {

            // step 1: make sure the DB is in synch with the mdd, by asking makumba to check it
            d.openTable(mdd.getName());

            URL url = DataDefinitionProvider.findDataDefinition(mddName, "mdd");
            File f = new File(url.toURI());
            Logger.getLogger("org.makumba.devel").info("Reading MDD from " + f.getAbsolutePath());

            // step 2: modify the MDD, in the file on disk.
            // This doesn't work well if we have an intEnum which is defined by a macro type

            // step 2.1: backup existing file
            File tempFile = File.createTempFile(mdd.getName() + "_old", ".mdd");
            FileUtils.copyFile(f, tempFile);
            Logger.getLogger("org.makumba.devel").info("Making backup copy to " + tempFile.getAbsolutePath());

            String encoding = System.getProperty("file.encoding");
            @SuppressWarnings("unchecked")
            List<String> lines = FileUtils.readLines(f, encoding);
            String[] fileContents = lines.toArray(new String[lines.size()]);

            // step 2.2: find the thing to modify
            Logger.getLogger("org.makumba.devel").info("Searching for field " + fieldName);
            boolean found = false;
            for (int i = 0; i < fileContents.length; i++) {
                String s = fileContents[i];
                String sFieldName = s.trim();
                if (s.contains("=")) {
                    sFieldName = sFieldName.substring(0, s.indexOf("=")).trim();
                }
                if (sFieldName.equals(fieldName.trim())) {
                    FieldDefinition fdTemp = mdd.getFieldDefinition(fieldName);
                    // need to replace the type
                    if (fdTemp.getIntegerType() == FieldDefinition._intEnum) {
                        // TODO: make sure we replace the correct place, and not part of the field name..
                        final String sNew = s.replace("int", "set int");
                        if (s.equals(sNew) || !sNew.trim().startsWith(fieldName)) { // did not managed to replace the
                            // type
                            Logger.getLogger("org.makumba.debug.abstr").warning(
                                "The changes could not be written back to the MDD, most probably as the type for '"
                                        + fieldName + "' is defined via a macro type.");
                        } else {
                            // step 2.3: found the line, managed to replace it: write back to the file
                            fileContents[i] = sNew;
                            FileUtils.writeLines(f, encoding, Arrays.asList(fileContents));
                            // clean the caches, so we read the MDD again
                            NamedResources.cleanupStaticCaches();
                        }
                    } else if (fdTemp.getIntegerType() == FieldDefinition._ptr) {
                        // TODO: make sure we replace the correct place, and not part of the field name..
                        final String sNew = s.replace("ptr", "set");
                        if (s.equals(sNew) || !sNew.trim().startsWith(fieldName)) { // did not managed to replace the
                            // type
                            Logger.getLogger("org.makumba.debug.abstr").warning(
                                "The changes could not be written back to the MDD, most probably as the type for '"
                                        + fieldName + "' is defined via a macro type.");
                        } else {
                            // step 2.3: found the line, managed to replace it: write back to the file
                            fileContents[i] = sNew;
                            FileUtils.writeLines(f, encoding, Arrays.asList(fileContents));
                            // clean the caches, so we read the MDD again
                            NamedResources.cleanupStaticCaches();
                        }
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new LogicException("Did not find field among MDD contents:\n" + fileContents);
            }
        }

        mdd = DataDefinitionProvider.getInstance().getDataDefinition(mddName);
        fd = mdd.getFieldDefinition(fieldName);

        Logger.getLogger("org.makumba.devel").info("Changed field type to: " + fd.getDataType());
        if (!fd.getType().equals(targetTypeString)) {
            throw new LogicException("Unexpected target field type " + fd.getType() + ", expected " + targetTypeString);
        }

        if (!sqlOnly) {
            // step 3: trigger the DB changes
            d.openTable(fd.getSubtable().getName());
        }

        // step 4: copy the data
        TableManager parentTable = (TableManager) d.getTable(mddName);
        TableManager subTable = (TableManager) parentTable.getRelatedTable(fieldName);
        String subTableName = subTable.getDBName();
        String parentTableName = parentTable.getDBName();
        String parentIndexName = parentTable.getFieldDBName(mdd.getIndexPointerFieldName());
        String subIndexName = subTable.getFieldDBName(fd.getSubtable().getIndexPointerFieldName());

        // step 4.1: compose the insert statement
        String sql = null;
        if (fd.getIntegerType() == FieldDefinition._intEnum) {
            sql = "INSERT INTO " + subTableName + " (" + parentIndexName + ", " + subIndexName
                    + ", TS_create_, TS_modify_" + ", " + subTable.getFieldDBName(DataDefinitionImpl.ENUM_FIELD_NAME)
                    + ") SELECT " + parentIndexName + ", " + parentIndexName + ", TS_create_, TS_modify_, "
                    + subIndexName + " FROM " + parentTableName + ";";
        } else if (fd.getIntegerType() == FieldDefinition._ptr) {
            sql = "INSERT INTO " + subTableName + " (" + parentIndexName + ", " + subIndexName
                    + ", TS_create_, TS_modify_" + ", " + fieldName + ") SELECT " + parentIndexName + ", "
                    + parentIndexName + ", TS_create_, TS_modify_, " + subIndexName + " FROM " + parentTableName + ";";
        }
        System.out.println(sql);

        if (!sqlOnly) {
            // step 4.2: execute the insert
            // can not do that with the DataBase/Transaction class, need an SQLDBConnection
            DBConnection connection = d.getDBConnection();
            if (connection instanceof DBConnectionWrapper) {
                connection = ((DBConnectionWrapper) connection).getWrapped();
            }
            SQLDBConnection sqlConnection = (SQLDBConnection) connection;
            Statement statement = sqlConnection.createStatement();
            int execute = statement.executeUpdate(sql);
            sqlConnection.commit();
            sqlConnection.close();
            System.out.println("Executed update statement, " + execute + " rows affected.");
        }
    }
}
