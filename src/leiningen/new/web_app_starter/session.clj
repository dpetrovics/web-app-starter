(ns {{name}}.session
  "Functions for managing the Redis session store."
  (:use [clj-redis-session.core :only (redis-store)]
        [{{name}}.time :only (days->secs)])
  (:require [clj-redis.client :as redis]))

(def cache-expiration-days
  "Number of days before a session cached in redis expires."
  30)

(defn try-local-redis
  []
  (let [local-red (redis/init)]
    (try (when (redis/ping local-red) local-red)
         (catch Exception e (println "ERROR: Couldn't connect to local Redis! Using Noir's session store.")))))

(defn try-remote-redis
  []
  (let [remote-red (redis/init {:url (System/getenv "REDISTOGO_URL")})]
    (try (when (redis/ping remote-red) remote-red)
         (catch Exception e (println "ERROR: Couldn't connect to remote Redis! Using Noir's session store.")))))

(defn setup-redis-store [local-or-remote]
  "Returns a local or remote redis store. Defaults to remote."
  (if (= local-or-remote :local)
    (do
      (println "Setting up local Redis...")
      (when-let [local-red (try-local-redis)]
        (redis-store (redis/init)
                     :expire-secs (days->secs cache-expiration-days))))
    (do
      (println "Setting up Redis To Go...")
      (when-let [remote-red (try-remote-redis)]
        (redis-store (redis/init {:url (System/getenv "REDISTOGO_URL")})
                     :expire-secs (days->secs cache-expiration-days))))))
