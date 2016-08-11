Kurator Akka Web Demo
=================================

Demonstrates the use of actors in a workflow builder that allows you to dynamically add and configure actors to a
running system

Check out this project and run from the command line via "activator run". The activator will download all dependencies
and compile the project for you automatically.

Directory structure
===========

- app/actors/

 Contains implementations of the akka actors used in the builder

- app/controllers

 Demo controller handles all of the http requests and passes them off as messages to the actor system

- app/messages

 Messages used by the actors

- app/transformers

 Transformer interface and implementation classes used by the StringTransformer actor.

- app/views

 Html template pages

- conf/routes

 The http endpoint to controller method mappings file

- data

 Test data that can be used as input
