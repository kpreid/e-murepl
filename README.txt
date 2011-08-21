MUREPL for E
------------

MUREPL is a framework and application for providing E REPLs which can be used by multiple people over networks. It provides for dynamically creating REPLs as needed (with separate environments), each in a separate vat, so that they may be killed if they become hung or use excessive resources.

Future plans include persistence, asynchronous output, and per-user storage to support real encapsulation.

Building MUREPL
---------------

While the core is pure E, MUREPL includes Java code as part of most of its transports. The build system has been written using the 'redo' tool, which may be found at <https://github.com/apenwarr/redo/>. If you cannot obtain redo, then simply consider the .do files as shell scripts and replace the "redo-ifchange" dependency links with invoking the subsidiary .do files directly.

The HTTP transport depends on:
  * the Jetty HTTP server <http://jetty.codehaus.org/jetty/> version 6, which
    should be downloaded and unpacked into deps/jetty/.
  * jQuery, which should be placed at deps/jquery.js.

Running
-------

We need better ways to load libraries in E. That said, the currently available way to run MUREPL is the script "devrun", which is essentially equivalent to "devrune" (the command for running an E built from source) with the appropriate classpath options. Therefore:

  devrun murepl.e

will start MUREPL with the HTTP transport, such that you can use it at:

  http://localhost:8012/repl

Command-line options or a configuration file to configure transports will be added in the future.

Transports
----------

The connection between the REPL manager and a network service is managed by "transport" objects. Currently, the only existant transport is HTTP; IRC will be coming soon.

The HTTP transport is defined as a Java servlet which is loaded into an instance of Jetty. No attempt has been made to support starting with a servlet container and then loading E into it, so the HTTP server will necessarily be a separate process. Therefore, to virtual host this server it must be placed behind a reverse-proxy.