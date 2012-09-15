(ns {{name}}.views.home
    (:require [{{name}}.views.shared :as shared]
              [{{name}}.models.user :as user]
              [noir.session :as session])
    (:use noir.core
          net.cgrand.enlive-html
          forms-bootstrap.core
          [{{name}}.util :only (link-to)])) 
 
(defpage "/index" {:as m}
  (shared/page {}))

(defpage "/" {:as m}
  (shared/page {:main (do-> (content {:tag "p" :content
                                      (str "Welcome " (session/get :username) "!")})
                            (append (link-to "/signup" "Signup"))
                            (append " or ")
                            (append (link-to "/login" "Login")))
                :sources ["/js/reset-codes.js"]}))
