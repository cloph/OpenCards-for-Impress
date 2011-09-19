This files containes some hints and tricks concerning the build process and the use of the OpenCards API.


2) To build OpenCards the location of an OpenOffice installation has to be provided as ant-proprerty. For instance windows user might use:
    ant -Doffice.home="C:\Program Files\OpenOffice.org 3" deploy
   To deploy OpenCards 
1) To make the demo- and test-applications to work with OOo 3.0 follow the idea of copying juh.jar to the program-directory. This only applies if you run into problems with the Bootstrap class. More infos about this subject can be found at http://user.services.openoffice.org/en/forum/viewtopic.php?f=44&t=10825


User profile locations:
Mac: ~/Library/Application Support/OpenOffice3/...