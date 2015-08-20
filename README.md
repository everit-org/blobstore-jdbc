# blobstore-jdbc

[Blobstore API][1] implementation that uses standard JDBC API and Blob type.

## Download

All artifacts are available on [maven-central][5].

## Database support

JDBC blobstore has been tested with the following database engines:

 - Apache Derby
 - HSQL Database Engine
 - MySQL
 - Oracle
 - PostgreSQL
 - Microsoft SQL Server

It might be possible that Blobstore works with other database engines as
well. There is a possibility to pass a _configuration_ object to the
_JdbcBlobstore_ class that allows to handle specialties of the different
database engines.

## Populating the database

SQL scripts of tested databases are available in the distributed JAR
file.

However, it is advised to use [Liquibase][4] to populate the database.
The Liquibase changelog XML is available in the distributed JAR file as well.

For OSGi environments, Liquibase changelog is also provided as
_liquibase.schema_ Bundle capability.

## Usage

    // Get the JTA aware dataSource from any place
    DataSource myDataSoure = getDataSource();
   
    // Create the blobstore instance
    Blobstore blobstore = new JdbcBlobstore(myDataSource);
    
Now that you have a blobstore instance, you can use it in the way as
it is described in the documentation of the [blobstore-api][6] project.

One of the best JTA aware datasource implementations is
[BasicManagedDataSource][7] from the [commons-dbcp][8] project.

## Notes on database engines

### PostgreSQL

 - PostgreSQL uses [LargeObject API][2] inside.
 - To be able to provide consistent data, the implementation uses
   [FOR SHARE][3] row level lock during reading the blob.
 - In case other XA resources participate in the same JTA transaction, the
   [max_prepare_transactions][10] should be set to the number of allowed
   connections.

In case you are free to choose, postgreSQL is suggested as the backend
database.

### Oracle

Oracle is the only known database that allows starting the update of a
blob while reading operations are in progress. Oracle is 3-6 times faster
than other database engines. They must know something for their money :-).

### MySQL

MySQL has two modes:

The whole content of a blob is copied into the memory of the client program
when _MysqlXADataSource.setEmulateLocators(false);_ is used. This is the
default mode.  

Blob chunks are modified with SQL statements when
_MysqlXADataSource.setEmulateLocators(true);_ is used. In this mode, the
content of the whole blob will not be held in the memory of the client
application. However, every time a tiny modification or read is made, the
whole blob is processed on the server side.

Usage of MySQL is suggested only in case of small size blobs or in case
the whole content of the blob is processed (read or written) always. In
case only chunks of the blob is processed or seeking is used frequently,
the application can get seriously slow.

### SQLServer

SQLServer holds the whole blob in the memory on the client side. Therefore
the usage of SQLServer is only suggested for small blobs.

As this blobstore uses distributed transactions, in case of using the
proprietary jdbc driver, you need to install XA support for your SQLServer.

### Derby

Based on the stress tests it seems that pessimistic lock is not working well
in Derby. It is possible that one transaction accesses the expired data that
was locked for update and than modified by other transaction. From the point
of view of Blobstore it means that the version of a blob that is modified
on multiple threads will not get incremented as many times as it was
modified.

### Hsqldb

Passed the stress tests.

### H2

H2 does not support seeking in blobs at all. Until this is implemented, H2
will not be supported by _JdbcBlobstore_.

## Cache

Wrap your _blobstore_ instance with [blobstore-cache][9] as it can improve
reading performance a lot.

[1]: https://github.com/everit-org/blobstore-api
[2]: http://www.postgresql.org/docs/9.4/static/largeobjects.html
[3]: http://www.postgresql.org/docs/9.4/static/explicit-locking.html
[4]: http://www.liquibase.org/
[5]: http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22org.everit.blobstore.jdbc%22
[6]: https://github.com/everit-org/blobstore-api
[7]: http://commons.apache.org/proper/commons-dbcp/api-2.0/org/apache/commons/dbcp2/managed/BasicManagedDataSource.html
[8]: https://commons.apache.org/proper/commons-dbcp/
[9]: https://github.com/everit-org/blobstore-cache
[10]: http://www.postgresql.org/docs/9.4/static/runtime-config-resource.html
