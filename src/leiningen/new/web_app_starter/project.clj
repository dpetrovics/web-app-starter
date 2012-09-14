(defproject {{name}} "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0" ;;for Heroku
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [forms-bootstrap "0.3.0-SNAPSHOT"] 
                 [noir "1.3.0-beta10"]
                 [clj-time "0.4.4"]
                 [com.ashafa/clutch "0.4.0-SNAPSHOT"]
                 [org.clojars.dpetrovics/sandbar "0.4.0-SNAPSHOT"]
                 [com.googlecode.libphonenumber/libphonenumber "5.0"]
                 [commons-validator/commons-validator "1.4.0"]
                 [paddleguru/enlive "1.2.0-alpha1"]
                 [paddleguru/postal "1.7-SNAPSHOT"]
                 [paddleguru/clj-redis-session "0.0.2"]
                 [ring/ring-core "1.1.1"]] ;; do we need this?
  :profiles {:dev {:dependencies [[clj-stacktrace "0.2.4"]]}} ;;great
  ;;for debugging
  :plugins [[lein-swank "1.4.4"]]
  :main {{name}}.server)
