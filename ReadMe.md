ScalaFX Extras
==============

[![Build Status](https://travis-ci.org/scalafx/scalafx-extras.svg?branch=master)](https://travis-ci.org/scalafx/scalafx-extras)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.11)
[![Scaladoc](http://javadoc-badge.appspot.com/org.scalafx/scalafx-extras_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/org.scalafx/scalafx-extras_2.11)

Additions to ScalaFX that do not have corresponding concepts in JavaFX. 
  * Package `org.scalafx.extras` contains basic helper methods for running tasks on threads and showing exception messages.
  * Package `org.scalafx.extras.modelview` contains classes for creating with UI components based on FXML.

Module `scalafx-extras-demos` has a demo StopWatch application that illustrates uses of the Model-View and FXML API.

ScalaFX Extras is still quite experimental and APIs may change significantly.

To use ScalaFX Extras with SBT add following dependency:

```
libraryDependencies += "org.scalafx" %% "scalafx-extras" % "0.1.0"
```

Discussion and Support
----------------------

Please use [ScalaFX Users Group](https://groups.google.com/forum/#!forum/scalafx-users). Please report issues using the projects Issue tracker.


License
-------

BSD-3-Clause ScalaFX license.