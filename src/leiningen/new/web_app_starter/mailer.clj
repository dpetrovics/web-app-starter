(ns {{name}}.mailer
  (:use [postal.core :only (send-message)]
        {{name}}.util))

(def site-url
  "http://localhost:8080") ;;Fix Me!

(defn send-email!
  "Accepts a recipient email, a subject line and a body (in text form)
  and sends the message from the email address below."
  [{:keys [to-email subject body] :as m}]
  (send-message ^{:host "smtp.gmail.com"
                  :user "fixme@example.com"
                  :pass "fixme"
                  :port 465}
                {:sender "{{name}}"
                 :from "fixme@example.com"
                 :to to-email
                 :subject subject
                 :body body}))

(defn email-activation-code
  "Emails account activation code (for email validation) to the
  supplied address."
  [{:keys [email first-name activation-code]}]
  (future (send-email!
           {:to-email email
            :subject "Account Activation"
            :body (str "Hi " first-name ",\n\n"
                       "To activate your account please click the link below:\n\n"
                       (str site-url "/activate/" activation-code)
                       "\n\nWelcome!")})))

(defn email-pw-reset-code
  "Emails password reset code to the supplied address."
  [{:keys
    [email first-name reset-code]}]
  (future (send-email!
           {:to-email email
            :subject "Password Reset"
            :body (str "Hi " first-name ",\n\n"
                       "To reset your password please click the link below:\n\n"
                       (str site-url "/reset-pw/" reset-code)
                       "\n\n Thanks!")})))
