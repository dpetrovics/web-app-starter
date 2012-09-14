(ns {{name}}.couchdb
    (:refer-clojure :exclude (get))
    (:require [com.ashafa.clutch :as c]
              [cemerick.url :as url]))

(def my-db (atom nil))

(defn remote-db [db-name]
  (try (c/get-database (str (System/getenv "CLOUDANT_URL")
                            (str "/" db-name "/")))
       (catch Exception e (println "ERROR: "(.getMessage e)))))

(defn local-db [db-name]
  (try
    (c/get-database (str (System/getenv "COUCHDB_LOCAL")
                         (str "/" db-name "/")))
    (catch Exception e (println "ERROR: "(.getMessage e)))))

(defn setup-couchdb [db-name local-or-remote]
  (println "Setting up couchdb...")
  (if (= local-or-remote :local)
    (swap! my-db (constantly (local-db db-name)))
    (swap! my-db (constantly (remote-db db-name)))))
 
(defn get-view [design-name map-name & [query-params-map post-data-map]]
  (println "getting view: " design-name " " map-name)
  (c/get-view @my-db design-name map-name query-params-map post-data-map)) 


(defn get-all
  "Get all objects from CouchDB."
  [& {:keys [include-docs]}]
  (-> @my-db
      (c/all-documents {:include_docs (boolean include-docs)})))

 (defn get
  "Query CouchDB for the supplied key."
  [id]
  (println "DB/GET: " id)
  (c/get-document @my-db (url/url-encode id)))

(defn create!
  "Add the supplied object to the database; no validations are
  necessary. Call with any of the following:
   - map of data
   - data and an id
   - data, vector of attachments
   - data, id, vector of attachments"
  [document & opts]
;;  (println "doc: " document " opts: " opts)
  (apply c/put-document @my-db document opts))

(defn delete!
  "Takes a document and deletes it from the database."
  [document]
  (c/delete-document @my-db document)) 

(defn update!
  "Takes any of the following sets of arguments:

   - A single document
   - A document and another map to merge into it
   - A document, a group of keys and a function, like update-in

  update! acts on the CouchDB document with the key of the first map
  provided."
  [& args]
  (apply c/update-document @my-db args))

;;ATTACHMENTS
(defn get-attachment
  "Gets the attchment from the document in the db"
  [document file]
  (c/get-attachment @my-db document file))

(defn add-attachment!
  "Add the attachment to the document database; no validations are
  necessary. Call with the following:
   - regatta document
   - file for attachment ie 'resources/imgs/logo.png')
   - optional :filename filename :mime-type 'image/jpeg' "
  [document file & opts]
  (apply c/put-attachment @my-db document file opts))

(defn delete-attachment!
  "Deletes the attachment from the document in the database; Call with the following:
   - regatta document
   - file for attachment ie 'logo.png')"
  [document file]
  (c/delete-attachment @my-db document file))

;;BULK-DOCUMENT FUNCTIONS
(defn get-bulk
 "Takes in a collection of couchdb ids and fetches the corresponding documents in one request"
  [ids]
  (println "Get-Bulk: " (count ids) " things from the db")
  (if ids
    (c/all-documents @my-db
                     {:include_docs true}
                     {:keys ids})
    `()))

(defn bulk-update
  "Takes in a collection of maps, each of which must have and :_id and a :_rev field corresponding to the document in couchdb. It then updates those documents with whatever is in the input maps"
  [docs]
  (c/bulk-update @my-db docs))

(defn delete-bulk!
 "Takes in a collection of documents, each of which must have and :_id and :_rev field, and deletes them from couchdb in one request"
  [docs]
  (c/bulk-update @my-db
                 (map (fn [doc] (assoc doc :_deleted true)) docs)))

;;CouchDB Views
(defn save-user-view [design-name] 
  (c/save-view @my-db design-name
               (c/view-server-fns :javascript
                                  {:users
                                   {:map
                                    "function(doc) {
      if (doc.type == 'user') {
          emit(doc['username'], doc); }} "}
                                   :emails-by-username
                                   {:map
                                    "function(doc) {
      if (doc.type == 'user') {
          emit(doc['username'], doc['email']); }}"}
                                   :users-by-email
                                   {:map
                                    "function(doc) {
      if (doc.type == 'user') {
          emit(doc['email'], doc); }}"
                                    }
                                   :users-by-activation-code
                                   {:map
                                    "function(doc) {
      if (doc.type == 'user') {
          emit(doc['activation-code'], doc); }}"
                                    }
                                   :users-by-pw-reset-code
                                   {:map
                                    "function(doc) {
      if (doc.type == 'user') {
          emit(doc['pw-reset-code'], doc); }}"
                                    }
                                   :usernames-by-fullname
                                   {:map
                                    "function(doc) {
      if (doc.type == 'user') {
          emit(doc['first-name'] + ' ' + doc['last-name'], doc['username']); }} "}
                                   :users-by-fullname
                                   {:map
                                    "function(doc) {
      if (doc.type == 'user') {
          emit(doc['first-name'] + ' ' + doc['last-name'], doc); }} "}})))

(defn setup-views
  []
  (do
    (save-user-view "user-view")))
