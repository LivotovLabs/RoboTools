RoboTools v2
============

This version is deprecated
------
V2 is the deprecated temp version, for the compatibility purposes only. Consider using master branch for the current V3 generation
with the new api and functionality. Thanks you for your supoprt !





RoboTools is an AAR library, containing a large collection of utility classes for making android apps development process 
a bit more comfortable and quicker.

Version 2 is a completely reviewed, redesigned and refactored one in comparison to previous release. It also lives as
standard Android AAR library now and completely moved to gradle.



Installation
------------
Current compiled snapshots and releases could be automatically added to your project using gradle from our maven repository http://maven.livotovlabs.pro

Add link to our repository to your gradle build file (repositories section):

``
    maven { url 'http://maven.livotovlabs.pro/content/groups/public' }
``

Then append "compile" statement as follows:

For snapshots:

``
    compile 'labs.livotov.eu:robotools:2.0.0-SNAPSHOT'
``

For releases

``
    compile 'labs.livotov.eu:robotools:2.x.x' // No release available at the moment
``

Alternatively, you may download the source code and build it on your own.



Previous Version (V1) Compatibility
------------------------------
RoboTools V2 is mainly NOT compatible to V1 (although, package structure and class namings are similar). Moreover,
some sensitive helper classes, such as crypto ones, may not be compatible with old V1 data.

For your convenience, all V1 classes are present and just moved to "compat.v1" subpackage, so you may use this new RoboTools
with your old projects by just adjusting the "import" statements in your code, then slowly migrate to V2 classes.


License
-------
RoboTools is licensed under Apache V2 license, so feel free to use it in any open-source or commercial project.


Contact
-------
If you have any bugs, ideas about changing existing helpers or adding new ones - feel free to post the information into the issues section. Or also join the development and submit pull requests :)
