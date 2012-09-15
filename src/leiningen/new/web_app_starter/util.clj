(ns {{name}}.util
    (:require [net.cgrand.enlive-html :as html]
              [clojure.string :as s])
    (:import [java.net URI]
             (com.google.i18n.phonenumbers PhoneNumberUtil NumberParseException)
             java.security.SecureRandom))

;;GENERAL
(defn insert
  "Returns a new vector with value inserted at the given postion."
  [v pos val]
  (apply conj (subvec v 0 pos) val (subvec v pos)))

(defn spread
  "Returns the difference between the max and min numbers in the
  supplied collection."
  [xs]
  (- (apply max xs)
     (apply min xs)))

(defn parse-url
  "Parses a Heroku URL and returns a 3-vector containing host,
  username and password."
  [path]
  (let [^URI uri (URI. path)
        host (.getHost uri)
        port (-> (.getAuthority uri) (s/split #"@") second)
        [uname password] (-> (.getUserInfo uri) (s/split #":"))]
    [host uname password]))

(def div-rem
  "Takes two numbers and returns a 2-vector of [quotient, remainder]"
  (juxt quot mod))

;;ENLIVE HELPERS
(defn nodes-to-html
  [n]
  (apply str (html/emit* n)))

(defn onclick-str
  "Takes in an onclick-fn name and some or no arguments, and retruns a
  string suitable for use in html. Ex: (onclick-str 'somejs' '('one'
  'two')) => 'somejs(one two)'"
  [onclick-fn onclick-args]
  (let [a (str onclick-fn "(")
        b (when onclick-args
            (apply str (interpose " " onclick-args)))
        c (str a b ")")]
    c))

(defn link-to
  "Returns a map representation link of an anchor link to `ref` with
  the supplied content. Suitable for use in enlive templating calls."
  [ref content & [onclick & oc-args]]
  (let [base-map {:tag :a
                  :attrs {:href ref}    
                  :content [content]}]
    (if onclick
      (assoc base-map :attrs {:href ref
                              :onclick (onclick-str onclick oc-args)})
      base-map)))

(defmacro maybe-substitute
  ([expr] `(if-let [x# ~expr]
             (html/substitute x#)
             identity))
  ([expr & exprs] `(maybe-substitute (or ~expr ~@exprs))))

(defmacro maybe-content
  ([expr] `(if-let [x# ~expr] (html/content x#) identity))
  ([expr & exprs] `(maybe-content (or ~expr ~@exprs))))

(defn insert-break-tags [text]
 (s/replace text "\r\n" "<br/>"))

(defn insert-html-entities [text]
  (-> (s/replace text " " "&nbsp;")
      (s/replace "\r\n" "<br/>")))

;;STRING HELPERS
(defn remove-spaces
  "Returns a new string with spaces removed."
  [input]
  (s/replace input " " ""))

(defn remove-special-chars
  "Returns a new string with special characters removed."
  [input]
  (s/replace input #"[^a-zA-Z0-9]+" "") )

(defn convert-str-to-keyword
  "Returns a keyword with spaces replaced by hyphens, and made lower case."
  [text]
  (keyword (s/lower-case (s/replace text " " "-"))))

(defn str-to-int
  "Converts string to ints, checks for empty string"
  [s]
  (if (= "" s)
    0
    (Integer. s)))

;;EMAIL AUTHENTICATION
;;(taken from  noir-auth-app)
;;ex: (hexadecimalize (generate-secure-token 20))
(defn generate-secure-token
  "Returns a byte array of the given size with random numbers in each
  index."
  [size]
  (let [seed (byte-array size)]
    (.nextBytes (SecureRandom/getInstance "SHA1PRNG") seed)
    seed))

(defn hexadecimalize
  "Converts byte array to hex string" [a-byte-array]
  (->> (map #(format "%02X" %) a-byte-array)
      (apply str)
      (s/lower-case)))

;;VALIDATION
(defn national-phone-number
  "Takes in a phone number string, which doesnt have to be all
  numbers, and returns a long of the phone number using Google's
  libphonenumber."
  [phone]
  (let [phoneUtil (. PhoneNumberUtil getInstance)]
    (try (. (. phoneUtil parse phone "US") getNationalNumber)
         (catch NumberParseException e
           (println "Invalid phone number!")))))
