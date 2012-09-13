(ns {{name}}.models.user
  (:use {{name}}.time
        {{name}}.util)
  (:require [noir.util.crypt :as crypt]
            [noir.session :as session]
            [noir.validation :as vali]
            [{{name}}.couchdb :as db]
            [{{name}}.mailer :as mailer]
            [sandbar.validation :as v])
  (:import  (com.google.i18n.phonenumbers PhoneNumberUtil NumberParseException)
            (org.apache.commons.validator.routines EmailValidator))) 

(defn username-key
 "Takes in a username and returns a user-id"
  [username]
  (str "{{name}}.user:" username))

(def get-user
  "If the user exists in the database, returns the user document, else
   nil."
  (comp db/get username-key))

(defn me
  "Returns the CouchDB document for the current user."
  []
  (when-let [username (session/get :username)]
    (get-user username)))

(defn get-full-name
  "Takes in a user-doc and returns the user's full name"
  [user-doc]
  (str (:first-name user-doc) " " (:last-name user-doc)))
 
(defn user-valid?
  "Accepts a map with username and password keys. If the user exists
  and the password is valid, returns the map untouched; else, adds a
  sandbar validation error. written for use with v/build-validator."
  [{:keys [username password] :as m}]
  (let [{stored-pass :password} (get-user username)]
    (if (and stored-pass (crypt/compare password stored-pass))
      m
      (v/add-validation-error m :form "Incorrect username or password."))))

