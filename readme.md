Howto
=====

Run java -jar like this:
java -jar TransformPA.jar "E:\Dev\Eclipse\scalaide\workspace\TransformPAX\conf.json" "E:\Dev\Eclipse\scalaide\workspace\TransformPAX\transform.json"

So java -jar TransformPA.jar <config file> <transform file>

See the included config and transform files and change them for your needs.

The transform file has to be an array of objects. Each of these objects has an "affects" array of unit-IDs.
For every of those units the ops will be run. ops is an array of objects, every object is an operation to run. An operation can have the cmd +-*/=
and works only on numeric fields so far. keys is an array of arrays. The inner array is used to specify properties that are nested in the unit json like production.metal
The outer array is a list of properties to effect. Value is the value that is used together with cmd to modify the property.