(ns teemo-tool-api.core
  (:gen-class))

(require '[clojure.java.jdbc :as sql]
         '[ring.adapter.jetty :as jetty]
         '[ring.util.response :refer [response]]
         '[ring.middleware.json :refer [wrap-json-response wrap-json-body]]
         '[compojure.core :refer :all]
         '[compojure.route :as route]
         )

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "/home/delta/devel/teemo-tool/db/database.db"})

(defn bets-for-team
  [db team]
  (let [team (clojure.string/lower-case team)
        results (sql/query db ["SELECT amount FROM bets WHERE team = ?
AND
timestamp > date((SELECT MAX(timestamp) FROM bets), '-10 minutes')
" team])]
    (map (fn [row] (get row :amount)) results)
    ))

(bets-for-team db "blue")

(defn handle-team-request
  [team]
  (response {:bets (bets-for-team db team)}))

(def app-routes
  (routes
   (GET "/bets/blue" request (handle-team-request "blue"))
   (GET "/bets/red" request (handle-team-request "red"))
   (route/not-found {:error "not found"})))

(def app
  (-> app-routes
      wrap-json-body
      wrap-json-response
      ))

(.stop server)

(def server (jetty/run-jetty app {:port 3002
                      :join? false}))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
