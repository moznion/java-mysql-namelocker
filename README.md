mysql-namelocker [![Build Status](https://travis-ci.org/moznion/java-mysql-namelocker.svg)](https://travis-ci.org/moznion/java-mysql-namelocker)
==

Provides MySQL name based lock for Java 7 or later.

Synopsis
--

```java
import java.sql.Connection;
import java.sql.DriverManager;
import moznion.net.mysql.namelocker.NameLocker;

Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/table_name", "", "");
try (NameLocker locker = new NameLocker(connection, "lock-name", 10)) {
    /*
     * do something here
     */
} // lock will be released automatically when it escapes this scope
```

Description
--

This package is port of [Mysql::NameLocker](https://metacpan.org/pod/Mysql::NameLocker) from Perl to Java.

Note
--

Obtained lock will be release automatically when it escapes scope of try-with-resources, because `NameLocker` implements `AutoCloseable`.

See Also
--

- [Mysql::NameLocker](https://metacpan.org/pod/Mysql::NameLocker)

License
--

```
The MIT License (MIT)
Copyright © 2014 moznion, http://moznion.net/ <moznion@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

