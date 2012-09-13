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

(defpage "/forms" {:as m}
  (shared/page {:main (content
                       (make-form
                        :legend "Form Test"
                        :action "someaction"
                        :submit-label "Send it!"
                        :cancel-link "/"
                        :fields [{:type "text"
                                  :name "nickname"
                                  :label "Nick Name"
                                  :size "input-large"} 
                                 {:type "password"
                                  :name "password"
                                  :label "Password"}
                                 {:type "text"
                                  :name "city"
                                  :label "City"
                                  :placeholder "Placeholder!"}
                                 {:type "text-area"
                                  :name "description"
                                  :label "Favorite Quote"}
                                 {:type "select"
                                  :name "colors"
                                  :label "Favorite Color"
                                  :inputs [["blue" "Blue"]
                                           ["red" "Red"]
                                           ["yellow" "Yellow"]]}
                                 {:type "radio"
                                  :name "cars"
                                  :label "Favorite Car"
                                  :inputs [["honda" "Honda"]
                                           ["toyota" "Toyota"]
                                           ["chevy" "Chevy"]]}
                                 {:type "checkbox" :name "languages" :label "Languages"
                                  :inputs [["german" "German"]
                                           ["french" "French"]
                                           ["english" "English"]]}
                                 {:type "file-input"
                                  :name "afile"
                                  :label "Choose a pic"}]))}))

(defpage "/db" {:as m}
  (shared/page {:main (content "Put a couchdb test here")}))
