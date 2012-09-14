(ns {{name}}.views.login
    (:use noir.core
          {{name}}.util
          net.cgrand.enlive-html
          forms-bootstrap.core)
    (:require [noir.session :as session]
              [noir.response :as response]
              [noir.validation :as vali]
              [{{name}}.mailer :as mailer]
              [{{name}}.views.shared :as shared]
              [{{name}}.models.user :as user]))

(form-helper login-form
             :validator user/login-validator
             :post-url "login"
             :fields [{:name "username"
                       :label "Username" 
                       :type "text"} 
                      {:name "password"
                       :label "Password"
                       :type "password"}]
             :submit-label "Login"
             :on-success (fn [{uname :username :as m}]
                           (user/login! uname)
                           (session/flash-put! :flash ["success" "Logged in successfully."])
                           (response/redirect "/"))
             :on-failure (fn [form-data]
                           (if-let [err-msg (peek (vali/on-error :form identity))]
                             (session/flash-put! :flash err-msg)
                             (session/flash-put! :flash "Please Fix Errors"))      
                           (render "/login" form-data)))

(defpage "/login" {:as m}
  (shared/page {:main (do-> (substitute (login-form m "login" "/"))
                            (append (shared/link-to "forgot-password"
                                                    "Forgot your password?"))
                            (wrap :div {:id "login_links"}))}))

