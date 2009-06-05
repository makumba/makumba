This is the base directory of the Makumba documentation.

Installation
------------

The documentation is rendered using Apache Forrest 0.8 (http://forrest.apache.org/).
You'll need to do a few things to your Forrest installation in order to make it work

== images in PDFs

Forrest uses FOP to generate PDFs and not all images types are supported. To get it to work:
1/ download JIMI at http://java.sun.com/products/jimi/
2/ extract it, and copy the JimiProClasses.zip to $FORREST_HOME/lib/optional/jimi-1.0.jar (i.e., rename it)


Folder structure
----------------

There are many folders in /documentation, but the most important one for documentation is

  content/xdocs

The rest are directories needed for the customization or for Forrest to run:

  /classes          contains the CatalogManager.properties which tells where catalog files are
  /plugins          the plugins directory. contains a wiki plugin for now, may be removed later.
  /resources        several resources for Forrest
  /resources/schema the DTD schema used by Forrest. We have a customised version of it which allows
                    for special things such as <li> in <note> tags and the <ihtml> tag which allows
                    to have custom html tags included (the latter might be removed, to use screenshots insteads)
  /skins            the skins for forrest. currently, pelt-cusomised is used (we need a better one)
  /skins/common     the common directory for skins, copied from the forrest distrib. it is a bit customised
                    (e.g. contains a bugfix for images in PDFs)
  
  /tag-examples     this has nothing to do with Forrest, it is the example section of the taglib documentation, which is merged
                    with the documentation generated from the makumba taglib
                    

Makumba taglib documentation generation
---------------------------------------

Instead of documenting the tags by hand, the documentation is generated from the /classes/META-INF/taglib-documented.xml file.
This file is also used in order to generate the makumba and makumba-hibernate DTD (this is done by org.makumba.commons.MakumbaTLDGenerator).

The generation is done in two steps:

1/ generate the taglib files and the example files
2/ merge the files together

During step 1, the taglib files are generated under content/xdocs/doc/taglib and the example files under /tag-examples.
Then, the example files can be modified. Step 2 merges the files together in /content/xdocs/doc/taglib.

Note that during the generation process, a few things are done:
- attribute specifications are copied, e.g.
    
    <attribute name="from" specifiedIn="list"/>
                      
  copies the attribute "from" from the "list" tag specification.

- the "see" tags are resolved, e.g.

    <see>value, object</see>
    
   is resolved to links to the value and object tags in the "See also" section.

