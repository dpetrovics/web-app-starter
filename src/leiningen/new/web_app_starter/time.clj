(ns {{name}} .util.time
  (:require [clj-time.core :as time]
            [clj-time.format :as format]
            [clojure.string :as s]))

(defn timestamp
  "Returns the current timestamp, formatted using the supplied
  formatter. Call `(clj-time.format/show-formatters) to see all
  options; formatter defaults to :base-date-time-no-ms."
  ([] (timestamp :basic-date-time-no-ms))
  ([format-kwd]
     (format/unparse (format/formatters format-kwd)
                     (time/now))))

(defn days->secs [days]
  (-> days (* 24) (* 60) (* 60)))

(defn convert-to-rfc822
  "Takes in a date-time string and converts it to rfc822 format."
  [s]
  (let [newstring (format/unparse
                   (format/formatters :rfc822)
                   (format/parse s))]
    (s/replace newstring " +0000" "")))

(defn timestamp-to-datetime
  "Takes in a time string (like from timestamp) and returns a Java datetime object."
  [s]
  (format/parse s))

(defn code-expired?
  "Takes in the activation-code-created-at string stored in the db,
  and returns true if the code has expired (over 1 hr old)."
  [code-time-str]
  (let [time-hrs (time/in-hours
                  (time/interval (format/parse code-time-str)
                                 (time/now)))]
    (if (< time-hrs 1)
      false
      true)))

(defn convert-ms-to-time-map
  "Takes in a time in ms and returns a map of {:hours num :minutes num :seconds num :ms num}"
  [mstime]
  (when (= (type mstime) java.lang.Integer)
    (let [[hrs rem] (div-rem mstime 3600000)
          [mins rem] (div-rem rem 60000)
          [secs rem] (div-rem rem 1000)]
      {:hours hrs
       :minutes mins
       :seconds secs
       :ms rem})))

(defn time-all-zeroes-or-nil?
  "Takes in a map with hours, minutes, seconds, and ms keys and returns true if all values are zero or nil."
  [time-map]
  (when (or (nil? time-map)
            (and
             (= 0 (:hours time-map))
             (= 0 (:minutes time-map))
             (= 0 (:seconds time-map))
             (= 0 (:ms time-map))))
    true))

(defn convert-ms-to-string
  "takes an int representing a number of milliseconds and converts it to hh:mm:ss.ms"
  [mstime]
  (let [tmap (convert-ms-to-time-map mstime)
        hrs (:hours tmap)
        mins (:minutes tmap)
        secs (:seconds tmap)
        ms (:ms tmap)]
    (str (when (not (= 0 hrs))
           (str hrs "hrs "))
         (when (not (and (= 0 hrs)
                         (= 0 mins)))
           (str  mins "min "))
         secs
         (if (not (= 0 ms))
           (str "." ms "sec")
           "sec"))))
