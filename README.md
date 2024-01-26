# nml

[![ci](https://github.com/maddenp/nml/actions/workflows/ci.yml/badge.svg)](https://github.com/maddenp/nml/actions/workflows/ci.yml)

A query/modify utility for Fortran namelists

### Build

To create an executable uberjar:

1. [Install Clojure](https://clojure.org/guides/install_clojure) if you do not already have it. You can use the installer's `--prefix` option to specify any installation location that works for you.
2. Add your Clojure installation's `bin/` directory to your `PATH` and ensure that `clj --help` runs successfully.
3. Run `make uberjar`.

You should now be able to run `java -jar target/nml.jar --help` for a usage synopsis.

Alternatively, you may leverage [GraalVM](https://www.graalvm.org/) to build a native executable:

1. Download and extract the [GraalVM release](https://github.com/graalvm/graalvm-ce-builds/releases) matching your OS and Java version. (A successful build was last done with GraalVM Community Edition 22.3.3 on Linux with Java 11.)
2. Export environment variable `GRAALVM` pointing to the extracted directory's root.
3. Run `$GRAALVM/bin/gu install native-image` to install GraalVM's `native-image` tool.
4. Run `make native`. This may take some time.

You should now have an `nml` native executable in this directory.

### Test

Run `make test`.

The Fortran program `test/nml_test.f90` may be used to verify the validity of the test namelist file `test/nl.in`. Compile and run like e.g.

``` bash
$ gfortran -g nml_test.f90 && ./a.out
```

### Run

Running either `java -jar target/nml.jar`, or the `nml` native executable, with the `--help` flag:

```
Usage: nml [options]

Options:

  -c, --create      Create new namelist file
  -e, --edit file   Edit file (instead of '-i file -o file')
  -f, --format fmt  Output in format 'fmt' (default: namelist)
  -g, --get n:k     Get value of key 'k' in namelist 'n'
  -h, --help        Show usage information
  -i, --in file     Input file (default: stdin)
  -k, --keep-order  Keep namelists in original order
  -n, --no-prefix   Report values without 'namelist:key=' prefix
  -o, --out file    Output file (default: stdout)
  -s, --set n:k=v   Set value of key 'k' in namelist 'n' to 'v'
  -v, --version     Show version information

Valid output formats are: bash, json, ksh, namelist
```

### Examples

Assume that the contents of a file `nl` are as follows:

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

By default, `nml` reads from stdin and writes to stdout and, with no command-line options specified, prints a simplified, sorted version of the input:

```
% cat nl | nml
&a
  r=2.2e8
  s='Hello World'
/
&b
  c=(3.142,2.718)
  i=88
  l=t
/
```

Note that `nml` normalizes many formatting options:

- Whitespace and comments are removed.
- Non-string text is presented in lower-case.
- Key-value pairs are printed one-per-line without comma separators.
- Logical values are represented in their simplest form.

You may supply the `-k/--keep-order` flag to have `nml` output namelists in the same order in which they were read.

The `--in` and `--out` options can be used to specify input and output files, respectively.

##### Querying

To get values:

```
% nml --in nl --get a:r --get b:i
a:r=2.2e8
b:i=88
```

To print only values, without `namelist:key=` prefixes:

```
% nml --in nl --no-prefix --get b:i --get a:r
88
2.2e8
```

Note that values are printed in the order they were requested on the command line.

An obvious application is to use `nml` to insert namelist settings in scripts:

```
% cat say.sh
#!/bin/sh
echo "The value of i is $(nml --in nl --no-prefix --get b:i)"

% ./say.sh
The value of i is 88
```

If any requested namelists or keys are not found, `nml` reports the first ungettable value and exits with error status:

```
% nml --in nl --get a:r --get b:x || echo "FAIL"
nml: b:x not found
FAIL
```

##### Modifying

To set (or add) values:

```
% nml --in nl --set a:s="'Hi'" --set b:x=.false. --set c:z=99
&a
  r=2.2e8
  s='Hi'
/
&b
  c=(3.142,2.718)
  i=88
  l=t
  x=f
/
&c
  z=99
/
```

Note that get and set commands may not be mixed in a single `nml` invocation.

A file may be edited in place with the `--edit` command (equivalent, in this case, to `--in nl --out nl`):

```
% nml --in nl --get a:s
a:s='Hello World'

% nml --edit nl --set a:s="'Hi'"

% nml --in nl --get a:s
a:s='Hi'
```

To create a new namelist file from scratch (i.e. without starting with an input file):

```
% rm -f new

% nml --create --out new --set a:x=77 --set a:y=88

% cat new
&a
  x=77
  y=88
/
```

##### Output Format

In addition to the default Fortran namelist output format, `nml` can output namelist data as a bash/ksh function, or as JSON data.

The bash/ksh function allows fast lookups in shell scripts after a single `nml` invocation, via the defined `nmlquery` shell function.

```
% eval "$(nml --in nl --format bash)"

% nmlquery a s
'Hi'

% nmlquery b c
(3.142,2.718)
```

Example JSON output:

```
% nml --in nl --format json
{"b":{"l":true, "c":"(3.142,2.718)", "i":88},
 "a":{"r":2.2E8, "s":"Hello World"}}
```

Note that several valid Fortran namelist values, e.g. `r*c` repeat values like `10*'c'`, or complex literals like `(1.2,3.4)` are represented as strings in JSON for lack of native support.

### Limitations

##### Standards support

`nml` tries to conform to [Fortran 2008](https://gcc.gnu.org/wiki/GFortranStandards#Fortran_2008) section 10.11 "Namelist formatting", though no explicit attempt has been made to support object-oriented constructs. The standard permits all sorts of nonsense that ought, for sanity's sake, to be prohibited; compilers make matters worse by apparently allowing further, non-conformant nonsense. I would be grateful for bug reports describing non-conformant `nml` behavior, but please confirm that your namelist is conformant before filing a ticket or PR.

##### Repeated namelists

The Fortran standard allows namelist files like this:

```
&nl v = 77 /
&nl v = 88 /
```

With the namelist file open, a first Fortran `read` statement would set `v` to `77`, and the second would set it to `88`. This use case is not supported by `nml`. Rather, the last of a set of same-named namelists will override previous ones, and `nml` will output a single `nl` namelist with the final values.

Currently, `nml` does not (TODO: but should) correctly support this variant of the above:

```
&nl v = 77 /
&nl w = 88 /
```

The correct behavior would be to merge the contents of the two same-named namelists, providing values for both `v` and `w`.

##### Semicolon as value separator

The use of the semicolon as a value separator (in COMMA decimal edit mode, rather than POINT decimal edit mode -- see Fortran 2008 standard section 10.10.2), is not currently supported.

### Thanks

Thanks to Mark Engelberg for the wonderful [Instaparse](https://github.com/Engelberg/instaparse), on which `nml` is based.
