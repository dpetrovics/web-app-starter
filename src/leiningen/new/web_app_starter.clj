(ns leiningen.new.web-app-starter
  (:require [clojure.java.io :as io])
  (:use [leiningen.new.templates :only [renderer name-to-path ->files]]))

(def render (renderer "web-app-starter"))

(defn web-app-starter
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (->files data
             [".gitignore" (render "gitignore" data)]
             ["project.clj" (render "project.clj" data)]
             ["README.md" (render "README.md" data)]
             ["resources/public/css/bootstrap.css" (render "bootstrap.css" data)]
             ["resources/public/html/shared.html" (render "shared.html" data)]
             ["src/{{sanitized}}/util.clj" (render "util.clj" data)]
             ["src/{{sanitized}}/time.clj" (render "time.clj" data)]
             ["src/{{sanitized}}/couchdb.clj" (render "couchdb.clj" data)]
             ["src/{{sanitized}}/mailer.clj" (render "mailer.clj" data)]
             ["src/{{sanitized}}/server.clj" (render "server.clj" data)]
             ["src/{{sanitized}}/models/user.clj" (render "user.clj" data)]
             ["src/{{sanitized}}/views/shared.clj" (render "shared.clj" data)]
             ["src/{{sanitized}}/views/home.clj" (render "home.clj" data)]
             ["src/{{sanitized}}/views/login.clj" (render "login.clj" data)]
             )))
