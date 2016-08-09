The libraries makumba uses are structured as follows:

building/
  libraries necessary for the building environment, mostly ANT tasks and libraries needed for building, scp/ftp upload, svn...

common/
  libraries used both by makumba and by other libraries

core/
  the core libraries required to run a makumba webapp. this should really be kept to a minimum

db/
  database drivers (MySQL, MSSQL, ...)
  
doc/
  libraries required for the documentation

optional/
  libraries that are optional at runtime (advanced devel tools, hibernate db layer)

optional/hibernate
  all the hibernate-related libs as well as its dependencies. needs cleaning up.
  
test/
  libraries for testing (JUnit, Cactus, Selenium, ...)
  