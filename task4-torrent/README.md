# Torrent client and server

## Modules

* client -- torrent client
* tracker -- torrent server
* shared -- shared stuff
* test -- integration tests

## Build

```bash
./gradlew build
```

## Run

After successful build there will be client and server jars available at:

* `client/build/libs/client-0.1.jar`
* `tracker/build/libs/tracker-0.1.jar`

To run them do the following:

```
Terminal 1
>> java -jar tracker/build/libs/tracker-0.1.jar

Terminal 2
>> java -jar client/build/libs/client-0.1.jar 1
```

As you can see you need to pass client id to client jar.

Possible commands for tracker server: start, stop.
See possible commands for client in help (write abracadabra after client prompts you or run jar without arguments).

