RoboTools V3
===

RoboTools is a micro framework and toolset library for Android apps, aimed to make every day apps
development easier and quickier by automating most routined tasks, which are typical for every app.


Compatibility Notice
---

- Supports SDK Android 4.1+ only. Yes, it is time to retire old android APIs.
- API is **NOT** compatible with V1 and V2 of RoboTools.


Status
---

- Stable version: **n/a**
- Dev snapshot: **n/a**


Installation
------------

Include our maven repo to your gradle project file maven repositories list:
``
    maven { url 'http://maven.livotovlabs.pro/content/groups/public' }
``

Then add dependency as follows:

For snapshot:

``
    compile 'labs.livotov.eu:robotools:x.y.z-SNAPSHOT'
``

For release:

``
    compile 'labs.livotov.eu:robotools:x.y.z'
``

*Replace x.y.z with the proper version number you want to use**


License
-------
RoboTools is licensed under Apache V2 license, so feel free to use it in any open-source or commercial project.


Contact
-------
If you have any bugs, ideas about changing existing helpers or adding new ones - feel free to post the information into the issues section. Or also join the development and submit pull requests :)


Credits
---

- @soarcn for the great @soarcn/BottomSheet project that was used as a basement for our custom implementation in RoboTools
