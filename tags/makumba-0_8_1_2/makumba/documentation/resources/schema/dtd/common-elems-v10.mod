<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- ===================================================================

     Apache Common Elements (Version 1.0)

PURPOSE:
  Common elements across DTDs

TYPICAL INVOCATION:

  <!ENTITY % common PUBLIC
      "-//APACHE//ENTITIES Common elements Vx.y//EN"
      "common-elems-vxy.mod">
  %common;

  where

    x := major version
    y := minor version

FIXME:

CHANGE HISTORY:
[Version 1.0]
  20020611 Initial version. (SN)
  20050509 Added importance attribute. (JJP)
  20050606 Remove context an entity list. #FOR-514 (JJP)
  20050609 action@type is required. (JJP)

==================================================================== -->

<!-- =============================================================== -->
<!-- Common entities -->
<!-- =============================================================== -->

<!ENTITY % types "add|remove|update|fix">
<!ENTITY % importances "high|medium|low">

<!-- =============================================================== -->
<!-- Common elements -->
<!-- =============================================================== -->

<!ELEMENT devs (person+)>
<!ATTLIST devs %common.att;>

<!ELEMENT action (%content.mix;)*>
<!ATTLIST action %common.att;
          dev  IDREF  #REQUIRED
          type (%types;)  #REQUIRED
          context  IDREF  #REQUIRED
          importance (%importances;) "medium"
          due-to CDATA #IMPLIED
          due-to-email CDATA #IMPLIED
          fixes-bug CDATA #IMPLIED>

<!-- =============================================================== -->
<!-- End of DTD -->
<!-- =============================================================== -->
