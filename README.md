##Curse of Enchantia image extraction utility
This utility will create a BMP file out of a decompressed DAT file or a SPR file and their associated PAL (palette) file.

DAT files are compressed with RNC (Rob Northern Compression). An utility to decompress this kind of files can be found [here](https://github.com/lab313ru/rnc_propack_source).

The utility is also able to print a PAL file's colors into the terminal.

###Installation:
`mvn clean install`

###Usage:
`java -jar coe-read-dat.jar [dat|spr|pal] <uncompressed DAT file|SPR file> <associated PAL file>`

###TODO
- Complete sprites support
- Add automatic unpacking of RNC-packed files

##License
[BMP-IO](https://github.com/nayuki/BMP-IO) is included in this software and distributed under the MIT license:

-------

Copyright © 2016 Project Nayuki. (MIT License)  
[https://www.nayuki.io/page/bmp-io-library-java](https://www.nayuki.io/page/bmp-io-library-java)

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

* The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

* The Software is provided "as is", without warranty of any kind, express or
  implied, including but not limited to the warranties of merchantability,
  fitness for a particular purpose and noninfringement. In no event shall the
  authors or copyright holders be liable for any claim, damages or other
  liability, whether in an action of contract, tort or otherwise, arising from,
  out of or in connection with the Software or the use or other dealings in the
  Software.