#JavaMPD

## Overview
This is a fork of [TheJavaShop's JavaMPD](http://www.thejavashop.net/javampd) version 4.1 which was released in May 2012.
Unfortunately, it seems that the code is no longer maintained there.

I forked the lib primarily to be able to commit *bugfixes*. See commit messages to get details on the changes I've made.

The original source code is available at [Google Code](http://code.google.com/p/javampd/)

## Notes
The only change in behaviour is that `MPDSong.equals()` holds only if the two references are equal. 
Before, it was enough if the two filesnames were equal.

## Future Releases
For the next release, I would make the following changes in the API

* Remove MPDSong.getPosition()
* Remove MPDSong.getId() if that's possible
* Don't let MPDItem implement Comparable - it's a can of worms
* Remove all exception declarations that are never used

## License
This software has been licensed under [GNU GPL v3](http://www.gnu.org/licenses/gpl.html)
