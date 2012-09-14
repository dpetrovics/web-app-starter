(ns {{name}}.views.home
    (:require [{{name}}.views.shared :as shared]
              [{{name}}.models.user :as user])
    (:use noir.core
          net.cgrand.enlive-html
          forms-bootstrap.core)) 
 
(defpage "/index" {:as m}
  (shared/page {}))

(defpage "/" {:as m}
  (shared/page {:main (content "Welcome!")}))
