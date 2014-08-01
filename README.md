logstash-gelf-subsystem
=========================

[![Build Status](https://api.travis-ci.org/mp911de/logstash-gelf-subsystem.svg)](https://travis-ci.org/mp911de/logstash-gelf-subsystem) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/biz.paluch.logging/logstash-gelf-subsystem/badge.svg)](https://maven-badges.herokuapp.com/maven-central/biz.paluch.logging/logstash-gelf-subsystem)

This is a JBossAS7/Wildfly subsystem module to provide injection/JNDI bindings of `GelfSender` and `Datenpumpe` instances. 


But wait: What is Datenpumpe?
--------------

**TL;DR;** Datenpumpe allows you to submit your own data structures to be processed by logstash, ElasticSearch and Kibana (the ELK stack) 


And the longer version:
`Datenpumpe` is a concept evolved from submitting log entries using GELF (Graylog Extended Log Format, see also https://github.com/mp911de/logstash-gelf). 
logstash-gelf was intended to allows simple submission of log events to logstash. With starting to use the ELK stack (see http://elasticsearch.org) you will
explore the possibilities of centralized event and data management. This causes the need to be able to submit any data to your logstash for later resarch and
statistics gathering. `Datenpumpe` takes proven working components and allows you to submit your own data strucures. This is useful for event processing of bussiness and
technical events.

Example
-------------

```java
public class MyServlet extends HttpServlet {

    @Resource(mappedName = "jndi:/jboss/datenpumpe")
    public Datenpumpe datenpumpe;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)  {
        
        Map<String, Object> message = new HashMap<>;
        message.put("uri", req.getRequestUri());
        message.put("resource", "MyServlet");
        message.put("event", "access");
        
        datenpumpe.submit(message);
    }
}
```

Or more sophisticated (using reflection to retrieve the fields from *your* model):

```java
@Stateless
public class MyEjb {

    @Resource(mappedName = "jndi:/jboss/datenpumpe")
    public Datenpumpe datenpumpe;
    
    public void shoppingCartOrdered(ShoppingCart cart) {
        datenpumpe.submit(cart);
    }
}

public class ShoppingCart{

    private String cartId;
    private double amount;
    private String customerId;
    
    public String getCartId(){
        return cartId;
    }
    
    public double getAmount(){
        return amount;
    }
    
    public String getCustomerId(){
        return customerId;
    }
}
```

This results in a Gelf message like:

```json
{ "timestamp": "1406797244.645",
  "facility": "logstash-gelf", 
  "_cartId": "the cart id", 
  "_amount": 9.27,
  "_customerId": "the customer id" }
```

How to get
--------------

JBoss AS7 and Wildfly modules are not binary compatible, therefore you need to download the right library. 

Maven Repo: http://oss.sonatype.org/content/repositories/snapshots/

JBoss AS7 Module Download:

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <artifactId>logstash-gelf-subsystem</artifactId>
        <version>7.0.0-SNAPSHOT</version>
        <classifier>module</classifier>
    </dependency>

https://oss.sonatype.org/content/repositories/snapshots/biz/paluch/logging/logstash-gelf-subsystem/7.0-SNAPSHOT/logstash-gelf-subsystem-7.0-20140801.182946-1-module.zip

Wildfly (AS8) Module Download:

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <artifactId>logstash-gelf-subsystem</artifactId>
        <version>8.0.0-SNAPSHOT</version>
        <classifier>module</classifier>
    </dependency>

How to install
--------------

Download the zipped module and drop the contents of the zip-file (without the top-level directory logstash-gelf-subsystem-(version)) 
into your JBoss' module directory (either below modules/system/layers/base, a new layer or below modules/ directly). The module zip contains also the
dependency to logstash-gelf, so you don't need to download it separately.

Activating the subsystem
--------------
Add following lines to your JBoss config

In the `extensions` section (below server/extensions): 

```xml
    <extension module="biz.paluch.logging.logstash-gelf-subsystem"/>
```

And anywhere in the `profile` secion (below server/profile):
```xml
    <subsystem xmlns="urn:biz.paluch.logging:logstash-gelf-subsystem:1.0">
    </subsystem>
```

Afterwards you can add Datenpumpe or GelfSender instances.

<a name="cli"/>CLI Configuration
--------------
Adding a new Datenpumpe:

    /subsystem=logstash-gelf-subsystem/datenpumpe="jndi:/jboss/datenpumpe"/:add(jndi-name="jndi:/jboss/datenpumpe", host="udp:logstash-host", port=12201)

Adding a new GelfSender:

    /subsystem=logstash-gelf-subsystem/sender="jndi:/jboss/sender"/:add(jndi-name="jndi:/jboss/sender", host="udp:logstash-host", port=12201)

<a name="xml"/>XML Configuration
--------------
Adding a new Datenpumpe:

```xml
<subsystem xmlns="urn:biz.paluch.logging:logstash-gelf-subsystem:1.0">
    <datenpumpe host="udp:logstash-host" port="12201" jndi-name="jndi:/jboss/datenpumpe" />
</subsystem>
```

Adding a new GelfSender:

```xml
<subsystem xmlns="urn:biz.paluch.logging:logstash-gelf-subsystem:1.0">
    <sender host="udp:logstash-host" port="12201" jndi-name="jndi:/jboss/sender" />
</subsystem>
```

Properties
---------------
* `host`: Hostname/IP-Address of the Logstash or Redis Host
    * tcp:(the host) for TCP, e.g. tcp:127.0.0.1 or tcp:some.host.com
    * udp:(the host) for UDP, e.g. udp:127.0.0.1 or udp:some.host.com
    * [redis](#redis)://\[:REDISDB_PASSWORD@\]REDISDB_HOST:REDISDB_PORT/REDISDB_NUMBER#REDISDB_LISTNAME , e.g. redis://:donttrustme@127.0.0.1:6379/0#myloglist or if no password needed redis://127.0.0.1:6379/0#myloglist
    * (the host) for UDP, e.g. 127.0.0.1 or some.host.com
* `port`: Port, default 12201
* `jndi-name`: JNDI Name of the binding

<a name="redis"/>Notes on redis Connection
--------------
 * IMPORTANT: for getting your logstash config right it is vital to know that we do LPUSH (list push and not channel method)
 * The redis connection is done through jedis (https://github.com/xetorthio/jedis)
 * The Url used as connection property is a java.net.URI , therefore it can have all nine components. we use only the following:
   * scheme    (fixed: redis, directly used to determine the to be used sender class)
   * user-info (variable: only the password part is used since redis doesnt have users, indirectly used from jedis)
   * host      (variable: the host your redis db runs on, indirectly used from jedis)
   * port      (variable: the port your redis db runs on, indirectly used from jedis)
   * path      (variable: only numbers - your redis db number, indirectly used from jedis)
   * fragment  (variable: the listname we push the log messages via LPUSH, directly used)

License
-------
* [The MIT License (MIT)] (http://opensource.org/licenses/MIT)

Contributing
-------
Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. 
Create Github tickets for bugs and new features and comment on the ones that you are interested in.
