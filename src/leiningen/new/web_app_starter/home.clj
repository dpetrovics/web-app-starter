(ns {{name}} .views.home
  (:require [{{name}} .views.shared :as shared]
            [{{name}} .models.user :as user])
  (:use noir.core
        forms-bootstrap.core)) 
 
(defpage "/index" {:as m}
  (shared/base-layout {}))

(defpage "/" {:as m}
  (shared/base-layout {:main "Welcome!"}))

(defpage "/forms" {:as m}
  (shared/base-layout {:main (make-form
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
                                        :label "Choose a pic"}])}))

(defpage "/db" {:as m}
  (shared/base-layout {:main (str "Put a couchdb test here")}))
