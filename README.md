# B-Threads

Playing with the B-Thread concept.

## Notes:

### As a concurrency model
I think that B-Threads are strictly less-powerful than the actor-model. We can think of B-Threads as actors with a particular function for changing their internal behavior that involves cycling through a pre-set list of reactions. To complete the transition, we further posit the existence of a central actor managing the requesting/blocking/alerting. The ability to send particular messages directly to particular actors is lost in this centralized idea of B-Threads. To get a smiliar effect, you must make use of globally unique keys.

I think this model could still be super useful for distributed systems, but you'd need to compile the B-Thread program into a distributed system based on the definitions of the B-Threads, essentially turning it BACK into the actor model, but with some nice synchronization properties you may not have had before.

The nice thing about this idea is that you can create your processes without really thinking about the intricacies of distributed communication, and have the system figure out which components need to talk to eachother in order to accomplish your goal. The downside is that because you don't _really_ think about _where_ these things are happening means that you could accidentally create a VERY congested system without meaning to.

### My bad implementation
From what I can tell, the ambiguity in B-Threads comes from the question of what to do in the event more than one non-blocked event is requested `(Ex: add_hot_water and add_cold_water requested by different threads, neither blocked)`.

In [this paper](https://www.wisdom.weizmann.ac.il/~/amarron/BP%20-%20CACM%20-%20Author%20version.pdf), the author calls attention the varying semantics of the multi-request situation, and cites some examples of different strategies for handling it.

Reading through the paper, these strategies seem to take one of two forms:
- Come up with some resolution function `(Ex: thread priority, random choice)` to guarantee there is always _one_ event chosen in a given moment.
- Split the universe such that each possible reality reflects a different choice that could have been made. This is either used to:
  - inform a look-ahead that can more effectively choose which runtime path the user wants `(just a sneakier, longer option #1)`
  - run all the realities in parallel with the goal of picking your favorite at the end, or analyzing the divergence in some meaningful way

I have elected to ignore the problem completely and simply have the ability to emit multiple events per timestep - the reason being that I came to this idea via [this talk](https://www.youtube.com/watch?time_continue=9&v=cXuvCMG21Ss&feature=emb_logo) which was largely pitching B-Threads as a mechanism for append-only software changes. In light of this, I want the stuff I add to make as little difference as possible to the stuff that's already there `(unless of coure I WANT it to make a difference)`. It seemed that the easiest way to do that was to eliminate the interactions between unrelated events that happen to occur at the same time by just letting them occur at the same time.

## Interaction
I need to add an input/output mechansim for interacting with the B-Thread system. I think that's going to take the form of some very basic traits that allow me to choose whether the system operates in lock-step with the I/O or not. For example:

```scala
trait Input[F[_]] {
  def pull(): F[Seq[Request]]
}

trait Output[F[_]] {
  def push(events: Seq[Event]): F[Unit]
}
```

In the simplest version, these are just queues that are being worked through as fast as they can be by separate processes.

In a slightly more involved version, the output and the input could be conjoined such that the `push()` effect doesn't complete until any requests that serve as the response to the B-Thread system's output are ready to be pulled.

Between those two versions, we can get async, less determinisitc (timing-wise) communication, which will be good for some systems, and a synchronous, time-locked version that could be good for others.

## Spinning Your Wheels

Right now there's a really cheap mechanism for avoiding endless loops - just stop processing after some pre-set number of frames. Can we do better?

### No Events Produced

In the case there are no events generated, the system is in a state that will always repeat unless outside requests are added to the system. In this case, we could simply pause execution and wait for such a request to perturb the system instead of the more `computer go brrrrrrr` situation we have now. That way we could leave the system running without having to have a cutoff.

## Refactoring

I keep having this thought that refactoring a system like this should be deceptively easy. Thinking about examples has led me to some intuitions:
1. Combining two threads feels really easy.
2. Pulling threads apart feels harder.
3. Clustering on common keys will be involved.

I really wanna work out a visual editing system for these, because it feels like a thing that will become immediately obvious once I can see it. For now, I'll keep using the whiteboard.

## Run application

```bash
sbt run
```

## Dockerize

```bash
# should create `./target/docker/stage/Dockerfile`
sbt "Docker/publishLocal"
```

## More Links:

https://medium.com/@lmatteis/b-threads-programming-in-a-way-that-allows-for-easier-changes-5d95b9fb6928

https://www.wisdom.weizmann.ac.il/~bprogram/

