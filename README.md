Brooklyn Play Framework Roll-out
======================

This project contains the Brooklyn entity for deploying and managing Play Framework applications.
It also contains a sample application showing some of the advanced Brooklyn features, which can
be deployed to your favourite cloud or simply to localhost.

### Compile

To compile, simply `mvn clean install` in the project root.


### Demo

To run the demo, either:

* Download and install the `brooklyn` CLI tool from http://brooklyncentral.github.com/ 
  (ensuring it is the right version for this project) and run in the project root:

    export BROOKLYN_CLASSPATH=target/brooklyn-playframework-0.5.0-SNAPSHOT.jar
    brooklyn launch -a brooklyn.web.play.demo.OnePlayApp -l localhost

  Alternatively to run a cluster:

    brooklyn launch -a brooklyn.web.play.demo.PlayHelloCluster -l aws-ec2:us-east-1

  Optionally target your favourite cloud, e.g. for AWS (and assuming credentials
  are configured in `~/.brooklyn/brooklyn.properties` as per instructions below):

    brooklyn launch -a brooklyn.web.play.demo.PlayHelloCluster -l aws-ec2:us-east-1

* Grab all dependencies (using maven, or in your favourite IDE) and run the static `main` in 
  `brooklyn.web.play.demo.OnePlayApp` or `PlayClusterApp`.

After a wee while, it should print out the URL of the application and of the Brooklyn console.


### Using it Yourself

Of course the only point of the demo is to show you how to run your own app;
copy the code there and adjust the constants to point to your application.

You can also attach policies, e.g. for scaling, and do more sophisticated wiring,
to stand this up alongside a Hadoop cluster or your preferred instantiation of fun.
That's what Brooklyn is really for.  See examples at  brooklyn.io  for more info!


### Cloud Setup

If running in a cloud (e.g. AWS), you'll need AWS credentials in `~/.brooklyn/brooklyn.properties`:

    brooklyn.jclouds.aws-ec2.identity=AKXXXXXXXXXXXXXXXXXX
    brooklyn.jclouds.aws-ec2.credential=secret01xxxxxxxxxxxxxxxxxxxxxxxxxxx

Most other clouds should work too, with minor variations to the code (in particular the disk setup in MyM3App),
as will fixed IP machines (bare-metal/byon).  MaaS clouds (metal-as-a-service) are in development, over at jclouds.org.


### Finally

This software is (c) 2013 Cloudsoft Corporation, released as open source under the Apache License v2.0.

Any questions drop a line to brooklyn-users@googlegroups.com !

