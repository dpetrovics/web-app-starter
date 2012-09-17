## Web-App-Starter

A 'starter kit' for developing web apps in Clojure. It creates a project skeleton that uses Noir, CouchDB, Enlive, Forms-Bootstrap, and email authentication inspired by Noir Auth App. Meant to be hosted on Heroku with Cloudant for CouchDB and RedisToGo for the key value store. It might be over-configured for many users, but it gets you up and going very quickly if you want to use the above libraries anyways.

## Getting Started

1. lein new web-app-starter PROJECTNAME
2. In your bash profile, set the following variables: 
	REDISTOGO_URL   (used in session.clj)
	CLOUDANT_URL  (used in couchdb.clj)
	LOCAL_COUCH   (used in couchdb.clj)
	PORT  (defaults to 8080)
	If you want different names for these variables then be sure to change them in the code. REDISTOGO_URL and CLOUDANT_URL are created automatically be heroku when you add the RedisToGo and Cloudant addons to your app. Read [this article](https://devcenter.heroku.com/articles/config-vars) for information on accessing or changing these config vars.
3. lein swank, go to couchdb.clj, run (setup-couchdb dbname :remote) ;;or :local
4. still in couchdb.clj, run (setup-views)
5. in mailer.clj fix the send-email! fn with your email credentials, and set the site-url.

## Generated Files

General:

* couchdb.clj
* mailer.clj
* server.clj
* session.clj
* time.clj
* util.clj

Models:

* user.clj

Views:

* home.clj 
* shared.clj 
* login.clj

Other:

* gitignore
* README.md
* project.clj

Resources:

* shared.html
* bootstrap.css (v 2.1)
* bootstrap-alert.js
* reset-codes.js


## CouchDB Views
The template makes the following views available to you. Remember to run (setup-views) in couchdb.clj once you have configured your CouchDB credentials. These views are used in login, activation, password reseting, etc.

1. users
2. emails-by-username
3. users-by-fullname
4. users-by-email
5. users-by-activation-code
6. users-by-pw-reset-code


## To Do
refactor if necessary, esp login.clj
add documentation! 'lein doc' to check.
Project deps - move away from using the paddleguru ones if not necessary.

## License

Copyright (C) David Petrovics

Distributed under the Eclipse Public License, the same as Clojure.
