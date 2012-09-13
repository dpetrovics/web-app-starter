# {{name}}

A website written with Noir, CouchDB, Enlive, Forms-Bootstrap, and email authentication inspired by Noir Auth App. Meant to be hosted on Heroku with Cloudant for CouchDB and RedisToGo for the key value store.
googlephone numbers
commons validator for email validation 

## Created Files

fixes:
in form-helper, on-failure, should we pass the action to render?


local and remote modes for couchdb, redis (install both of these locally)
REDISTOGO_URL = 
CLOUDANT_URL = 
LOCAL_COUCH = 
NOIR_PORT = defaults to 8080

Redis session stuff:
https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/middleware/session/store.clj defines the protocol
https://github.com/wuzhe/clj-redis-session/blob/master/src/clj_redis_session/core.clj
https://github.com/mmcgrana/clj-redis/blob/master/src/clj_redis/client.clj


ring middleware reload?

server.clj - all set
couchdb.clj - put in the cloudant url, or make this an environmental variable. Make local mode available. Fix remote-db to just {{name}}-db
time.clj - set
util.clj - set
mailer.clj - done
session.clj - done

user.clj - need to split this up into several other namespaces

gitignore - done

bootstrap.css - done (minified?) v 2.1

home.clj - compare to pg
shared.clj - compare to pg
shared.html - keep footer?

login.clj - check to make sure its ok

{{name}}.css - ??

codox ?


Project deps - move away from using the paddleguru ones if not necessary.

## Usage

```bash
lein deps
lein run
```

## License

Copyright (C) 2012 FIXME

Distributed under the Eclipse Public License, the same as Clojure.