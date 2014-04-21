The Banker's Sequence ![project status](http://stillmaintained.com/au-phiware/bankers.png)
=====================

This project contains various functions for working with the Banker's Sequence.
The same concepts are implemented in various languages:

- C
- Java
- JavaScript
- Clojure

Background
----------

This work began with a simple observation of the bit string of successive
numbers in the Banker's Sequence, as described in section 1
[here](docs/banker.pdf). This lead to a search for a more efficient and
non-recursive algorithm, which developed into a fast method of finding
binomial coefficients.

The best description of this algorithm, and indeed problem, that I have found
is on Eric Burnett's blog [The Lowly Programmer](http://www.thelowlyprogrammer.com/2010/04/indexing-and-enumerating-subsets-of.html).
Eric gives a description of the reasoning behind the alorgithm and links to
different implementations.

Implementations
---------------

The C implementation has been designed for speed, not for a useable API.
The functions use a fixed length bit string represented by an integer:
- `choose`, an array backed calculation of binomial coefficients.
- `compute`, translates from the natural numbers to the Banker's numbers.
- `inverse`, translates from the Banker's numbers to the natural numbers.
- `next`, produces the successive number from the given Banker's number.

The equivalent functions are also provided with arbitrary precision by using
[GMP](https://gmplib.org/).

The JavaScript implementation uses `Strings` of ones and zeros (e.g. `"1000"`).
It is not considered fast or efficient.

The Java implementation is designed for power, speed and ease of use (although
it is lacking a pom file). Binomial coefficients are calculated from a DAG that
represents Pascal's Triangle, to which the algorithm leads itself to nicely.

The Clojure implementation is a complete rewrite of the Java implementation that
takes advantage of Clojure's many features/libraries, including, persistant data
structures, protocols, dynamic typing, automatic promotion to `BigInt`s,
`clojure.test.check`, etc.

