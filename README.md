Overview
========

A simple framework for creating distributed functional tests. This library aims
to be for distributed functional tests what unittest libraries are for unit tests.
It provides equivalent of test scenarios, runners, initializers and dealocators in
such a way that it is easy to configure new testing environment, generate a report etc.

It is better than using bash/python scripts for such tasks, because:

* Changing testing environment or parameters is as easy as changing a few lines
  of configuration.
* dfuntest uses statically typed language - making sure your tests only fail
  when the actual code tested fails and not, because test scripts are buggy.
* Using Java makes it easy to write nontrivial testing scenarios.
* Allows you not to worry about how to correctly set up environment when
  writing each test.

Building
========

dfuntest uses gradle with java plugin for typical java build operations.

Usage
=====

To use dfuntest you need to first prepare your project:

* Define your own EnvironmentPreparator which prepares environment on which
  your application is run.
* Write App implementation which acts as a proxy to your application's
  interface.

Having the above the only thing you need to write is TestScript for each 
test you have and run TestRunner with those TestScripts.

History
=======

This package was created as an effort to simplify the work of a test writer in
Nebulostore project while ensuring that those test will properly catch errors
and generate useful reports. 
