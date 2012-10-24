![Electra](http://i.imgur.com/o9b7G.png)

# Introduction

Electra is a server that is created to be intuitive, well designed, and fast. It's currently under development and many things have yet to be implemented. As for why there's a github repository for something so barren, I plan on using it to catalog the progression of the project. This repository will be updated periodically to display progress.

## Authors
 
 * [Supah Fly](https://github.com/SuprahFry)

## Dependencies

 * [SnakeYAML](http://code.google.com/p/snakeyaml/) - YAML parser and emitter for configuration files
 * [Apache Commons BCEL](http://commons.apache.org/bcel/) - Bytecode Engineering Library for event 'compiler'

## Features
 * Math parser and evaluator, used in the compiler for converting expressions into bytecode for array lengths but can also be used in your projects as well. You can translate infix (the notation humans use) to postfix notation. From postfix notation you can evaluate the expression.
 * Efficient networking via Java's NIO API.
 * An improved and more usable event/handler system.
 * An extremely efficient and 'coder'-friendly network event (packet) system.
 * Support for reconnecting instead of just forcing the old player to disconnect and reload the same player as a different instance. Only the client that opened the original connection can reconnect to a session.
 * Simple and non-redundant naming conventions for variable and method names.
 * The login block is encrypted using RSA. This means that every session is secure.

## Compatibility Notes

It's important to note that I've removed all the "special" data types from the client. I've also removed all the random, unused packets since they're useless anyway. The reason for removing these things is because they serve no purpose but complication of the protocol. They were originally intended to make cheating and reverse-engineering harder; therefore, now that we understand the protocol, their existence isn't justifiable; therefore, they have been removed accordingly. Removal of the anti-cheat/injection packets causes no compatibility issues because no servers expected to receive those packets or read their data. The packets that reported mouse movement data are still intact, though. They still serve a purpose if servers wish to implement cheat detection. Of course, these are old solutions, so their effectiveness is questionable.

### Honorable Mentions

 * [super_](http://www.rune-server.org/members/veer/) - For his contributions to my knowledge of clients, servers, and theory.
 * [Major](https://github.com/Major-) - Being an intelligent person, and a true gaming pal.
 * [Blake Beaupain](https://github.com/blakeman8192) - For [RuneSource](https://www.assembla.com/code/runesource/subversion/nodes), which I used as a basis for a few things, not to mention all his contributions to servers in general.

### Credits

 * Apollo - I took some things out, built on them, and definitely improved them.
 * [RuneSource](https://www.assembla.com/code/runesource/subversion/nodes) - I used this as a crutch to wrap my head around NIO.
 * [Stack Overflow](http://stackoverflow.com/questions/1593080/how-can-i-modify-my-shunting-yard-algorithm-so-it-accepts-unary-operators) - I'm not sure what question it was but the users of Stack Overflow really pointed me in the right direction.
 * [LiteratePrograms](http://en.literateprograms.org/Shunting_yard_algorithm_%28C%29) - Useful implementation of the [Shunting-yard algorithm](http://en.wikipedia.org/wiki/Shunting-yard_algorithm) used for parsing infix notational expressions outputting in postfix or Reverse Polish notation.
 * [Nikki](http://www.rune-server.org/members/nikki/) - For the thread, [Enabling RSA](http://www.rune-server.org/runescape-development/rs2-server/tutorials/305532-any-revision-enabling-rsa.html), I used it as reference to minimize the amount of debugging I have to do. I also used her key generator.