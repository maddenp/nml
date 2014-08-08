nml
===

A query/modify utility for Fortran namelists

###Build

Install [Leiningen](http://leiningen.org/) if you don't have it, then:

`lein uberjar`

###Run

The _nml_ wrapper script invokes _java -jar_ with the path to the Leiningen-generated _target/nml.jar_.

````
Usage: nml [options] file

Options:

  -g, --get n:k    Get value of key 'k' in namelist 'n'
  -h, --help       Show usage information
  -n, --no-prefix  Report values without 'namelist:key=' prefix
  -s, --set n:k=v  Set value of key 'k' in namelist 'n' to 'v'
  -v, --version    Show version information
````

###Examples

Assume that the file _nl_ contains the Fortran namelists:

```
&b
  L = .TRUE. ! logical value
  c=( 3.142 , 2.718)
  ! This is a comment.
  i=88
/

&a r=1.1e8, S = 'Hello World' r=2.2e8 /

This junk isn't in a namelist.
```

Invoked with no options, _nml_ prints a simplified, sorted version of the input:

```
% nml nl
&a
  r=2.2e8
  s='Hello World'
/
&b
  c=(3.142,2.718)
  i=88
  l=t
/
````

Note that _nml_ normalizes many formatting options: Whitespace and comments are removed, non-string text is converted to lower-case, key-value pairs are printed one-per-line without comma separators, etc.

To query values:

````
% nml -g a:r --get b:i nl
a:r=2.2e8
b:i=88
````

To print only values, without namelist:key prefixes:

````
% nml --no-prefix --get b:i -g a:r nl
88
2.2e8
````

Note that values are printed in the order they were requested on the command line.

An obvious use case is to use _nml_ to insert namelist settings in scripts:

```
% cat say.sh
#!/bin/sh
echo "The value of i is $(nml -n -g b:i nl)"

% ./say.sh 
The value of i is 88
````

To modify (or add) values:

````
% nml --set a:s="'Hello Yourself'" -s b:f=.false. -s c:x=99 nl
&a
  r=2.2e8
  s='Hello Yourself'
/
&b
  c=(3.142,2.718)
  f=f
  i=88
  l=t
/
&c
  x=99
/
````

Note that get and set commands may not be mixed in a single _nml_ invovation.

###Thanks

Thanks to Mark Engelberg for the wonderful [Instaparse](https://github.com/Engelberg/instaparse), on which _nml_ is based.