(defn user-activated?
  "Checks to see if the user map contains an :activation-code. If so, then the user has not yet been activated."
  [{:keys [username] :as m}]
  (if-let [act-code (:activation-code (get-user username))]
    (v/add-validation-error m :form   "You haven't activated the account
                            yet. Click here to send a new activation
                            email.")
    m))

(def login-validator
  "Validates that username and login are both filled in, that the user
  exists, and that the password is correct."
  (v/build-validator (v/non-empty-string :username)
                     (v/non-empty-string :password)
                     :ensure
                     user-activated?
                     user-valid?))

(def forgot-password-validator
  (v/build-validator
   (v/non-empty-string :email)))

(defn login!
  "Performs login operations for the supplied username."
  [username]
  (session/put! :username username))

(defn logout!
  "Performs login operations for the logged-in user, if one exists."
  []
  (session/remove! :username))

(defn passwords-match?
  "Takes in password and password_repeat and checks if they match. If not, it adds a sandbar validation error"
  [{:keys [password password_repeat] :as user-map}]
  (if (= password password_repeat)
    user-map
    (v/add-validation-error user-map
                            :password "Password doesn't match the confirmation.")))

(defn unique-user?
  "Takes in a user-map checks to see if the username already exists in the database"
  [user-map]
  (if (get-user (:username user-map))
    (v/add-validation-error user-map :username "Username is already taken.")
    user-map))

(defn unique-email?
  "Takes in a user-map checks to see if the email already exists in the database"
  [{:keys [email] :as user-map}]
  (let [emails (map :value (db/get-view "user-view" "emails-by-username"))]
    (if (some #(= %1 email) emails)
      (v/add-validation-error user-map :email "That email is already in use!")
      user-map)))

(defn gender-selected?
  [{:keys [gender] :as user-map}]
  ;;since gender is in a radio button, it will be nil (missing from
  ;;the user-map) if the user did not selected.
  ;;put the empty? check in there in case in the future its included
  (if (or (nil? gender) (empty? gender))
    (v/add-validation-error user-map :gender "Please select a gender!")
    user-map))

;;THIS NEEDS TO BE MOVED!
(defmacro dropdown-validator
  "Generates a validator for a dropdown menu."
  [sym field msg]
  `(defn ~sym [user-map#]
     (if (seq (get user-map# ~field))
       user-map#
       (v/add-validation-error user-map# (keyword ~field) ~msg))))

(dropdown-validator birthday-day-selected? :birthday-day "Select a day!")

(dropdown-validator birthday-month-selected? :birthday-month "Select a month!")

(dropdown-validator birthday-year-selected? :birthday-year "Select a year!")

(defn valid-first-name?
  "Check for valid first name. May contain only letters,
  a hyphen or an apostrophe. No numbers or other special chars. Must
  be 2 to 20 characters long."
  [{:keys [first-name] :as
    user-map}]
  (if (re-matches #"[A-Za-z'-]{2,20}" first-name)
    user-map
    (v/add-validation-error user-map :first-name "Invalid first name.")))

(defn valid-last-name?
  "Check for valid last name. May contain only letters,
  a hyphen or an apostrophe. No numbers or other special chars. Must
  be 2 to 20 characters long."
  [{:keys [last-name] :as
    user-map}]
  (if (re-matches #"[A-Za-z'-]{2,20}" last-name)
    user-map
    (v/add-validation-error user-map :last-name "Invalid last name.")))

(defn valid-username?
  "Check to make sure the username contains only letters and numbers,
  no spaces or special characters."
  [{:keys [username] :as user-map}]
  (if (re-matches #"[a-zA-Z0-9]{3,20}" username)
    user-map
    (v/add-validation-error user-map :username "Username must contain
    only letters and numbers, and be 3-20 chars long.")))

(defn valid-password?
  "Checks to make sure the password contains only letters, numbers, and some allowable special chars. Password must be 6-20 chars long."
  [{:keys [password] :as user-map}]
  (if (re-matches #"[a-zA-Z0-9!@#$%^&*()_]{6,20}" password)
    user-map
    (v/add-validation-error user-map :password "Password must be 6-20
    chars in length, and contain valid characters.")))

(defn valid-email?
  "Check for valid email using apache commons validator."
  [{:keys [email] :as user-map}]
  (if (and (. (. EmailValidator getInstance) isValid email)
           (nil? (re-seq #"[\s]" email))) ;;no spaces
    user-map
    (v/add-validation-error user-map :email "Please enter a valid email.")))

(defn valid-phone?
  "Check for valid phone number, may contain digits, parentheses and
  hyphens. Must be at least 10 chars long."
  [{:keys [phone] :as
    user-map}]
  (if (and (re-matches #"[0-9-() ]{10,14}" (remove-spaces phone))
           (national-phone-number phone))
    user-map
    (v/add-validation-error user-map :phone "Please enter a valid phone number.")))

(def signup-validator
  "Validates all signup form fields. Also makes sure that the user
   doesn't already exist, and that the two passwords match."
  (v/build-validator
   (v/non-empty-string :first-name :last-name
                       :email :phone :username
                       :password :password_repeat)
   (valid-first-name?)
   (valid-last-name?)
   (gender-selected?)
   (valid-email?)
   (birthday-day-selected?)
   (birthday-month-selected?)
   (birthday-year-selected?)
   (valid-phone?)
   (valid-password?)
   (valid-username?)
   (unique-email?)
   (unique-user?)
   :ensure
   passwords-match?)) 

(defn update!
  [{:keys [username] :as user-map}]
  (db/update! (get-user username) user-map))

;;ACTIVATION STUFF
(defn find-by-pw-reset-code
  "Returns the user (if any) with the given password reset code."
  [pw-reset-code]
  (let [user-doc (db/get-view "user-view"
                              "users-by-pw-reset-code"
                              {} {:keys [pw-reset-code]})]
    (when (seq user-doc)
      (apply :value user-doc))))

(defn find-by-activation-code
  "Returns the user (if any) with the given activation code."
  [activation-code]
  (let [user-doc (db/get-view "user-view"
                              "users-by-activation-code"
                              {} {:keys [activation-code]})]
    (when (seq user-doc)
      (apply :value user-doc))))

(defn- activate-user-in-db!
  "Sets activation-code and activation-code-created-at to nil in the
  supplied user doc in couchdb. Not sure if theres a way to just
  remove these fields. User-map should be the user doc from couchdb
  with a valid id."
  [user-map]
  (db/update! user-map (assoc user-map :activation-code nil
                              :activation-code-created-at nil)))
(defn reset-activation-code
  "Takes in a username, and generates a new activation-code and
  activation-code-created-at time for them, updates it in the
  database."
  [username]
  (if-let [user-doc (get-user username)]
    (if (:activation-code user-doc)
      (do
        (db/update! user-doc (assoc user-doc
                               :activation-code (hexadecimalize (generate-secure-token 20))
                               :activation-code-created-at (timestamp))))
      (vali/set-error :reset-activation ["error"
                                         "Invalid request, that user
                                         has already been
                                         activated."]))
    (vali/set-error :reset-activation ["error"
                                       "Invalid request, that username does not exist."])))

(defn reset-pw-code
  "Takes in a username, and generates a new reset-pw-code and
  reset-pw-created-at time for them, updates it in the
  database."
  [username]
  (if-let [user-doc (get-user username)]
    (db/update! user-doc (assoc user-doc
                           :pw-reset-code (hexadecimalize (generate-secure-token 20))
                           :pw-reset-code-created-at (timestamp)))
    (vali/set-error :pw-reset-code ["error"
                                    "Invalid request, that username does not exist."])))

(def reset-password-validator
  (v/build-validator (v/non-empty-string :password)
                     (v/non-empty-string :password_repeat)
                     (valid-password?)
                     :ensure                     
                     passwords-match?))

(defn reset-password
  [email]
  (let [user (db/get-view "user-view" "users-by-email" {:key email})]
    (if (seq user)
      (let [user-doc (apply :value user)]
        (db/update! user-doc (assoc user-doc
                               :pw-reset-code
                               (hexadecimalize (generate-secure-token 20))
                               :pw-reset-code-created-at
                               (timestamp))))
      (vali/set-error :reset-password "That email address is not on
      PaddleGuru."))))

(defn validate-reset-code
  [reset-code]
  (if-let [user (find-by-pw-reset-code reset-code)]
    (if (code-expired? (:pw-reset-code-created-at user))
      (do
        (vali/set-error :pw-reset-code
                        (str
                         "Your password reset code has expired, please <a
                        href='/reset-pw-code?username="
                         (:username user)
                         "'>click here</a> for a new password reset email.")))    
      user)
    (vali/set-error :activation-code "That reset code does not exist.")))

(defn change-password-with-code
  "Changes password in user-doc with the given reset password
  code. This serves as a form of email validation, so we set the
  activation codes to nil as well. This applies in the case that a
  user created a new account but never activated it, then clicked
  forgot my password and reset it successfully."
  [pass code]
  (let [user-doc (find-by-pw-reset-code code)]
    (db/update! user-doc (assoc user-doc :password (crypt/encrypt pass)
                                :pw-reset-code nil
                                :pw-reset-code-created-at nil
                                :activation-code nil
                                :activation-code-created-at nil))))

(defn activate!
  "Takes in an activation-code, finds the associated user and
  activates her account. Stores errors in Noir if the activation code
  does not exist or if it is expired. Returns the user doc if
  activated, else nil."
  [activation-code] 
  (if-let [user (find-by-activation-code activation-code)]
    (if (code-expired? (:activation-code-created-at user))
      (do
        (vali/set-error :activation-code
                        (str
                         "Your activation code has expired, please <a
                        href='/reset-activation?username="
                         (:username user)
                         "'>click here</a> for a new activation email.")))    
      (activate-user-in-db! user))
    (vali/set-error :activation-code "That activation code does not exist.")))
 
(defn- make-user-doc
  "Creates a user document from a form map, suitable for storage in
  couchdb. Noir generates passwords using BCrypt, which ends up being
  very secure. BCrypt provides salts automatically."
  [{:keys [username phone] :as user-map}]
  (-> user-map
      (select-keys [:first-name :last-name :gender :email :birthday-day
                    :birthday-month :birthday-year :phone :username :password])
      (update-in [:password] crypt/encrypt)
      (update-in [:phone] national-phone-number)
      (update-in [:birthday-day] str-to-int)
      (update-in [:birthday-month] str-to-int)
      (update-in [:birthday-year] str-to-int)
      (assoc :type "user")
      (assoc :created-at (timestamp))))

(defn create!
  "Adds the user to the database with activation credentials and sends out an activation email."
  [{:keys [username email first-name] :as user-map}]
  (let [activation-code (hexadecimalize (generate-secure-token 20))
        activation-code-created-at (timestamp)
        user-doc (make-user-doc user-map)]
    (do (-> user-doc
            (assoc :activation-code activation-code)
            (assoc :activation-code-created-at activation-code-created-at)
            (db/create! :id (username-key username)))
        (mailer/email-activation-code {:email email
                                       :first-name first-name
                                       :activation-code activation-code}))))

;;PHOTOS
(defn get-photo
  [username filename]
  (db/get-attachment (get-user username) filename))

(defn has-photo? [user-doc filename]
  ((keyword (remove-spaces filename))
   (:_attachments user-doc)))

(defn add-photo!
  "Adds a photo as an attachment to the user document in the db"
  [username file filename type]
  (db/add-attachment! (get-user username) file :filename filename :mime-type type))

(defn get-all-users
 "Returns a collection of every user document on the database"
  []
  (try
    (for [{:keys [value]} (db/get-view "user-view" "users")] value)
       (catch java.lang.IllegalStateException e
         (println "failed to get all users, view probably doesnt exist"))))

(defn get-usernames-by-full-name
  "Takes in a collection of user full names and returns a collection of their user docs"
  [full-names]
  (assert (coll? full-names)
          "Full-names must be a collection")
  (db/get-view "user-view" "users-by-fullname" {} {:keys (vec full-names)}))

(defn get-usernames-for-these-full-names
  "Takes in a collection of user full names and returns a map with full names as keys and their corresponding usernames as values"
  [full-names]
  (assert (coll? full-names)
          "Full-names must be a collection")
  (reduce
   (fn[res doc]
     (assoc res
       (:key doc)
       (:value doc)))
   {} (db/get-view "user-view" "usernames-by-fullname" {} {:keys (vec full-names)})))
