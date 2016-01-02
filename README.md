# jPOCSAG #

**jPOCSAG** is a [POCSAG](http://en.wikipedia.org/wiki/POCSAG) paging protocol decoder written in Java. It uses the sound card input to sample the signals from a radio receiver.

jPOCSAG tries its best to exploit the NFM-demodulated signals from the receiver (aka "earphone plug"), so a discriminator output is not compulsory.

_jPOCSAG is essentially a personal experiment that I think might interest other people. It functions correctly with my own setting, using a [Yaesu/Vertex Standard VX-6](http://www.yaesu.com/indexvs.cfm?cmd=DisplayProducts&ProdCatID=111&encProdID=4C6F204F6FEBB5BAFA58BCC1C131EAC0&DivisionID=65&isArchived=0). However, it is by no means supposed to be complete. Therefore please do not consider it is a released product. Rather, it is a codebase to experiment/play/tinker with, with no warranty of any kind._

Note that it is illegal to listen to or attempt to decode any non-public broadcast in many countries. Therefore this code is for legal use only, such as decoding amateur radio POCSAG traffic by authorized amateur radio operators.
