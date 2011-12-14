Upstream decided to drop the Extension approach and go with a standalone version
that reads ppt (!) files by default - I don't like this idea, thus the fork.

If you want upstream, go to http://opencards.info/ - their current code is here:
http://code.google.com/p/opencards/source/checkout

Holger's code is licenced according to the license.terms files, my modifications will
(additionally) be under LGPLv3+/MPL

Holger has no relation to this fork.

################################################################################
This files containes some hints and tricks concerning the build process and the use of the OpenCards API.


2) To build OpenCards the location of an OpenOffice installation has to be provided as ant-proprerty. For instance windows user might use:
    ant -Doffice.home="C:\Program Files\OpenOffice.org 3" deploy
   To deploy OpenCards 
1) To make the demo- and test-applications to work with OOo 3.0 follow the idea of copying juh.jar to the program-directory. This only applies if you run into problems with the Bootstrap class. More infos about this subject can be found at http://user.services.openoffice.org/en/forum/viewtopic.php?f=44&t=10825


User profile locations:
Mac: ~/Library/Application Support/OpenOffice3/...Cloned from svn@599 ( https://opencards.svn.sourceforge.net/svnroot/opencards opencards )
