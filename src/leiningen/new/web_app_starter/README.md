# {{name}}

A website written with Noir, CouchDB, Enlive, Forms-Bootstrap, and email authentication inspired by Noir Auth App. Meant to be hosted on Heroku with Cloudant for CouchDB and RedisToGo for the key value store.
googlephone numbers
commons validator for email validation 

## Created Files
server.clj - all set
couchdb.clj - put in the cloudant url, or make this an environmental variable. Make local mode available. 
time.clj - set
util.clj - set
mailer.clj - done

user.clj - need to split this up into several other namespaces

gitignore - done

bootstrap.css - done (minified?) v 2.1

home.clj - compare to pg
shared.clj - compare to pg
shared.html - keep footer?

login.clj - check to make sure its ok

{{name}}.css - ??

codox ?


## Usage

```bash
lein deps
lein run
```

## License

Copyright (C) 2012 FIXME

Distributed under the Eclipse Public License, the same as Clojure.