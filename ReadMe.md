ScalaFX Extras
==============

[![Build Status](https://travis-ci.org/scalafx/scalafx-extras.svg?branch=master)](https://travis-ci.org/scalafx/scalafx-extras)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12)
[![Scaladoc](http://javadoc-badge.appspot.com/org.scalafx/scalafx-extras_2.12.svg?label=scaladoc)](http://javadoc-badge.appspot.com/org.scalafx/scalafx-extras_2.12)

ScalaFX Extras are additions to ScalaFX that simplify creation of User interfaces. 
In contrast to ScalaFX core, the Extras do not have direct corresponding concepts in JavaFX. 

**Contents**

0. [Project Structure](#project-structure)
0. [SBT](#sbt)
0. [Features](#features)
   0. [Helper Methods](#helper-methods)
   0. [Simpler Display of Dialogs](#simpler-display-of-dialogs)
   0. [Simpler Use of FXML with MVCfx Pattern](#simpler-use-of-fxml-with-mvcfx-pattern)
0. [Demos](#demos)
   0. [StopWatch Application](#stopwatch-application)
   0. [ShowMessage Demo](#showmessage-demo)
0. [Status](#status)
0. [Discussion and Support](#discussion-and-support)
0. [License](#license)

Project Structure
-----------------

Module `scalafx-extras` contain feature implementations.
Module `scalafx-extras-demos` illustrates use of `scalafx-extras`

SBT
---

To use ScalaFX Extras with SBT add following dependency:

```
libraryDependencies += "org.scalafx" %% "scalafx-extras" % scalafx_extras_version
```

The latest published ScalaFX Extras version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.scalafx/scalafx-extras_2.12)

Features
--------

### Helper Methods

Package `org.scalafx.extras` contains basic helper methods for running tasks on threads and showing exception messages.
The main helper methods:

* `onFX` run code on FX Application thread in parallel
* `onFXAndWait` run code on FX Application thread and wait till finished
* `offFX` run code a thread in parallel
* `offFXAndWait` run code a thread and wait till finished
* `showException` show an exception dialog

Example scheduling some code on FX Application thread
```scala
onFX {
    counterService.doResume()
    _running.value = true
}

```

Example execution some code on a separate thread and waiting for the result of computation
```scala
val x = offFXAndWait {
    val a = 3
    val b = 7
    a * b
}

```

### Simpler Display of Dialogs

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

### Simpler Use of FXML with MVCfx Pattern

Package `org.scalafx.extras.mvcfx` contains classes for creating with UI components based on FXML.

The demos module has a complete example of a simple application: `StopWatchApp`.

Demos
-----

Module `scalafx-extras-demos` contains examples of using ScalaFX Extras.

### StopWatch Application

`StopWatchApp` is an application that illustrates uses of the MVCfx: a Model-Controller and SFXML/FXML API.

### ShowMessage Demo
* `ShowMessageDemoApp`: full example of using `ShowMessage` and MVCfx.

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