# B-Threads

Playing with the B-Thread concept.

## Notes:
I think that B-Threads are strictly less-powerful than the actor-model. We can think of B-Threads as actors with a particular function for changing their internal behavior that involves cycling through a pre-set list of reactions. To complete the transition, we further posit the existence of a central actor managing the requesting/blocking/alerting. The ability to send particular messages directly to particular actors is lost in this centralized idea of B-Threads. To get a smiliar effect, you must make use of globally unique keys.

I think this model could still be super useful for distributed systems, but you'd need to compile the B-Thread program into a distributed system based on the definitions of the B-Threads, essentially turning it BACK into the actor model, but with some nice synchronization properties you may not have had before.

The nice thing about this idea is that you can create your processes without really thinking about the intricacies of distributed communication, and have the system figure out which components need to talk to eachother in order to accomplish your goal. The downside is that because you don't _really_ think about _where_ these things are happening means that you could accidentally create a VERY congested system without meaning to.

## Run application

```bash
sbt run
```

## Dockerize

```bash
# should create `./target/docker/stage/Dockerfile`
sbt "Docker/publishLocal"
```


