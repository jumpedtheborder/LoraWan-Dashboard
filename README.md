# Spring + Vue Sample Project

## What is this?

This is the most basic and complete functional sample project using:

* [Spring](http://spring.io/) as the backend
* [javax](https://en.wikipedia.org/wiki/Java_Persistence_API) for the relational mapping (no more SQL writing)
* [Vue.js](https://vuejs.org/) as the frontend
* [Vuetify](https://vuetifyjs.com/en/) Material design theme for Vue.js
* [Maven](https://maven.apache.org/) for Java configuration
* [npm](https://www.npmjs.com/) for the frontend dependency handling

The purpose of this sample project is to plug together all of the
technologies listed above together. This project only serves a sample
static page with two routes (via Vue Router) on which the `Home` view
lists posts requested from the Java backend. The Java backend contains
a simple entity (Post) with three fields that map to a database, alongside
with a simple CRUD controller.

Any changes in the frontend src folder will be automatically
recognised by the spring (see below how to run it). No need to recompile
entire Java backend, simply refresh the web browser.

## Why another Spring sample project?

I could not find a single project that had the Sping with Vue
(vue-cli 3.x) configured properly, including autoreloading. So I
have crated a one.

## TODO

* User authentication?
* Unit tests example

## Dependencies

You need the following dependencies to run this project:

* Maven installed (mvn command)
* Node Package manager installed (npm command)
* Java JDK 1.8+

and the following npm packages installed globally:

```
sudo npm install -g @vue/cli vue-template-compiler
```

## Running

Before building anything, you need to run the following command
to install local dependencies to build Vue.js.
This needs to be done only once!

```bash
cd spring-vue-sample-project/src/main/js
npm install
```

Then simply run the following command below. Please note that
running the command below will automatically start the spring
application and the web server will be served at:
<http://localhost:8080>. Also, this also builds the front end
automatically! you can skip the frontend build by adding:
`-DskipNpm=true` at the end of the `mvn` command.

```bash
cd spring-vue-sample-project/
mvn spring-boot:run
# Press Ctrl+C to stop the server gracefully
```

This however won't autoreload the frontend if any changes are made
while the server is running! You can do this by running the `mvn 
spring-boot:run` normally, but then in the new terminal run:

```bash
# While spring-boot:run is running in a different terminal
cd src/main/js
npm run watch
# Stop the watch+autobuild by pressing Ctrl+C
```

**If the changes are not visible after refreshing the web page
try disabling the cache by pressing F12 and going into the
network tab.**

This will run the watcher which will auto build the front end
every time a change has been made to any Vue files. The spring
will automatically pick it up. Simply refresh the web browser.

If you wish to compile only (do not run the web server) simply
run the command below. The jar file will be located in:
`./target/example-1.0.0.jar`

```bash
mvn package
```

## Questions and problems

Feel free to post any issue here on GitHub issues.

## Contributing

Pull request are welcome!

## License

The MIT License