(form-helper signup-form
             :validator user/signup-validator
             :post-url "/signup"
             :submit-label "Sign Up!"
             :fields [{:name "first-name" 
                       :label "First Name"
                       :type "text"}
                      {:name "last-name"
                       :label "Last Name"
                       :type "text"}
                      {:name "gender"
                       :label "Gender"
                       :type "radio"
                       :inputs [["male" "Male"] ["female" "Female"]]}
                      {:name "email"
                       :label "Email Address" 
                       :type "text"}
                      {:type "inline-fields"
                       :name "birthday"
                       :label "Birthday"
                       :columns [{:name "birthday-day"
                                  :type "select"
                                  :size "input-small" 
                                  :inputs (let [days (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (range 1 32))]
                                            (insert days 0 ["" "Day"]))}
                                 {:name "birthday-month"
                                  :type "select"
                                  :size "input-small" 
                                  :inputs (let [days (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (range 1 13))]
                                            (insert days 0 ["" "Month"]))}
                                 {:name "birthday-year"
                                  :type "select"
                                  :size "input-small"
                                  :inputs (let [year (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (reverse
                                                                 (range 1900 2013)))]
                                            (insert year 0 ["" "Year"]))}]}
                      {:name "phone"
                       :label "Phone Number"
                       :type "text"}
                      {:name "username"
                       :label "Username"
                       :type "text"}
                      {:name "password"
                       :label "Password"
                       :type "password"}
                      {:name "password_repeat"
                       :label "Confirm Password"
                       :type "password"}]
             :on-success (fn [{uname :username :as user-map}]
                           (user/create! user-map)
                           ;;  (user/login! uname)
                           (session/flash-put! :flash ["success"
                                                       "Please check your email
                                            to activate your
                                            account!"])
                           (response/redirect "/"))
             :on-failure (fn [form-data]
                           (session/flash-put! :flash "Please Fix Errors")
                           ;;form-data map will not contain checkboxes or radios
                           ;;(like gender) if the user has not selected one
                           (render "/signup" form-data)))

(defpage "/signup" {:as m}
  (shared/page {:main (substitute (signup-form m "/signup" "/"))
                :sources []})) 

(defpage "/logout" []
  (do
    (user/logout!)
    (session/flash-put! :flash ["success"
                                "Logged out successfully."])
    (response/redirect "/"))) 

;;Page for activating account
(defpage "/activate/:activation-code" {:keys [activation-code]}
  (if-let [user (user/activate! activation-code)]
    (do
      (session/clear!) ;;not sure if we need this, just in case
      (user/login! (:username user))
      (session/flash-put! :flash ["success"
                                  "Your account has been activated. Welcome!"])
      (response/redirect "/profile")) 
    (do
      (session/flash-put! :flash ["error"
                                  (first (vali/get-errors))])
      (response/redirect "/"))))  

(form-helper forgot-password-form
             :validator user/forgot-password-validator
             :post-url "forgot-password"
             :submit-label "Reset Password"
             :fields [{:name "email" 
                       :label "Email Address"
                       :type "text"}]
             :on-success (fn [{:keys [email] :as user-map}]
                           (if-let [user-doc (user/reset-password email)]
                             (do
                               (mailer/email-pw-reset-code {:email (:email user-doc)
                                                            :first-name (:first-name user-doc)
                                                            :reset-code (:pw-reset-code user-doc)})
                               (session/flash-put! :flash ["success"
                                                           "We have sent you an
                                            email with instructions on
                                            how to reset your
                                            password."])
                               (response/redirect "/"))                  
                             (do
                               (session/flash-put! :flash ["error"
                                                           (first (vali/get-errors))])
                               (render "/forgot-password" user-map))))
             :on-failure (fn [form-data]
                           (if-let [errs (vali/get-errors)]
                             (session/flash-put! :flash (first errs))
                             (session/flash-put! :flash "Please Fix Errors"))
                           (render "/forgot-password" form-data)))

(defpage "/forgot-password"
  {:as m}
  (shared/page {:main (substitute (forgot-password-form m
                                                        "forgot-password"
                                                        "/login"))
                :sources []}))

(defpage "/reset-activation" {:keys [username] :as m}
  (if-let [user (user/reset-activation-code username)]
    (do 
      (mailer/email-activation-code {:email (:email user)
                                     :first-name (:first-name user)
                                     :activation-code (:activation-code user)})
      (session/flash-put! :flash ["success"
                                  "Please check your email for the new
                              activation link."])
      (response/redirect "/"))
    (do
      (session/flash-put! :flash (first (vali/get-errors)))
      (response/redirect "/"))))

(defpage "/reset-pw-code" {:keys [username] :as m}
  (if-let [user (user/reset-pw-code username)]
    (do 
      (mailer/email-pw-reset-code {:email (:email user)
                                   :first-name (:first-name user)
                                   :reset-code (:pw-reset-code user)})
      (session/flash-put! :flash ["success"
                                  "Please check your email for the new
                              reset password link."])
      (response/redirect "/"))
    (do
      (session/flash-put! :flash (first (vali/get-errors)))
      (response/redirect "/"))))

(form-helper reset-password-form
  :validator user/reset-password-validator
  :legend "Reset Password"
  :post-url "/reset-pw/:code"
  :submit-label "Reset Password"
  :fields [{:name "password" 
            :label "New Password"
            :type "password"} 
           {:name "password_repeat" 
            :label "Confirm Password"
            :type "password"}]
  :on-success (fn [{:keys [password password-repeat code] :as user-map}]
                (user/change-password-with-code password code)
                (session/flash-put! :flash ["success"
                                            "Your password has been reset. Log in below"])
                (response/redirect "/login"))
  :on-failure (fn [form-data]
                (println "FSDFDS " form-data)
                (if-let [errs (vali/get-errors)]
                  (session/flash-put! :flash (first errs))
                  (session/flash-put! :flash "Please Fix Errors"))
                (render "/reset-pw/:code" form-data)))

(defpage "/reset-pw/:code"
  {:keys [code] :as m}
  (if-let [user-doc (user/validate-reset-code code)]
    (shared/page {:main (substitute (reset-password-form m
                                                         (str "/reset-pw/"
                                                              code)
                                                         "/"))
                  :sources []})
    (do (session/flash-put! :flash ["error"
                                    (first (vali/get-errors))])
        (response/redirect "/"))))
