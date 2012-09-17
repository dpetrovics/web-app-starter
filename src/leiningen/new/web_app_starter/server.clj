(ns {{name}}.server
    (:use [{{name}}.couchdb :only (setup-couchdb)])
    (:require [noir.server :as server]
              [{{name}}.session :as session]))

(server/load-views-ns '{{name}}.views)

(defn mk-opts
  "Returns an options map suitable to pass to Noir's server/start. It
  adds in the Redis session store (either local or remote), if found,
  otherwise Noir will use its own session store."
  [mode local-or-remote]
  (let [base-opts {:mode (keyword (or mode :dev))
                   :ns '{{name}}}]
    (when-let [redis-store (session/setup-redis-store local-or-remote)]
      (assoc base-opts :session-store redis-store))))

(defn -main
  "Main entry point. Mode can be prod or dev (default), and
  local-or-remote can be local or remote (default)"
  [& [mode local-or-remote :as args]]
  (let [port (Integer. (or (System/getenv "PORT") "8080"))
        mode (or (keyword mode) :dev)
        local-or-remote (or (keyword
                             local-or-remote) :remote)]
    (println "Starting in " mode " mode.")
    (println "CouchDB and Redis are " local-or-remote)
    (setup-couchdb "{{name}}" local-or-remote)
    ;;starts the sever, with the Redis session store if found couchdb
    ;;is bound to either the local-db or remote-db, if neither is
    ;;found id does not start the server!
    (server/start port (mk-opts mode local-or-remote))))
