package org.makumba.db.sql.oql;


public class GrammarTest
{
   public static void main (String [] argv) throws Exception
   {
       OQLLexer lexer =  new OQLLexer(new java.io.StringBufferInputStream(argv[0]));
       OQLParser parser = new OQLParser(lexer);
       // Parse the input expression
       OQLAST t= null;
         parser.queryProgram();
   }

}
