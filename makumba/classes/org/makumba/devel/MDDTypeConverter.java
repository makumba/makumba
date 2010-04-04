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
import org.makumba.providers.datadefinition.mdd.MDDProvider;

/**
 * This class provides a developer tool to convert from single relational or enum types (intEnum, ptr) to set types
 * (setIntEnum, set). Currently, only support for the intEnum is provided.
 * 
 * @author Rudolf Mayer
 * @version $Id: MDDTypeConverter.java,v 1.1 Apr 5, 2010 12:39:54 AM rudi Exp $
 */
public class MDDTypeConverter {

    public static void main(String[] args) throws IOException, URISyntaxException, CloneNotSupportedException,
            SQLException {
        // create CLI options
        Options options = new Options();

        Option mddNameOption = new Option("m", "mdd", true, "the name of the mdd to refacator in");
        mddNameOption.setRequired(true);
        Option fieldNameOption = new Option("f", "field", true, "the name of the field to change");
        fieldNameOption.setRequired(true);
        Option targetTypeOption = new Option("t", "targetType", true, "the target type of the field");
        targetTypeOption.setRequired(true);

        options.addOption(mddNameOption);
        options.addOption(fieldNameOption);
        options.addOption(targetTypeOption);

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new PosixParser();
        CommandLine line = null;

        try {
            line = parser.parse(options, args);
        } catch (ParseException p) {
            System.out.println("Error while executing the mdd refactoring crawler: " + p.getMessage());
            System.out.println();
            formatter.printHelp("java " + MDDTypeConverter.class.getName() + " [OPTION]... [FILE]...", options);
            System.exit(-1);
        }

        String mddName = line.getOptionValue("m");
        String fieldName = line.getOptionValue("f", "");
        String targetTypeString = line.getOptionValue("t", "");

        // check if we have a correct field type
        FieldType.valueOf(targetTypeString.toUpperCase());

        DataDefinition mdd = DataDefinitionProvider.getInstance().getDataDefinition(mddName);

        Database d = MakumbaTransactionProvider.getDatabase(Configuration.getDefaultDataSourceName());

        // step 1: make sure the DB is in synch with the mdd, by asking makumba to check it
        d.openTable(mdd.getName());

        URL url = MDDProvider.findDataDefinition(mddName, "mdd");
        File f = new File(url.toURI());

        // step 2: modify the MDD, in the file on disk.
        // This doesn't work well if we have an intEnum which is defined by a macro type

        // step 2.1: backup existing file
        File tempFile = File.createTempFile(mdd.getName() + "_old", ".mdd");
        FileUtils.copyFile(f, tempFile);

        String encoding = System.getProperty("file.encoding");
        List<String> lines = FileUtils.readLines(f, encoding);
        String[] fileContents = lines.toArray(new String[lines.size()]);

        // step 2.2: find the thing to modify
        for (int i = 0; i < fileContents.length; i++) {
            String s = fileContents[i];
            String sFieldName = s.trim();
            if (s.contains("=")) {
                sFieldName = sFieldName.substring(0, s.indexOf("=")).trim();
            }
            if (sFieldName.equals(fieldName.trim())) {
                FieldDefinition fd = mdd.getFieldDefinition(fieldName);
                // need to replace the type
                if (fd.getIntegerType() == FieldDefinition._intEnum) {
                    // TODO: make sure we replace the correct place, and not part of the field name..
                    final String sNew = s.replace("int", "set int");
                    if (s.equals(sNew) || !sNew.trim().startsWith(fieldName)) { // did not managed to replace the type
                        Logger.getLogger("org.makumba.debug.abstr").warning(
                            "The changes could not be written back to the MDD, most probably as the type for '"
                                    + fieldName + "' is  defined via a macro type.");
                    } else {
                        // step 2.3: found the line, managed to replace it: write back to the file
                        fileContents[i] = sNew;
                        FileUtils.writeLines(f, encoding, Arrays.asList(fileContents));
                        // clean the caches, so we read the MDD again
                        NamedResources.cleanCaches();
                    }
                }
                break;
            }
        }

        mdd = DataDefinitionProvider.getInstance().getDataDefinition(mddName);
        System.out.println(mdd.getFieldDefinition(fieldName));

        FieldDefinition fd = mdd.getFieldDefinition(fieldName);

        // step 3: trigger the DB changes
        d.openTable(fd.getSubtable().getName());

        // step 4: copy the data
        TableManager parentTable = (TableManager) d.getTable(mddName);
        TableManager subTable = (TableManager) parentTable.getRelatedTable(fieldName);
        String subTableName = subTable.getDBName();
        String parentTableName = parentTable.getDBName();
        String parentIndexName = parentTable.getFieldDBName(mdd.getIndexPointerFieldName());
        String subIndexName = subTable.getFieldDBName(fd.getSubtable().getIndexPointerFieldName());

        // step 4.1: compose the insert statement
        String sql = "INSERT INTO " + subTableName + " (" + parentIndexName + ", " + subIndexName
                + ", TS_create_, TS_modify_" + ", " + subTable.getFieldDBName(DataDefinitionImpl.ENUM_FIELD_NAME)
                + ") SELECT " + parentIndexName + ", " + parentIndexName + ", TS_create_, TS_modify_, " + subIndexName
                + " FROM " + parentTableName + ";";

        System.out.println(sql);

        // step 4.2: execute the inser
        // can not do that with the DataBase/Transaction class, need an SQLDBConnection
        DBConnection connection = d.getDBConnection();
        if (connection instanceof DBConnectionWrapper) {
            connection = ((DBConnectionWrapper) connection).getWrapped();
        }
        SQLDBConnection sqlConnection = ((SQLDBConnection) connection);
        Statement statement = sqlConnection.createStatement();
        int execute = statement.executeUpdate(sql);
        sqlConnection.commit();
        sqlConnection.close();
        System.out.println("Executed update statement, " + execute + " rows affected.");
    }
}
