(ns {{name}}.views.shared
  (:use noir.core
        net.cgrand.enlive-html)
  (:require [noir.session :as session]))

(def shared-template "public/html/shared.html")

(def jquery-js "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js")
(def jquery-ui-js "http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/jquery-ui.min.js")
(def jquery-css "http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css")

(defn link-to    
  "Returns a map representation link of an anchor link to `ref` with
  the supplied content. Suitable for use in enlive templating calls."
  [ref content] 
  {:tag :a
   :attrs {:href ref}    
   :content [content]})

(deftemplate base-layout
  shared-template
  [{:keys [title header main footer styles flash active-section sources]
    :or {title "Title Here"}}] 
  [:title]      (content title)          
  [:.main] (if main
             main
             identity)
  [:footer] (content footer))

(defn page             
  [{:keys [title header main footer styles flash active-section sources]
    :or {flash (session/flash-get :flash)}}]
  (base-layout {:header header  
                :flash flash
                :active-section active-section
                :main  main  
                :styles [jquery-css]
                :sources (into
                          [jquery-js jquery-ui-js]
                          sources)}))
