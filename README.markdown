![Electra](http://i.imgur.com/o9b7G.png)

# Introduction

Electra is a server that is created to be intuitive, well designed, and fast. It's currently under development and many things have yet to be implemented. As for why there's a github repository for something so barren, I plan on using it to catalog the progression of the project. This repository will be updated periodically to display progress.

## Authors
 
 * [Supah Fly](https://github.com/SuprahFry) @ [Rune-Server](http://www.rune-server.org/members/supah+fly/) // [SupahFly.net](http://www.supahfly.net/)

## Dependencies

 * [SnakeYAML](http://code.google.com/p/snakeyaml/) - YAML parser and emitter for configuration files
 * [Apache Commons BCEL](http://commons.apache.org/bcel/) - Bytecode Engineering Library for event 'compiler'
 * [Apache Commons Compress](http://commons.apache.org/compress/) - For BZip2 compression/decompression in the cache

## Features
 * Math parser and evaluator, used in the compiler for converting expressions into bytecode for array lengths but can also be used in your projects as well. You can translate infix (the notation humans use) to postfix notation. From postfix notation you can evaluate the expression.
 * Efficient networking via Java's NIO API.
 * An improved and more usable event/handler system.
 * An extremely efficient and 'coder'-friendly network event (packet) system.
 * Support for reconnecting instead of just forcing the old player to disconnect and reload the same player as a different instance. Only the client that opened the original connection can reconnect to a session.
 * Simple and non-redundant naming conventions for variable and method names.
 * The login block is encrypted using RSA. This means that every session is secure as long as the private key is unknown. Change the private/public key set if you're going to use this publicly.
 * Cache file and cache archive reading.
 * JAGGRAB and OnDemand file server for cache updating.

## Compatibility Notes

I've taken it upon myself to simplify and modify the client. The modifications I've made are to make the design of the server simpler. I've simplified the protocol (no random data types), removed unused anti-cheat packets (which are useless now anyway), etc.

### Honorable Mentions

 * [super_](http://www.rune-server.org/members/veer/) - For his contributions to my knowledge of clients, servers, and theory.
 * [Major](https://github.com/Major-) - Being an intelligent person, and a true gaming pal.
 * [Graham](http://grahamedgecombe.com/) - For Apollo as a reference for JAGGRAB and OnDemand.
 * [Blake Beaupain](https://github.com/blakeman8192) - For [RuneSource](https://www.assembla.com/code/runesource/subversion/nodes), which I used as a basis for a few things, not to mention all his contributions to servers in general.

### Credits

 * [Apollo](https://github.com/Major-/Apollo) - I took some ideas out, built on them, and, I feel, definitely improved them.
 * [RuneSource](https://www.assembla.com/code/runesource/subversion/nodes) - I used this as a crutch to wrap my head around NIO way back when.
 * [Stack Overflow](http://stackoverflow.com/questions/1593080/how-can-i-modify-my-shunting-yard-algorithm-so-it-accepts-unary-operators) - I'm not sure what question it was but the users of Stack Overflow really pointed me in the right direction.
 * [LiteratePrograms](http://en.literateprograms.org/Shunting_yard_algorithm_%28C%29) - Useful implementation of the [Shunting-yard algorithm](http://en.wikipedia.org/wiki/Shunting-yard_algorithm) used for parsing infix notational expressions outputting in postfix or Reverse Polish notation.
 * [Nikki](http://www.rune-server.org/members/nikki/) - For the thread, [Enabling RSA](http://www.rune-server.org/runescape-development/rs2-server/tutorials/305532-any-revision-enabling-rsa.html), I used it as reference to minimize the amount of debugging I have to do. I also used her key generator.