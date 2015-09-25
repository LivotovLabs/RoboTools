RoboTools V3
===

RoboTools is a micro framework and toolset library for Android apps, aimed to make every day apps
development easier and quickier by automating most routined tasks, which are typical for every app.


Current version numbers
---

- Stable Version: **n/a**
- Dev Snapshot: **3.0.0.DEV1-SNAPSHOT**

  The library is under heavy development and refactoring right now. Use provided sources and maven
  snapshots at your own risk. Packages structure, classes or methods may be reorganized, changed or
  completely removed at anu time.

  First stable release is planned by the end of 2015


Compatibility Notice
---

- Supports SDK Android 4.1+ only. Yes, it is time to retire old android APIs.
- API is **NOT** compatible with V1 and V2 of RoboTools.



Installation
---

Include our maven repo to your gradle project file maven repositories list:

``
    maven { url 'http://maven.livotovlabs.pro/content/groups/public' }
``

Then add dependency as follows:

``
    compile 'labs.livotov.eu:robotools:x.y.z'
``


*Replace x.y.z with the proper version number (currently available versions are listed above) you want to use**


License
---
RoboTools is licensed under Apache V2 license, so feel free to use it in any open-source or commercial project.


Contact
---
Please post all bugs, featurerequests, etc into the issues section. As usual, pull requests are welcome.


Credits
---

- @soarcn for the great https://github.com/soarcn/BottomSheet project that was used as a basement for our custom implementation in RoboTools
