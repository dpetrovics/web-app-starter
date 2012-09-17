(ns leiningen.new.web-app-starter
  "Generate a web-app-starter project."
  (:use [leiningen.new.templates :only [renderer year project-name
                                        sanitize-ns name-to-path ->files]]))

(defn web-app-starter
  "FIXME: write documentation"
  [name]
  (let [render (renderer "web-app-starter")
        data {:raw-name name
              :name (project-name name)
              :namespace (sanitize-ns name)
              :sanitized (name-to-path name)
              :year (year)}]
    (println "Generating a project called" name "based on the 'web-app-starter' template.")
    (->files data
             ["project.clj" (render "project.clj" data)]
             ;;             [".gitignore" (render "gitignore" data)]
             ["README.md" (render "README.md" data)]
             ["Procfile" (render "Procfile" data)]
             ["resources/public/css/bootstrap.css" (render "bootstrap.css" data)]
             ["resources/public/html/shared.html" (render "shared.html" data)]
             ["resources/public/js/bootstrap-alert.js" (render "bootstrap-alert.js" data)]
             ["resources/public/js/reset-codes.js" (render "reset-codes.js" data)]
             ["src/{{sanitized}}/util.clj" (render "util.clj" data)]
             ["src/{{sanitized}}/time.clj" (render "time.clj" data)]
             ["src/{{sanitized}}/couchdb.clj" (render "couchdb.clj" data)]
             ["src/{{sanitized}}/mailer.clj" (render "mailer.clj" data)]
             ["src/{{sanitized}}/server.clj" (render "server.clj" data)]
             ["src/{{sanitized}}/session.clj" (render "session.clj" data)]
             ["src/{{sanitized}}/models/user.clj" (render "user.clj" data)]
             ["src/{{sanitized}}/views/shared.clj" (render "shared.clj" data)]
             ["src/{{sanitized}}/views/home.clj" (render "home.clj" data)]
             ["src/{{sanitized}}/views/login.clj" (render "login.clj" data)]
             )))
