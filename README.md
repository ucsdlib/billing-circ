# Billing Circulation Program

The patron billing software includes the functions of processing and exporting of library circulation billing information to the campus financial systems.

## Prerequisites 

1.JDK 1.7

Make sure you are using JDK 1.7 -- running ```java -version``` should output something like:

``` 
java version "1.7.0_10"
Java(TM) SE Runtime Environment (build 1.7.0_10-b18)
Java HotSpot(TM) 64-Bit Server VM (build 23.6-b04, mixed mode)
```

2.Ant

3.PostgreSQL Database

Install PostgreSQL

```
brew install postgresql
```

Setup Billing database and user:

```
$ sudo -u postgres psql
postgres=# create user tomcat with password 'billing';
postgres=# create database billing owner tomcat;
postgres-# \q
```

4.Tomcat

Download Tomcat 7

http://tomcat.apache.org/download-70.cgi

Edit Tomcat Configuration  conf/server.xml and add to the GlobalNamingResources:

```
$ xml
    <Environment name="jdbc/billing" value="jdbc/billing" type="java.lang.String"/>
    <Resource name="jdbc/billing" auth="Container" type="javax.sql.DataSource"
        username="billing" password="XXXX" driverClassName="org.postgresql.Driver"
        url="jdbc:postgresql://localhost:3306/billing" maxActive="10" maxIdle="3"
        maxWait="5000" validationQuery="select 1" logAbandoned="true"
        removeAbandonedTimeout="60" removeAbandoned="true" testOnReturn="true"
        testOnBorrow="true"/>
```

## Usage

1.Clone the project

```
$ git clone https://github.com/ucsdlib/billing-circ.git
```

2.Open project.

```
$ cd billing-circ
```

3.Checkout develop branch.

```
$ git checkout develop
```

4.Build billing-circ.war

```
$ ant clean webapp
```

5.Deploy to Tomcat

```
$ ant local-deploy
```

## Docs

[Patron Billing Documentation](https://github.com/ucsdlib/docs/tree/master/billing-circ)

