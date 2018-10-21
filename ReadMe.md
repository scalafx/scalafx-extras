ScalaFX Extras
==============

[![Build Status](https://travis-ci.org/scalafx/scalafx-extras.svg?branch=master)](https://travis-ci.org/scalafx/scalafx-extras)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12)
[![Scaladoc](http://javadoc-badge.appspot.com/org.scalafx/scalafx-extras_2.12.svg?label=scaladoc)](http://javadoc-badge.appspot.com/org.scalafx/scalafx-extras_2.12)

Additions to ScalaFX to simplify creation of User interfaces. ScalaFX Extras do not have direct corresponding concepts in JavaFX. 

Module `scalafx-extras` contain feature implementations.
Module `scalafx-extras-demos` illustrates use of `scalafx-extras`

Usage
-----

To use ScalaFX Extras with SBT add following dependency:

```
libraryDependencies += "org.scalafx" %% "scalafx-extras" % scalafx_extras_version
```

The latest published ScalaFX Extras version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12)

Features
--------

### `org.scalafx.extras` Helper Methods

Package `org.scalafx.extras` contains basic helper methods for running tasks on threads and showing exception messages.

### Simplify Display of Dialogs

The mixin `ShowMessage` makes it easier to display dialogs. It is typically used with a UI `Model`. 
The dialogs can be displayed using a single method, like `showInformation`, `showConfirmation`. `ShowMessage` takes care of blocking parent windows and using parent icons in dialogs. It can also log warnings, errors, and exceptions when warnings, errors, and exceptions dialogs are displayed. 

```scala
class MyUIModel extends Model with ShowMessage {

  def onSomeUserAction(): Unit = {
    // ...
    showInformation("Dialog Title",
      "This is the information \"header\"",
      "This is the information detailed \"content\".")
    // ...
  }
  
  // ...
}
```  
The demos module has a complete example of an simple application in `ShowMessageDemoApp`.

### Simplify of FXML and SFXML

Package `org.scalafx.extras.modelview` contains classes for creating with UI components based on FXML.

The demos module has a complete example of an simple application in `StopWatchApp`.

Demos
-----

Module `scalafx-extras-demos` contains example of using ScalaFX Extas:

* `StopWatchApp`: an application illustrates uses of the Model-Controller and SFXML/FXML API.
* `ShowMessageDemoApp`: full example of using `ShowMessage` and Model-Controller with SFXML/FXML API

Status
------

ScalaFX Extras is still quite experimental and APIs may change significantly.

Discussion and Support
----------------------

For discussion and support, please use [ScalaFX Users Group](https://groups.google.com/forum/#!forum/scalafx-users) 
or [ScalaFX on StackOverflow](https://stackoverflow.com/questions/tagged/scalafx).
Please report issues using the projects Issue tracker.


License
-------

BSD-3-Clause ScalaFX license.