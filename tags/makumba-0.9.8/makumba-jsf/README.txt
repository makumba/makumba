=== scaffold for makumba JSF taglib

- ListComponent is for now a copy-paste of ui:repeat
- since we do not use CDI for now, all ui components have to be registered in the faces-context.xml of the client application.
  this either looks like a bug in Mojarra (other annotations such as @ManagedBean work fine),
  or like a hard requirement on a managed environment (full J2EE). I guess using CDI will be the way to go if we want to simplify
  the configuration for client applications
