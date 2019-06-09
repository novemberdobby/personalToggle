# Personal Builds Toggle - TeamCity plugin to restrict personal builds to specific agents
PBT allows setting individual agents to run only personal builds, only non-personal builds, or both (default):

![settings_tab](/images/settings_tab.png)

If either type can't run on any current agents, this is highlighted:

![unavailable](/images/unavailable.png)

Written for https://youtrack.jetbrains.com/issue/TW-6773

## Building
This plugin is built with Maven. To compile it, run the following in the root folder:

```
mvn package
```