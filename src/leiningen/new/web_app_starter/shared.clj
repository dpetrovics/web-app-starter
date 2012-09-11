(ns {{name}} .views.shared
  (:use noir.core
        net.cgrand.enlive-html))

(def shared-template "public/html/shared.html")
   
(deftemplate base-layout
  shared-template
  [{:keys [title header main footer styles flash active-section sources]
    :or {title "Title Here"}}] 
  [:title]      (content title)          
  [:.main] (when main
             (content main)
             identity)
  [:footer] (content footer))
