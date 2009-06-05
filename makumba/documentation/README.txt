This is the base directory of the Makumba documentation.

It uses apache forrest 0.8, but you'll need to do a few things to your forrest installation in order to make it work

== images in PDFs

forrest uses FOP to generate PDFs and not all images types are supported. to get it to work:
1/ download JIMI at http://java.sun.com/products/jimi/
2/ extract it, and copy the JimiProClasses.zip to $FORREST_HOME/lib/optional/jimi-1.0.jar (rename it)

